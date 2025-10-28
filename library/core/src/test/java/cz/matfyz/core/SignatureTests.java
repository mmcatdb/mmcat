package cz.matfyz.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.matfyz.core.identifiers.Signature;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SignatureTests {

    static Stream<Arguments> traverseArguments() {
        return Stream.of(
            Arguments.of("-2.-1", "1.2.3.4", "3.4"),
            Arguments.of("EMPTY", "1.2.3.4", "1.2.3.4"),
            Arguments.of("EMPTY", "EMPTY", "EMPTY"),
            Arguments.of("-1.-2", "1.2.3.4", "-1.-2.1.2.3.4"),
            Arguments.of("-4.-2.-1", "1.2.3", "-4.3")
        );
    }

    @ParameterizedTest
    @MethodSource("traverseArguments")
    void traverseTests(String originalString, String pathString, String expectedString) {
        final var original = Signature.fromString(originalString);
        final var path = Signature.fromString(pathString);
        final var expected = Signature.fromString(expectedString);

        final var result = original.traverse(path);

        assertEquals(expected, result);
    }

    static Stream<Arguments> traverseBackArguments() {
        return Stream.of(
            Arguments.of("1.2", "1.2.3.4", "3.4"),
            Arguments.of("EMPTY", "1.2.3.4", "1.2.3.4"),
            Arguments.of("1.2.3.4", "EMPTY", "-4.-3.-2.-1"),
            Arguments.of("EMPTY", "EMPTY", "EMPTY"),
            Arguments.of("1.2.3.4", "1.2", "-4.-3"),
            Arguments.of("-1", "1.2", "1.1.2"),
            Arguments.of("3.4.1.2", "1.2", "-2.-1.-4.-3.1.2")
        );
    }

    @ParameterizedTest
    @MethodSource("traverseBackArguments")
    void traverseBackTest(String originalString, String pathString, String expectedString) {
        final var original = Signature.fromString(originalString);
        final var path = Signature.fromString(pathString);
        final var expected = Signature.fromString(expectedString);

        final var result = original.traverseBack(path);

        assertEquals(expected, result);
    }

}
