"""
ids:
    [4,5,6,7,8,11,12,13,17,18,19,38,42,47,49,50,53,57,61,63,68,70,71,72,74,78,80,81,84,92,100,104,105]

11 dupl 6
12 dupl 4
18 kinda dupl 12 (filtering through different, though still unindexed column)
19 dupl 11
26 dupl

team:
    X 0
    with members:
        [16 29 94+emails >> 45+roles] 104(but different filter)
    with attrs:
        [24 > 33+orgScope] 97+options 85+features

    14+ooo
    35+features+workflows
    51+members+workflows
    55+features+schedules
    91+features+roles
    98+bookings+attendees

    76+features+orgScope(only filter)
    X 102+orgScope

    X 62+workflows+wfActions
    109+wfActions

user:
    with at most emails:
        X [5 15 25 1]
    with ooo:
        X [71 84 105]
        36+schedule 57+booking 95+availability
    with team features:
        9+orgScope 30+role
        X [60 107]

    X 13+availability (superceded?)
    70+availability (filter tho)
    X 22+schedule (superceded?)
    X 90+team+role (superceded?)

    50+email+availability
    78+email+membership
    67+email+orgScope

    47+booking+attendees
    54+role+attrOption
    101+features+teams+attrOptionValue
    108+booking+schedule

role:
    X [2 20+team 69+member+user 73+member]
    40+team+member+user

booking:
    with at most emails:
        X [3 4 12 18 26 53 72 81]

    10+emails+host+availablity
    32+emails+host+team+feature
    38+emails+host+user+email
    X 68+emails+host+user

workflow (+wf):
    with at most steps:
        X [11 19 27 56 74 93]
        6 80

    41+team+eventtype
    X 66+team+eventtype

    34+steps+team 86+eventtype+user 99+ownerUser+team

    106+eventType

workflowStep: 7

email: 8

eventType (+et):
    X 28+userOnEt
    37+host+userOnEt

    X 46+workflow
    X 52+workflow+host
    31+host+user+workflow

    17+host+schedule

    X 83+availability
    77+user+availability

    X 61+booking
    92+booking

    87+hostGroup+membership
    X 96+hostGroup+membership

    103+host+user+membership

attribute:
    X [21+options 39+options 58+options 75+options]

    64+member+user
    88+options+membership

feature (+feat):
    X 23+team
    X 44+user+team
    48+user+team

schedule (+sched):
    X 42+eventType+availability

    49+user+email+availiability
    63+user+availability

    X 79+booking
    89+eventType+booking
    100+eventType

membership:
    43+attributeValue+team
    X 59+??? this might actually be mistake
    82+emails

hostGroup: 65
"""



base = [i for i in range(110)]
noerr = [0, 1, 2, 4, 5, 6, 7, 8, 11, 12, 13, 15, 17, 18, 19, 21, 22, 24, 25, 26, 27, 36, 38, 39, 42, 47, 49, 50, 53, 56, 57, 58, 61, 62, 63, 68, 70, 71, 72, 74, 78, 80, 81, 84, 92, 93, 95, 97, 99, 100, 104, 105]
perf = [4,5,6,7,8,11,12,13,17,18,19,38,42,47,49,50,53,57,61,63,68,70,71,72,74,78,80,81,84,92,100,104,105]
redundant = [0, 1, 3, 4, 5, 11, 12, 13, 15, 16, 18, 19, 21, 22, 23, 24, 25, 26, 27, 28, 29, 39, 42, 44, 46, 52, 53, 56, 58, 59, 60, 61, 62, 66, 68, 71, 72, 74, 75, 79, 81, 83, 84, 90, 93, 94, 96, 102, 105, 107]

deduplicated = []
dPerf = []
dNoerr = []

for i in base:
    if i not in redundant:
        newIdx = len(deduplicated)

        deduplicated.append(newIdx)
        if i in perf:
            dPerf.append(newIdx)
        if i in noerr:
            dNoerr.append(newIdx)

print(len(deduplicated))
print()
print(dPerf)
print(len(dPerf))
print()
print(dNoerr)
print(len(dNoerr))
