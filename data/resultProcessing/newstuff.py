#!/usr/bin/env python
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.markers as markers

DBMSS = 'postgresql', 'mongodb', 'neo4j'
dfs = {
    'base': [],
    'predpushdown': [],
    'depjoins': [],
}

for dbms in DBMSS:
    for opt in 'base', 'predpushdown', 'depjoins':
        df = pd.read_csv('../cal.com-benchmark-{0}-{1}.csv'.format(dbms, opt))
        df['dbms'] = dbms
        df['totalMs'] = df['planningMs'] + df['innerSelectionMs'] + \
            df['underlyingSelectionMs'] + df['projectionMs']
        df['noPlanMs'] = df['innerSelectionMs'] + \
            df['underlyingSelectionMs'] + df['projectionMs']

        df = df[20:] # warmup

        dfs[opt].append(df)

base: pd.DataFrame = pd.concat(dfs['base'])
predpushdown: pd.DataFrame = pd.concat(dfs['predpushdown'])
depjoins: pd.DataFrame = pd.concat(dfs['depjoins'])



# region Query Phases

# Planning | Underlying Selection | Unifying Selection | Finalization

def query_phases(origdf, name):
    _, ax = plt.subplots()
    ax.set_xlabel('Underlying DBMS')
    ax.set_ylabel('Time [ms]')

    df = origdf[['dbms', 'planningMs','underlyingSelectionMs','innerSelectionMs','projectionMs']].groupby('dbms').mean()

    x = df.index
    ax.bar(x, df['planningMs'], label='planning', width=0.5)
    acc = df['planningMs']
    ax.bar(x, df['underlyingSelectionMs'], label='underlying selection', bottom=acc, width=0.5)
    acc += df['underlyingSelectionMs']
    ax.bar(x, df['innerSelectionMs'], label='unifying selection', bottom=acc, width=0.5)
    acc += df['innerSelectionMs']
    ax.bar(x, df['projectionMs'], label='finalization', bottom=acc, width=0.5)

    ax.legend(loc='upper right')
    plt.savefig(name)

query_phases(base, "query-phases-base-1-2.png")
query_phases(base[base['queryIdx'] < 10], "query-phases-base-1.png")
query_phases(base[base['queryIdx'] >= 10], "query-phases-base-2.png")
query_phases(depjoins, "query-phases-depjoins-1-2.png")
query_phases(depjoins[depjoins['queryIdx'] < 10], "query-phases-depjoins-1.png")
query_phases(depjoins[depjoins['queryIdx'] >= 10], "query-phases-depjoins-2.png")

# endregion



# region Opt. Impact

def opt_impact(orig_dfs, column, name):
    fig, ax = plt.subplots()
    # ax.set_xlabel(idk)
    ax.set_ylabel('Time [ms]')

    for df, label in orig_dfs:
        df_sorted = df[column][df['error'].isna()].sort_values().reset_index()[column]
        ax.plot(df_sorted, label=label)

    ax.set_yscale('log')
    ax.legend()
    plt.savefig(name)

opt_impact([(base, 'Baseline'), (predpushdown, 'Predicate pushdown')], 'totalMs', 'opt-impact-1.png')
opt_impact([(base, 'Baseline'), (predpushdown, 'Predicate pushdown'), (depjoins, 'Dependent joins')], 'totalMs', 'opt-impact-2.png')
opt_impact([(base, 'Baseline'), (predpushdown, 'Predicate pushdown'), (depjoins, 'Dependent joins')], 'noPlanMs', 'opt-impact-2-1.png')
opt_impact([
    (base[(base['queryIdx'] >= 10) & (base['dbms'] == 'mongodb')], 'Baseline'),
    (predpushdown[(predpushdown['queryIdx'] >= 10) & (predpushdown['dbms'] == 'mongodb')], 'Predicate pushdown'),
    (depjoins[(depjoins['queryIdx'] >= 10) & (depjoins['dbms'] == 'mongodb')], 'Dependent joins')
], 'totalMs', 'opt-impact-3.png')

