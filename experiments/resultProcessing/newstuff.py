#!/usr/bin/env python
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# region Data Setup

PADDING = 0

COLOR_BLU_HI = '#dae8fc'
COLOR_BLU_LO = '#6c8ebf'
COLOR_ORA_HI = '#ffe6cc'
COLOR_ORA_LO = '#d79b00'
COLOR_GRE_HI = '#d5e8d4'
COLOR_GRE_LO = '#82b366'
COLOR_RED_HI = '#f8cecc'
COLOR_RED_LO = '#b85450'
COLOR_PUR_HI = '#e1d5e7'
COLOR_PUR_LO = '#9673a6'

GRID_COLOR = '#dddddd'

DBMSS = 'PostgreSQL', 'MongoDB', 'Neo4j'
ALLDBMSS = 'PostgreSQL', 'MongoDB', 'Neo4j', 'Multimodel'
DBMS_COLORS = COLOR_PUR_LO, COLOR_GRE_LO, COLOR_BLU_LO, COLOR_ORA_LO
OPTS = 'base', 'predpushdown', 'depjoins', 'fastdrafting'
dfs = {
    opt: [] for opt in OPTS
}

for dbms in ALLDBMSS:
    for opt in OPTS:
        df = pd.read_csv('../cal.com-benchmark-{0}-{1}.csv'.format(dbms.lower(), opt))
        df['dbms'] = dbms
        df['totalMs'] = df['planningMs'] + df['innerSelectionMs'] + \
            df['underlyingSelectionMs'] + df['projectionMs']
        df['noPlanMs'] = df['innerSelectionMs'] + \
            df['underlyingSelectionMs'] + df['projectionMs']

        df = df[20:] # remove warmup TODO increase with bigger samples

        dfs[opt].append(df)

base: pd.DataFrame = pd.concat(dfs['base'])
predpushdown: pd.DataFrame = pd.concat(dfs['predpushdown'])
depjoins: pd.DataFrame = pd.concat(dfs['depjoins'])
fastdrafting: pd.DataFrame = pd.concat(dfs['fastdrafting'])



# region Query Phases

"""
# Planning | Underlying Selection | Unifying Selection | Finalization

def query_phases(origdf, name):
    fig, ax = plt.subplots()
    ax.set_xlabel('Underlying DBMS')
    ax.set_ylabel('Time [ms]')
    ax.grid(True, 'both', 'y', color=GRID_COLOR)
    ax.set_axisbelow(True)

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
    fig.tight_layout(pad=PADDING)
    plt.savefig(name, format='pdf')
    plt.close()

query_phases(base, 'query-phases-base-1-2.pdf')
query_phases(depjoins, 'query-phases-depjoins-1-2.pdf')
query_phases(fastdrafting, 'query-phases-fastdrafting-1-2.pdf')
"""

# endregion



# region Opt. Impact

def opt_impact(orig_dfs, column, name):
    fig, ax = plt.subplots()
    ax.set_xlabel('Sorted query execution measurement index')
    ax.set_ylabel('Time [ms]')
    ax.grid(True, 'both', 'y', color=GRID_COLOR)
    ax.set_axisbelow(True)

    for d in orig_dfs:
        if len(d) == 3:
            df, label, color = d
            df_sorted = df[column][df['error'].isna()].sort_values().reset_index()[column]
            ax.plot(df_sorted, label=label, color=color)
        else:
            df, label = d
            df_sorted = df[column][df['error'].isna()].sort_values().reset_index()[column]
            ax.plot(df_sorted, label=label)

    ax.set_yscale('log')
    ax.legend()
    fig.tight_layout(pad=PADDING)
    plt.savefig(name, format='pdf')
    plt.close()

opt_impact([(base, 'Baseline'), (predpushdown, 'Predicate pushdown'), (depjoins, 'Dependent joins'), (fastdrafting, 'Fast plan drafting')], 'totalMs', 'opt-impact-2-0.pdf')

for dbms in ALLDBMSS:
    opt_impact([(base[base['dbms'] == dbms], 'Baseline'), (predpushdown[predpushdown['dbms'] == dbms], 'Predicate pushdown'), (depjoins[depjoins['dbms'] == dbms], 'Dependent joins'), (fastdrafting[fastdrafting['dbms'] == dbms], 'Fast plan drafting')], 'totalMs', 'opt-impact-2-{}.pdf'.format(dbms))

# endregion

# region Impact per-query

