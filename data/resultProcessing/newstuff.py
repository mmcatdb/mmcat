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
# 

# Planning | Underlying Selection | Unifying Selection | Finalization

fig, ax = plt.subplots()
ax.set_xlabel('Underlying DBMS')
ax.set_ylabel('Time [ms]')

df = depjoins[['dbms', 'planningMs','underlyingSelectionMs','innerSelectionMs','projectionMs']][depjoins['queryIdx'] < 10].groupby('dbms').mean()

x = df.index
ax.bar(x, df['planningMs'], label='planning', width=0.5)
acc = df['planningMs']
ax.bar(x, df['underlyingSelectionMs'], label='underlying selection', bottom=acc, width=0.5)
acc += df['underlyingSelectionMs']
ax.bar(x, df['innerSelectionMs'], label='unifying selection', bottom=acc, width=0.5)
acc += df['innerSelectionMs']
ax.bar(x, df['projectionMs'], label='finalization', bottom=acc, width=0.5)

ax.legend(loc='upper right')
plt.savefig("query-phases-1.png")



fig, ax = plt.subplots()
ax.set_xlabel('Underlying DBMS')
ax.set_ylabel('Time [ms]')

# ax.set_xscale('log')

df = depjoins[['dbms', 'planningMs','underlyingSelectionMs','innerSelectionMs','projectionMs']][depjoins['queryIdx'] >= 10].groupby('dbms').mean()

x = df.index
ax.bar(x, df['planningMs'], label='planning', width=0.5)
acc = df['planningMs']
ax.bar(x, df['underlyingSelectionMs'], label='underlying selection', bottom=acc, width=0.5)
acc += df['underlyingSelectionMs']
ax.bar(x, df['innerSelectionMs'], label='unifying selection', bottom=acc, width=0.5)
acc += df['innerSelectionMs']
ax.bar(x, df['projectionMs'], label='finalization', bottom=acc, width=0.5)

ax.legend()
plt.savefig("query-phases-2.png")

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
