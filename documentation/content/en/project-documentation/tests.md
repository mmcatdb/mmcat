---
title: "Tests"
weight: 50
---

### Packages

The application uses the JUnit 5 framework to test the transformation algorithms because they contain by far the most complex logic. The other packages (`core`, `abstractWrappers`, `wrappers`) are all used by the transformations, so they are partially covered by these tests as well. Also, the `core` and `abstractWrappers` packages consists mostly of definitions of classes or other simple code, so it is not necessary to test them.

### Applications

Both the frontend and the backend application do not have any tests yet. The first one takes care primarily about the UX/UI while the second one typically just serves data. However, both contain a few nontrivial parts which would appreciate proper testing.