def impact_per_query(orig_dfs, name):
    fig, ax = plt.subplots()
    ax.set_xlabel('Query index')
    ax.set_ylabel('Time [ms]')
    ax.grid(True, 'both', 'y', color=GRID_COLOR)
    ax.set_axisbelow(True)

    width = 0.2
    xs = [-width * 3/2, -width * 1/2, width * 1/2, width * 3/2]
    for i in range(len(orig_dfs)):
        df, label = orig_dfs[i]
        offset = xs[i]

        means = df[['queryIdx', 'totalMs']].groupby('queryIdx').mean()
        stds = df[['queryIdx', 'totalMs']].groupby('queryIdx').std()

        x = means.index
        ax.bar(x + offset, means['totalMs'], width=width, label=label)
        # ax.errorbar(x + offset, means[column], yerr=stds[column], fmt='.', color='tab:red')
        if i == 0: ax.set_xticks(x, x)
    ax.set_yscale('log')
    ax.legend()
    fig.tight_layout(pad=PADDING)
    # fig.set_figwidth(12)
    fig.set_figwidth(9)
    fig.set_figheight(3)
    plt.savefig(name, format='pdf')
    plt.close()

for dbms in ALLDBMSS:
    impact_per_query([
        (base[base['dbms'] == dbms], 'Base'),
        (predpushdown[predpushdown['dbms'] == dbms], 'Predicate pushdown'),
        (depjoins[depjoins['dbms'] == dbms], 'Dependent joins'),
        (fastdrafting[fastdrafting['dbms'] == dbms], 'Fast plan drafting')
    ], 'impact-per-query-{}.pdf'.format(dbms))

# endregion



# region Planning Times
# An elaboration on the planning mechanism fault (preceded by phases chart)

fig, ax = plt.subplots()
ax.set_xlabel('Kind count')
ax.set_ylabel('Planning time [ms]')
ax.set_yscale('log')
ax.grid(True, 'both', 'y', color=GRID_COLOR)
ax.set_axisbelow(True)

x = depjoins['numberOfKinds']
y = depjoins['planningMs']
ax.plot(x, y, marker='o', fillstyle='none', lw=0, label='unoptimized measurements', color='tab:blue')
agg = depjoins[['numberOfKinds', 'planningMs']].groupby(['numberOfKinds']).mean()
ax.plot(agg.index, agg['planningMs'], color='tab:blue', label='unoptimized mean')

x = fastdrafting['numberOfKinds']
y = fastdrafting['planningMs']
ax.plot(x, y, marker='o', fillstyle='none', lw=0, label='optimized measurements', color='tab:orange')
agg = fastdrafting[['numberOfKinds', 'planningMs']].groupby(['numberOfKinds']).mean()
ax.plot(agg.index, agg['planningMs'], color='tab:orange', label='optimized mean')

ax.legend()
fig.tight_layout(pad=PADDING)
plt.savefig('planning-time.pdf', format='pdf')
plt.close(fig)


for dbms in ALLDBMSS:
    fig, ax = plt.subplots()
    ax.set_xlabel('Kind count')
    ax.set_ylabel('Planning time [ms]')
    ax.grid(True, 'both', 'y', color=GRID_COLOR)
    ax.set_axisbelow(True)

    x = depjoins[depjoins['dbms'] == dbms]['numberOfKinds']
    y = depjoins[depjoins['dbms'] == dbms]['planningMs']
    ax.plot(x, y, marker='o', fillstyle='none', lw=0, label='unoptimized measurements', color='tab:blue')
    agg = depjoins[depjoins['dbms'] == dbms][['numberOfKinds', 'planningMs']].groupby(['numberOfKinds']).mean()
    ax.plot(agg.index, agg['planningMs'], color='tab:blue', label='unoptimized mean')

    x = fastdrafting[fastdrafting['dbms'] == dbms]['numberOfKinds']
    y = fastdrafting[fastdrafting['dbms'] == dbms]['planningMs']
    ax.plot(x, y, marker='o', fillstyle='none', lw=0, label='optimized measurements', color='tab:orange')
    agg = fastdrafting[fastdrafting['dbms'] == dbms][['numberOfKinds', 'planningMs']].groupby(['numberOfKinds']).mean()
    ax.plot(agg.index, agg['planningMs'], color='tab:orange', label='optimized mean')

    ax.legend()
    fig.tight_layout(pad=PADDING)
    plt.savefig('planning-time-{}.pdf'.format(dbms), format='pdf')
    plt.close()


# endregion



# region Compare All

def group_bar_with_errors(df, name, column):
    fig, ax = plt.subplots()
    ax.set_xlabel('Query index')
    ax.set_ylabel('Time [ms]')
    ax.grid(True, 'both', 'y', color=GRID_COLOR)
    ax.set_axisbelow(True)

    width = 0.2
    xs = [-width * 3/2, -width * 1/2, width * 1/2, width * 3/2]
    for i in range(len(ALLDBMSS)):
        dbms = ALLDBMSS[i]
        offset = xs[i]
        color = DBMS_COLORS[i]

        df1 = df[df['dbms'] == dbms][['queryIdx', column]].groupby('queryIdx').mean()
        df2 = df[df['dbms'] == dbms][['queryIdx', column]].groupby('queryIdx').std()

        x = df1.index
        ax.bar(x + offset, df1[column], width=width, label=dbms, color=color)
        ax.errorbar(x + offset, df1[column], yerr=df2[column], fmt='.', color='tab:red')

    ax.set_xticks(x, x)
    ax.set_yscale('log')
    ax.legend()
    # fig.set_figwidth(12)
    fig.set_figwidth(9)
    fig.set_figheight(3)
    fig.tight_layout(pad=PADDING)
    plt.savefig(name, format='pdf')
    plt.close()

