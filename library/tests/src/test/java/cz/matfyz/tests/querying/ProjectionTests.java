package cz.matfyz.tests.querying;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.querying.resolver.queryresult.TformingResultStructure;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProjectionTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectionTests.class);

    // TODO fix the default values in the result structures. They should not be needed for this test, however.
    private static final Signature signature = Signature.empty();

    @Test
    void onlyRootList() {
        new ProjectionTestBase()
            .input(
                new ResultStructure("A[]", true, null)
            )
            .output(
                new TformingResultStructure("A[]", "A[]", null)
            )
            .expectedTform("""
            root
                list.create
                list.traverse
                list.write
                output.add
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
        final var output = new TformingResultStructure("A[]", "A[]", null);
        output.children.add(new TformingResultStructure("B", "B", null));

        new ProjectionTestBase()
            .input(
                new ResultStructure("A[]", true, null)
                    .addChild(new ResultStructure("B", false, null), signature)
                    .parent()
            )
            .output(output)
            .expectedTform("""
            root
                list.create
                list.traverse
                list.write
                map.create
                map.write(B)
                map.traverse(B)
                output.add
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
        final var output = new TformingResultStructure("A[]", "C[]", null);
        output.children.add(new TformingResultStructure("B", "D", null));

        new ProjectionTestBase()
            .input(
                new ResultStructure("A[]", true, null)
                    .addChild(new ResultStructure("B", false, null), signature)
                    .parent()
            )
            .output(output)
            .expectedTform("""
            root
                list.create
                list.traverse
                list.write
                map.create
                map.write(D)
                map.traverse(B)
                output.add
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
        final var output = new TformingResultStructure("A[]", "E[]", null);
        final var b = new TformingResultStructure("B[]", "F[]", null);
        output.children.add(b);
        final var c = new TformingResultStructure("C[]", "G[]", null);
        b.children.add(c);
        c.children.add(new TformingResultStructure("D", "H", null));

        new ProjectionTestBase()
            .input(
                new ResultStructure("A[]", true, null)
                .addChild(new ResultStructure("B[]", true, null), signature)
                    .addChild(new ResultStructure("C[]", true, null), signature)
                        .addChild(new ResultStructure("D", false, null), signature)
                        .parent()
                    .parent()
                .parent()
            )
            .output(output)
            .expectedTform("""
            root
                list.create
                list.traverse
                list.write
                map.create
                map.write(F[])
                list.create
                map.traverse(B[])
                list.traverse
                list.write
                map.create
                map.write(G[])
                list.create
                map.traverse(C[])
                list.traverse
                list.write
                map.create
                map.write(H)
                map.traverse(D)
                output.add
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
        final var output = new TformingResultStructure("C", "C[]", null);
        output.children.add(new TformingResultStructure("D", "D", null));
        output.children.add(new TformingResultStructure("E", "E", null));

        new ProjectionTestBase()
            .input(
                new ResultStructure("A[]", true, null)
                    .addChild(new ResultStructure("B[]", true, null), signature)
                        .addChild(new ResultStructure("C", false, null), signature)
                        .parent()
                    .parent()
                    .addChild(new ResultStructure("D", false, null), signature)
                    .parent()
                    .addChild(new ResultStructure("E", false, null), signature)
                    .parent()
            )
            .output(output)
            .expectedTform("""
            root
                list.create
                list.traverse
                map.traverse(B[])
                list.traverse
                map.traverse(C)
                list.write
                map.create:
                --- map.write(D)
                    parent.traverse
                    parent.traverse
                    parent.traverse
                    map.traverse(D)
                    output.add

                --- map.write(E)
                    parent.traverse
                    parent.traverse
                    parent.traverse
                    map.traverse(E)
                    output.add
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
        final var output = new TformingResultStructure("C", "C[]", null);
        output.children.add(new TformingResultStructure("F", "F[]", null));

        new ProjectionTestBase()
            .input(
                new ResultStructure("A[]", true, null)
                    .addChild(new ResultStructure("C", false, null), signature)
                    .parent()
                    .addChild(new ResultStructure("D[]", true, null), signature)
                        .addChild(new ResultStructure("F", false, null), signature)
                        .parent()
                    .parent()
            )
            .output(output)
            .expectedTform("""
            root
                list.create
                list.traverse
                map.traverse(C)
                list.write
                map.create
                map.write(F[])
                list.create
                parent.traverse
                map.traverse(D[])
                list.traverse
                map.traverse(F)
                list.write
                output.add
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
        final var output = new TformingResultStructure("A[]", "A[]", null);
        final var b = new TformingResultStructure("B[]", "B[]", null);
        output.children.add(b);
        b.children.add(new TformingResultStructure("D", "D[]", null));

        new ProjectionTestBase()
            .input(
                new ResultStructure("A[]", true, null)
                    .addChild(new ResultStructure("B[]", true, null), signature)
                        .addChild(new ResultStructure("C[]", true, null), signature)
                            .addChild(new ResultStructure("D", false, null), signature)
                            .parent()
                        .parent()
                    .parent()
            )
            .output(output)
            .expectedTform("""
            root
                list.create
                list.traverse
                list.write
                map.create
                map.write(B[])
                list.create
                map.traverse(B[])
                list.traverse
                list.write
                map.create
                map.write(D[])
                list.create
                map.traverse(C[])
                list.traverse
                map.traverse(D)
                list.write
                output.add
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
        final var output = new TformingResultStructure("A[]", "A[]", null);
        output.children.add(new TformingResultStructure("D", "D[]", null));

        new ProjectionTestBase()
            .input(
                new ResultStructure("A[]", true, null)
                    .addChild(new ResultStructure("B[]", true, null), signature)
                        .addChild(new ResultStructure("C[]", true, null), signature)
                            .addChild(new ResultStructure("D", false, null), signature)
                            .parent()
                        .parent()
                    .parent()
            )
            .output(output)
            .expectedTform("""
            root
                list.create
                list.traverse
                list.write
                map.create
                map.write(D[])
                list.create
                map.traverse(B[])
                list.traverse
                map.traverse(C[])
                list.traverse
                map.traverse(D)
                list.write
                output.add
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