# endregion




# region Planning Times
# An elaboration on the planning mechanism fault (preceded by phases chart)

fig, ax = plt.subplots()
ax.set_xlabel('Kind count')
ax.set_ylabel('Planning time [ms]')

# ax.set_xscale('log')

x = depjoins['numberOfKinds']
y = depjoins['planningMs']
ax.scatter(x, y, marker="x", label="measurements")

agg = depjoins[['numberOfKinds', 'planningMs']].groupby(['numberOfKinds']).mean()
ax.plot(agg.index, agg['planningMs'], color='orange', label="mean")

ax.legend()
plt.savefig("planning-time.png")

# endregion



# region Compare All

def group_bar_with_errors(df, name, column):
    fig, ax = plt.subplots()
    ax.set_xlabel('Query index')
    ax.set_ylabel('Time [ms]')

    width = 0.25
    xs = [-width, 0, width]
    colors = ['tab:orange', 'tab:green', 'tab:blue']
    for i in range(3):
        dbms = DBMSS[i]
        offset = xs[i]
        color = colors[i]

        df1 = df[df['dbms'] == dbms][['queryIdx', column]].groupby('queryIdx').mean()
        df2 = df[df['dbms'] == dbms][['queryIdx', column]].groupby('queryIdx').std()

        x = df1.index
        ax.bar(x + offset, df1[column], width=width, label=dbms, color=color)
        ax.errorbar(x + offset, df1[column], yerr=df2[column], fmt=".", color='tab:red')

    ax.set_xticks(x, x)
    ax.set_yscale('log')
    ax.legend()
    plt.savefig(name)

group_bar_with_errors(depjoins[(depjoins['queryIdx'] < 10)], "compare-all-1.png", "totalMs")
group_bar_with_errors(depjoins[(depjoins['queryIdx'] >= 10)], "compare-all-2.png", "totalMs")

group_bar_with_errors(depjoins[(depjoins['queryIdx'] < 10)], "compare-all-1-noplan.png", "noPlanMs")
group_bar_with_errors(depjoins[(depjoins['queryIdx'] >= 10)], "compare-all-2-noplan.png", "noPlanMs")

# endregion



# region Combination

_, ax = plt.subplots()
ax.set_xlabel('Underlying DBMS')
ax.set_ylabel('Time [ms]')

column = 'noPlanMs'
newdf = depjoins[depjoins['dbms'] == 'postgresql']
newdf.loc[newdf['error'].isna(), 'dbms'] = 'combination'

newdf.loc[newdf['error'].isna(), column] = np.minimum(
    np.minimum(
        depjoins[depjoins['dbms'] == 'postgresql'][column],
        depjoins[depjoins['dbms'] == 'mongodb'][column]
    ),
    depjoins[depjoins['dbms'] == 'neo4j'][column]
)

newdf = pd.concat([depjoins, newdf])

df = newdf[['dbms', column]].groupby('dbms').mean()

x = df.index
ax.bar(x, df[column], width=0.5)
plt.savefig('combination.png')


opt_impact([
    (newdf[newdf['dbms'] == 'postgresql'], 'PostgreSQL'),
    (newdf[newdf['dbms'] == 'mongodb'], 'MongoDB'),
    (newdf[newdf['dbms'] == 'neo4j'], 'Neo4j'),
    (newdf[newdf['dbms'] == 'combination'], 'Best performing plan'),
], column, 'combination-impact.png')

# endregion



"""
query phases for base (possibly split into group 1 and 2)
improvement for predicate pushdown (sorted)
improvement for depjoins
improvement for depjoins (noplan)
improvement for depjoins (total, but only mongodb and query above 10)
query phases for depjoins (split)

Note on neo4j large planning & planning-time graph

compare-all (note on plan redundancy with neo4j and compare-all-noplan)


TODO one last one after compare all - something like query phases, but without plan, and with additional column of minimum of all of them
"""
