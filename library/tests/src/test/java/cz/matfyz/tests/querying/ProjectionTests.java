package cz.matfyz.tests.querying;

import cz.matfyz.core.querying.QueryStructure;
import cz.matfyz.querying.algorithms.QueryProjector.TransformingQueryStructure;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProjectionTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectionTests.class);

    @Test
    void onlyRootList() {
        new ProjectionTestBase()
            .input(
                new QueryStructure("A[]", true)
            )
            .output(
                new TransformingQueryStructure("A[]", "A[]")
            )
            .expectedTransformation("""
            root
                C.list
                T.list
                W.list
                C.leaf
            """)
            .data("""
            [ "aaa", "bbb" ]
            """)
            .expectedData("""
            [ "aaa", "bbb" ]
            """)
            .run();
    }

    @Test
    void listWithMap() {
        final var output = new TransformingQueryStructure("A[]", "A[]");
        output.children.add(new TransformingQueryStructure("B", "B"));

        new ProjectionTestBase()
            .input(
                new QueryStructure("A[]", true)
                    .addChild(new QueryStructure("B", false))
                    .parent()
            )
            .output(output)
            .expectedTransformation("""
            root
                C.list
                T.list
                W.list
                C.map
                W.map(B)
                T.map(B)
                C.leaf
            """)
            .data("""
            [ { "B": "aaa" }, { "B": "bbb" } ]
            """)
            .expectedData("""
            [ { "B": "aaa" }, { "B": "bbb" } ]
            """)
            .run();
    }

    @Test
    void rename() {
        final var output = new TransformingQueryStructure("A[]", "C[]");
        output.children.add(new TransformingQueryStructure("B", "D"));
        
        new ProjectionTestBase()
            .input(
                new QueryStructure("A[]", true)
                    .addChild(new QueryStructure("B", false))
                    .parent()
            )
            .output(output)
            .expectedTransformation("""
            root
                C.list
                T.list
                W.list
                C.map
                W.map(D)
                T.map(B)
                C.leaf
            """)
            .data("""
            [ { "B": "aaa" }, { "B": "bbb" } ]
            """)
            .expectedData("""
            [ { "D": "aaa" }, { "D": "bbb" } ]
            """)
            .run();
    }

    @Test
    void renameNestedLists() {
        final var output = new TransformingQueryStructure("A[]", "E[]");
        final var b = new TransformingQueryStructure("B[]", "F[]");
        output.children.add(b);
        final var c = new TransformingQueryStructure("C[]", "G[]");
        b.children.add(c);
        c.children.add(new TransformingQueryStructure("D", "H"));
        
        new ProjectionTestBase()
            .input(
                new QueryStructure("A[]", true)
                .addChild(new QueryStructure("B[]", true))
                    .addChild(new QueryStructure("C[]", true))
                        .addChild(new QueryStructure("D", false))
                        .parent()
                    .parent()
                .parent()
            )
            .output(output)
            .expectedTransformation("""
            root
                C.list
                T.list
                W.list
                C.map
                W.map(F[])
                C.list
                T.map(B[])
                T.list
                W.list
                C.map
                W.map(G[])
                C.list
                T.map(C[])
                T.list
                W.list
                C.map
                W.map(H)
                T.map(D)
                C.leaf
            """)
            .data("""
            [ {
                "B[]": [ {
                    "C[]": [ { "D": "a1b1c1d" }, { "D": "a1b1c2d" } ]
                }, {
                    "C[]": [ { "D": "a1b2c1d" }, { "D": "a1b2c2d" } ]
                } ]
            }, {
                "B[]": [ {
                    "C[]": [ { "D": "a2b1c1d" }, { "D": "a2b1c2d" } ]
                }, {
                    "C[]": [ { "D": "a2b2c1d" }, { "D": "a2b2c2d" } ]
                } ]
            } ]
            """)
            .expectedData("""
            [ {
                "F[]": [ {
                    "G[]": [ { "H": "a1b1c1d" }, { "H": "a1b1c2d" } ]
                }, {
                    "G[]": [ { "H": "a1b2c1d" }, { "H": "a1b2c2d" } ]
                } ]
            }, {
                "F[]": [ {
                    "G[]": [ { "H": "a2b1c1d" }, { "H": "a2b1c2d" } ]
                }, {
                    "G[]": [ { "H": "a2b2c1d" }, { "H": "a2b2c2d" } ]
                } ]
            } ]
            """)
            .run();
    }

    @Test
    void newRoot() {
        final var output = new TransformingQueryStructure("C", "C[]");
        output.children.add(new TransformingQueryStructure("D", "D"));
        output.children.add(new TransformingQueryStructure("E", "E"));

        new ProjectionTestBase()
            .input(
                new QueryStructure("A[]", true)
                    .addChild(new QueryStructure("B[]", true))
                        .addChild(new QueryStructure("C", false))
                        .parent()
                    .parent()
                    .addChild(new QueryStructure("D", false))
                    .parent()
                    .addChild(new QueryStructure("E", false))
                    .parent()
            )
            .output(output)
            .expectedTransformation("""
            root
                C.list
                T.list
                T.map(B[])
                T.list
                T.map(C)
                W.list
                C.map:
                --- W.map(D)
                    T.up
                    T.up
                    T.up
                    T.map(D)
                    C.leaf

                --- W.map(E)
                    T.up
                    T.up
                    T.up
                    T.map(E)
                    C.leaf
            """)
            .data("""
            [ {
                "B[]": [ { "C": "a1b1c1" }, { "C": "a1b1c2" } ],
                "D": "a1d",
                "E": "a1e"
            }, {
                "B[]": [ { "C": "a2b1c1" }, { "C": "a2b1c2" } ],
                "D": "a2d",
                "E": "a2e"
            } ]
            """)
            .expectedData("""
            [ {
                "D": "a1d",
                "E": "a1e"
            }, {
                "D": "a1d",
                "E": "a1e"
            }, {
                "D": "a2d",
                "E": "a2e"
            }, {
                "D": "a2d",
                "E": "a2e"
            } ]
            """)
            .run();
    }

    @Test
    void newRootWithList() {
        final var output = new TransformingQueryStructure("C", "C[]");
        output.children.add(new TransformingQueryStructure("F", "F[]"));

        new ProjectionTestBase()
            .input(
                new QueryStructure("A[]", true)
                    .addChild(new QueryStructure("C", false))
                    .parent()
                    .addChild(new QueryStructure("D[]", true))
                        .addChild(new QueryStructure("F", false))
                        .parent()
                    .parent()
            )
            .output(output)
            .expectedTransformation("""
            root
                C.list
                T.list
                T.map(C)
                W.list
                C.map
                W.map(F[])
                C.list
                T.up
                T.map(D[])
                T.list
                T.map(F)
                W.list
                C.leaf
            """)
            .data("""
            [ {
                "C": "a1c",
                "D[]": [ { "F": "a1d1f" }, { "F": "a1d2f" } ]
            }, {
                "C": "a2c",
                "D[]": [ { "F": "a2d1f" }, { "F": "a2d2f" } ]
            } ]
            """)
            .expectedData("""
            [ {
                "F[]": [ "a1d1f", "a1d2f" ]
            }, {
                "F[]": [ "a2d1f", "a2d2f" ]
            } ]
            """)
            .run();
    }

    @Test
    void shortenList() {
        final var output = new TransformingQueryStructure("A[]", "A[]");
        final var b = new TransformingQueryStructure("B[]", "B[]");
        output.children.add(b);
        b.children.add(new TransformingQueryStructure("D", "D[]"));

        new ProjectionTestBase()
            .input(
                new QueryStructure("A[]", true)
                    .addChild(new QueryStructure("B[]", true))
                        .addChild(new QueryStructure("C[]", true))
                            .addChild(new QueryStructure("D", false))
                            .parent()
                        .parent()
                    .parent()
            )
            .output(output)
            .expectedTransformation("""
            root
                C.list
                T.list
                W.list
                C.map
                W.map(B[])
                C.list
                T.map(B[])
                T.list
                W.list
                C.map
                W.map(D[])
                C.list
                T.map(C[])
                T.list
                T.map(D)
                W.list
                C.leaf
            """)
            .data("""
            [ {
                "B[]": [ {
                    "C[]": [ { "D": "a1b1c1d" }, { "D": "a1b1c2d" } ]
                }, {
                    "C[]": [ { "D": "a1b2c1d" }, { "D": "a1b2c2d" } ]
                } ]
            }, {
                "B[]": [ {
                    "C[]": [ { "D": "a2b1c1d" }, { "D": "a2b1c2d" } ]
                }, {
                    "C[]": [ { "D": "a2b2c1d" }, { "D": "a2b2c2d" } ]
                } ]
            } ]
            """)
            .expectedData("""
            [ {
                "B[]": [ {
                    "D[]": [ "a1b1c1d", "a1b1c2d" ]
                }, {
                    "D[]": [ "a1b2c1d", "a1b2c2d" ]
                } ]
            }, {
                "B[]": [ {
                    "D[]": [ "a2b1c1d", "a2b1c2d" ]
                }, {
                    "D[]": [ "a2b2c1d", "a2b2c2d" ]
                } ]
            } ]
            """)
            .run();
    }

    @Test
    void shortenListFromRoot() {
        final var output = new TransformingQueryStructure("A[]", "A[]");
        output.children.add(new TransformingQueryStructure("D", "D[]"));

        new ProjectionTestBase()
            .input(
                new QueryStructure("A[]", true)
                    .addChild(new QueryStructure("B[]", true))
                        .addChild(new QueryStructure("C[]", true))
                            .addChild(new QueryStructure("D", false))
                            .parent()
                        .parent()
                    .parent()
            )
            .output(output)
            .expectedTransformation("""
            root
                C.list
                T.list
                W.list
                C.map
                W.map(D[])
                C.list
                T.map(B[])
                T.list
                T.map(C[])
                T.list
                T.map(D)
                W.list
                C.leaf
            """)
            .data("""
            [ {
                "B[]": [ {
                    "C[]": [ { "D": "a1b1c1d" }, { "D": "a1b1c2d" } ]
                }, {
                    "C[]": [ { "D": "a1b2c1d" }, { "D": "a1b2c2d" } ]
                } ]
            }, {
                "B[]": [ {
                    "C[]": [ { "D": "a2b1c1d" }, { "D": "a2b1c2d" } ]
                }, {
                    "C[]": [ { "D": "a2b2c1d" }, { "D": "a2b2c2d" } ]
                } ]
            } ]
            """)
            .expectedData("""
            [ {
                "D[]": [ "a1b1c1d", "a1b1c2d", "a1b2c1d", "a1b2c2d" ]
            }, {
                "D[]": [ "a2b1c1d", "a2b1c2d", "a2b2c1d", "a2b2c2d" ]
            } ]
            """)
            .run();
    }

}