group_bar_with_errors(base, 'compare-all-unopt.pdf', 'totalMs')
group_bar_with_errors(fastdrafting, 'compare-all-opt.pdf', 'totalMs')

# endregion



# region Combination

fig, ax = plt.subplots()
ax.set_xlabel('Underlying DBMS')
ax.set_ylabel('Time [ms]')
ax.grid(True, 'both', 'y', color=GRID_COLOR)
ax.set_axisbelow(True)

column = 'noPlanMs'
newdf = depjoins[depjoins['dbms'] == 'PostgreSQL']
newdf.loc[newdf['error'].isna(), 'dbms'] = 'combination'

newdf.loc[newdf['error'].isna(), column] = np.minimum(
    np.minimum(
        depjoins[depjoins['dbms'] == 'PostgreSQL'][column],
        depjoins[depjoins['dbms'] == 'MongoDB'][column]
    ),
    depjoins[depjoins['dbms'] == 'Neo4j'][column]
)

newdf = pd.concat([depjoins, newdf])

df = newdf[['dbms', column]].groupby('dbms').mean()

x = df.index
ax.bar(x, df[column], width=0.5)
fig.tight_layout(pad=PADDING)
plt.savefig('combination.pdf', format='pdf')
plt.close()


opt_impact([
    (newdf[newdf['dbms'] == 'PostgreSQL'], 'PostgreSQL', DBMS_COLORS[0]),
    (newdf[newdf['dbms'] == 'MongoDB'], 'MongoDB', DBMS_COLORS[1]),
    (newdf[newdf['dbms'] == 'Neo4j'], 'Neo4j', DBMS_COLORS[2]),
    (newdf[newdf['dbms'] == 'combination'], 'Best performing plan', COLOR_RED_LO),
], column, 'combination-impact.pdf')

# endregion

# region Query Phases 2
# logarithmic grouped graphs (only for one level of optimization)


def query_phases_2(df, dbms, columns, name):
    fig, ax = plt.subplots()
    ax.set_xlabel('Query index')
    ax.set_ylabel('Time [ms]')
    ax.grid(True, 'both', 'y', color=GRID_COLOR)
    ax.set_axisbelow(True)

    width = 0.2
    xs = [-width * 3/2, -width * 1/2, width * 1/2, width * 3/2] if len(columns) == 4 else [-width, 0, 0, width]

    colors = ['tab:blue', 'tab:orange', 'tab:green', 'tab:red']

    means = df[df['dbms'] == dbms][['queryIdx', 'planningMs','underlyingSelectionMs','innerSelectionMs','projectionMs']].groupby('queryIdx').mean()
    stds = df[df['dbms'] == dbms][['queryIdx', 'planningMs','underlyingSelectionMs','innerSelectionMs','projectionMs']].groupby('queryIdx').std()

    phases = 'planningMs','underlyingSelectionMs','innerSelectionMs','projectionMs'
    for i in columns:
        column = phases[i]
        offset = xs[i]
        color = colors[i]

        x = means.index
        ax.bar(x + offset, means[column], width=width, label=column, color=color)
        # ax.errorbar(x + offset, means[column], yerr=stds[column], fmt='.', color='tab:red')

    ax.set_xticks(x, x)
    ax.set_yscale('log')
    ax.legend()
    fig.set_figwidth(9)
    fig.set_figheight(3)
    fig.tight_layout(pad=PADDING)
    plt.savefig(name, format='pdf')
    plt.close()

for dbms in ALLDBMSS:
    cols = [0, 1, 2, 3] if dbms in ('MongoDB', 'Multimodel') else [0, 1, 3]

    # query_phases_2(depjoins[(depjoins['error'].isna()) & (depjoins['queryIdx'] < 10)], dbms, cols, 'query-phases-v2-{}-1.pdf'.format(dbms.lower()))
    # query_phases_2(depjoins[(depjoins['error'].isna()) & (depjoins['queryIdx'] >= 10)], dbms, cols, 'query-phases-v2-{}-2.pdf'.format(dbms.lower()))
    query_phases_2(base[base['error'].isna()], dbms, cols, 'query-phases-unopt-{}.pdf'.format(dbms.lower()))
    query_phases_2(fastdrafting[fastdrafting['error'].isna()], dbms, cols, 'query-phases-opt-{}.pdf'.format(dbms.lower()))

# endregion
