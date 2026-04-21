#!/usr/bin/env python
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt


baseline = pd.read_csv('baseline.csv')
predPushdown = pd.read_csv('predicate_pushdown.csv')
dependentJoins = pd.read_csv('dependent_joins.csv')
new = pd.read_csv('new.csv')

baseline['total'] = baseline['planningMs'] + baseline['innerSelectionMs'] + baseline['underlyingSelectionMs'] + baseline['projectionMs']
predPushdown['total'] = predPushdown['planningMs'] + predPushdown['innerSelectionMs'] + predPushdown['underlyingSelectionMs'] + predPushdown['projectionMs']
dependentJoins['total'] = dependentJoins['planningMs'] + dependentJoins['innerSelectionMs'] + dependentJoins['underlyingSelectionMs'] + dependentJoins['projectionMs']
new['total'] = new['planningMs'] + new['innerSelectionMs'] + new['underlyingSelectionMs'] + new['projectionMs']

fig_title = 'Comparison'

fig, ax = plt.subplots()
ax.set_xlabel('QueryTime')
ax.set_ylabel('Count')

ax.set_xscale('log')

ax.hist(baseline['total'], bins=32, label='baseline')

ax.legend()
ax.set_title(fig_title)
plt.savefig("test1.png")



# fig_title = 'Comparison'

# fig, ax = plt.subplots()
# ax.set_xlabel('QueryTime')
# ax.set_ylabel('Count')

# ax.hist(predPushdown[:55][predPushdown['datasourceNodes'] > 1]['total'], bins=range(0, 400, 10), label='predPushdown')
# ax.hist(dependentJoins[:55][dependentJoins['datasourceNodes'] > 1]['total'], bins=range(0, 400, 10), label='dependentJoins', color='#eeaa99aa')

# ax.legend()
# ax.set_title(fig_title)
# plt.savefig("test2.png")


fig_title = 'Query times'

fig, ax = plt.subplots()
ax.set_ylabel('QueryTime [ms]')

ax.set_yscale('log')

ax.boxplot([
    baseline['total'],
    predPushdown['total'],
    dependentJoins['total'],
],tick_labels=[
    'baseline',
    'pred. pushdown',
    'dep. joins'
])

ax.legend()
ax.set_title(fig_title)
plt.savefig("test3.png")



fig_title = 'Rates of improvement'

fig, ax = plt.subplots()
ax.set_xlabel('Query time relative to unoptimized version [%]')

ax.hist(predPushdown['total'] / baseline['total'], bins=32)

ax.legend()
ax.set_title(fig_title)
plt.savefig("test4.png")




fig_title = 'Rates of improvement'

fig, ax = plt.subplots()
ax.set_xlabel('Relative time improvement [%]')

ax.hist(baseline['total'] / predPushdown['total'], bins=32)

ax.legend()
ax.set_title(fig_title)
plt.savefig("test5.png")




fig_title = 'Time per part'

fig, ax = plt.subplots()
ax.set_ylabel('Time [ms]')

ax.boxplot([
    new['planningMs'],
    new['underlyingSelectionMs'],
    new['innerSelectionMs'],
    new['projectionMs'],
],tick_labels=[
    'planning',
    'underlying\nselection',
    'inner\nselection',
    'projection',
])


ax.legend()
ax.set_title(fig_title)
plt.savefig("test6.png")



fig_title = 'Time per part - relative'

fig, ax = plt.subplots()
ax.set_ylabel('Time [% of total]')

ax.boxplot([
    new['planningMs'] / new['total'],
    new['underlyingSelectionMs'] / new['total'],
    new['innerSelectionMs'] / new['total'],
    new['projectionMs'] / new['total'],
],tick_labels=[
    'planning',
    'underlying\nselection',
    'inner\nselection',
    'projection',
])


ax.legend()
ax.set_title(fig_title)
plt.savefig("test7.png")


fig_title = 'Time per part'

fig, ax = plt.subplots()
ax.set_ylabel('Time [ms]')

ax.bar([
    'planning',
    'underlying\nselection',
    'inner\nselection',
    'projection',
],[
    new['planningMs'].mean(),
    new['underlyingSelectionMs'].mean(),
    new['innerSelectionMs'].mean(),
    new['projectionMs'].mean(),
])


ax.legend()
ax.set_title(fig_title)
plt.savefig("test8.png")



fig_title = 'Query times'

fig, ax = plt.subplots()
ax.set_ylabel('QueryTime [ms]')

ax.bar([
    'baseline',
    'pred. pushdown',
    'dep. joins'
], [
    baseline['total'].mean(),
    predPushdown['total'].mean(),
    dependentJoins['total'].mean()
])

ax.legend()
ax.set_title(fig_title)
plt.savefig("test9.png")
