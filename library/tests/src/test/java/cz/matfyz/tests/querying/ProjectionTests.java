package cz.matfyz.tests.querying;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.Signature.SignatureGenerator;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.querying.resolver.queryresult.TformingResultStructure;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProjectionTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectionTests.class);

    private final SignatureGenerator signatureGenerator = SignatureGenerator.create();

    private Signature signature() {
        // Doesn't matter what we use here, as long as it's unique.
        return signatureGenerator.next();
    }

    private static ResultStructure structure(String name, boolean isArray) {
        // Doesn't matter what we use here, as long as it's unique.
        final var variable = new Variable(name, false);
        return new ResultStructure(name, variable, isArray);
    }

    private static TformingResultStructure tforming(String inputName, String outputName) {
        // Doesn't matter what we use here, as long as it's unique.
        final var variable = new Variable(inputName, false);
        return new TformingResultStructure(inputName, outputName, variable);
    }

    @Test
    void onlyRootList() {
        new ProjectionTestBase()
            .input(
                structure("a-rray", true)
            )
            .output(
                tforming("a-rray", "a-rray")
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
        final var output = tforming("a-rray", "a-rray");
        output.children.add(tforming("B", "B"));

        new ProjectionTestBase()
            .input(
                structure("a-rray", true)
                    .addChild(structure("B", false), signature())
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
        final var output = tforming("a-rray", "c-rray");
        output.children.add(tforming("B", "D"));

        new ProjectionTestBase()
            .input(
                structure("a-rray", true)
                    .addChild(structure("B", false), signature())
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
        final var output = tforming("a-rray", "e-rray");
        final var b = tforming("b-rray", "f-rray");
        output.children.add(b);
        final var c = tforming("c-rray", "g-rray");
        b.children.add(c);
        c.children.add(tforming("D", "H"));

        new ProjectionTestBase()
            .input(
                structure("a-rray", true)
                .addChild(structure("b-rray", true), signature())
                    .addChild(structure("c-rray", true), signature())
                        .addChild(structure("D", false), signature())
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
                map.write(f-rray)
                list.create
                map.traverse(b-rray)
                list.traverse
                list.write
                map.create
                map.write(g-rray)
                list.create
                map.traverse(c-rray)
                list.traverse
                list.write
                map.create
                map.write(H)
                map.traverse(D)
                output.add
            """)
            .data("""
            [ {
                "b-rray": [ {
                    "c-rray": [ { "D": "a1b1c1d" }, { "D": "a1b1c2d" } ]
                }, {
                    "c-rray": [ { "D": "a1b2c1d" }, { "D": "a1b2c2d" } ]
                } ]
            }, {
                "b-rray": [ {
                    "c-rray": [ { "D": "a2b1c1d" }, { "D": "a2b1c2d" } ]
                }, {
                    "c-rray": [ { "D": "a2b2c1d" }, { "D": "a2b2c2d" } ]
                } ]
            } ]
            """)
            .expectedData("""
            [ {
                "f-rray": [ {
                    "g-rray": [ { "H": "a1b1c1d" }, { "H": "a1b1c2d" } ]
                }, {
                    "g-rray": [ { "H": "a1b2c1d" }, { "H": "a1b2c2d" } ]
                } ]
            }, {
                "f-rray": [ {
                    "g-rray": [ { "H": "a2b1c1d" }, { "H": "a2b1c2d" } ]
                }, {
                    "g-rray": [ { "H": "a2b2c1d" }, { "H": "a2b2c2d" } ]
                } ]
            } ]
            """)
            .run();
    }

    @Test
    void newRoot() {
        final var output = tforming("C", "c-rray");
        output.children.add(tforming("D", "D"));
        output.children.add(tforming("E", "E"));

        new ProjectionTestBase()
            .input(
                structure("a-rray", true)
                    .addChild(structure("b-rray", true), signature())
                        .addChild(structure("C", false), signature())
                        .parent()
                    .parent()
                    .addChild(structure("D", false), signature())
                    .parent()
                    .addChild(structure("E", false), signature())
                    .parent()
            )
            .output(output)
            .expectedTform("""
            root
                list.create
                list.traverse
                map.traverse(b-rray)
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
                "b-rray": [ { "C": "a1b1c1" }, { "C": "a1b1c2" } ],
                "D": "a1d",
                "E": "a1e"
            }, {
                "b-rray": [ { "C": "a2b1c1" }, { "C": "a2b1c2" } ],
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
        final var output = tforming("C", "c-rray");
        output.children.add(tforming("F", "f-rray"));

        new ProjectionTestBase()
            .input(
                structure("a-rray", true)
                    .addChild(structure("C", false), signature())
                    .parent()
                    .addChild(structure("d-rray", true), signature())
                        .addChild(structure("F", false), signature())
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
                map.write(f-rray)
                list.create
                parent.traverse
                map.traverse(d-rray)
                list.traverse
                map.traverse(F)
                list.write
                output.add
            """)
            .data("""
            [ {
                "C": "a1c",
                "d-rray": [ { "F": "a1d1f" }, { "F": "a1d2f" } ]
            }, {
                "C": "a2c",
                "d-rray": [ { "F": "a2d1f" }, { "F": "a2d2f" } ]
            } ]
            """)
            .expectedData("""
            [ {
                "f-rray": [ "a1d1f", "a1d2f" ]
            }, {
                "f-rray": [ "a2d1f", "a2d2f" ]
            } ]
            """)
            .run();
    }

    @Test
    void shortenList() {
        final var output = tforming("a-rray", "a-rray");
        final var b = tforming("b-rray", "b-rray");
        output.children.add(b);
        b.children.add(tforming("D", "d-rray"));

        new ProjectionTestBase()
            .input(
                structure("a-rray", true)
                    .addChild(structure("b-rray", true), signature())
                        .addChild(structure("c-rray", true), signature())
                            .addChild(structure("D", false), signature())
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
                map.write(b-rray)
                list.create
                map.traverse(b-rray)
                list.traverse
                list.write
                map.create
                map.write(d-rray)
                list.create
                map.traverse(c-rray)
                list.traverse
                map.traverse(D)
                list.write
                output.add
            """)
            .data("""
            [ {
                "b-rray": [ {
                    "c-rray": [ { "D": "a1b1c1d" }, { "D": "a1b1c2d" } ]
                }, {
                    "c-rray": [ { "D": "a1b2c1d" }, { "D": "a1b2c2d" } ]
                } ]
            }, {
                "b-rray": [ {
                    "c-rray": [ { "D": "a2b1c1d" }, { "D": "a2b1c2d" } ]
                }, {
                    "c-rray": [ { "D": "a2b2c1d" }, { "D": "a2b2c2d" } ]
                } ]
            } ]
            """)
            .expectedData("""
            [ {
                "b-rray": [ {
                    "d-rray": [ "a1b1c1d", "a1b1c2d" ]
                }, {
                    "d-rray": [ "a1b2c1d", "a1b2c2d" ]
                } ]
            }, {
                "b-rray": [ {
                    "d-rray": [ "a2b1c1d", "a2b1c2d" ]
                }, {
                    "d-rray": [ "a2b2c1d", "a2b2c2d" ]
                } ]
            } ]
            """)
            .run();
    }

    @Test
    void shortenListFromRoot() {
        final var output = tforming("a-rray", "a-rray");
        output.children.add(tforming("D", "d-rray"));

        new ProjectionTestBase()
            .input(
                structure("a-rray", true)
                    .addChild(structure("b-rray", true), signature())
                        .addChild(structure("c-rray", true), signature())
                            .addChild(structure("D", false), signature())
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
                map.write(d-rray)
                list.create
                map.traverse(b-rray)
                list.traverse
                map.traverse(c-rray)
                list.traverse
                map.traverse(D)
                list.write
                output.add
            """)
            .data("""
            [ {
                "b-rray": [ {
                    "c-rray": [ { "D": "a1b1c1d" }, { "D": "a1b1c2d" } ]
                }, {
                    "c-rray": [ { "D": "a1b2c1d" }, { "D": "a1b2c2d" } ]
                } ]
            }, {
                "b-rray": [ {
                    "c-rray": [ { "D": "a2b1c1d" }, { "D": "a2b1c2d" } ]
                }, {
                    "c-rray": [ { "D": "a2b2c1d" }, { "D": "a2b2c2d" } ]
                } ]
            } ]
            """)
            .expectedData("""
            [ {
                "d-rray": [ "a1b1c1d", "a1b1c2d", "a1b2c1d", "a1b2c2d" ]
            }, {
                "d-rray": [ "a2b1c1d", "a2b1c2d", "a2b2c1d", "a2b2c2d" ]
            } ]
            """)
            .run();
    }

}
