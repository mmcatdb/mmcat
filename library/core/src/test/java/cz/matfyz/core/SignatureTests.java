package cz.matfyz.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cz.matfyz.core.identifiers.Signature;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author jachymb.bartik
 */
class SignatureTests {

    static Stream<Arguments> traverseThroughSucceedsArguments() {
        return Stream.of(
            Arguments.of("1.2.3.4", "-2.-1", "3.4"),
            Arguments.of("1.2.3.4", "EMPTY", "1.2.3.4"),
            Arguments.of("EMPTY", "EMPTY", "EMPTY")
        );
    }

    @ParameterizedTest
    @MethodSource("traverseThroughSucceedsArguments")
    void traverseThroughSucceeds(String originalString, String pathString, String expectedString) {
        final var original = Signature.fromString(originalString);
        final var path = Signature.fromString(pathString);
        final var expected = Signature.fromString(expectedString);

        final var result = original.traverseThrough(path);

        assertEquals(expected, result);
    }

    static Stream<Arguments> traverseThroughFailsArguments() {
        return Stream.of(
            Arguments.of("1.2.3.4", "-1.-2"),
            Arguments.of("1.2", "-3.-2.-1")
        );
    }

    @ParameterizedTest
    @MethodSource("traverseThroughFailsArguments")
    void traverseThroughFails(String originalString, String pathString) {
        final var original = Signature.fromString(originalString);
        final var path = Signature.fromString(pathString);

        final var result = original.traverseThrough(path);

        assertNull(result);
    }

    static Stream<Arguments> traverseAlongSucceedsArguments() {
        return Stream.of(
            Arguments.of("1.2.3.4", "1.2", "3.4"),
            Arguments.of("1.2.3.4", "EMPTY", "1.2.3.4"),
            Arguments.of("EMPTY", "1.2.3.4", "-4.-3.-2.-1"),
            Arguments.of("EMPTY", "EMPTY", "EMPTY"),
            Arguments.of("1.2", "1.2.3.4", "-4.-3"),
            Arguments.of("1.2", "-1", "1.1.2")
        );
    }

    @ParameterizedTest
    @MethodSource("traverseAlongSucceedsArguments")
    void traverseAlongSucceeds(String originalString, String pathString, String expectedString) {
        final var original = Signature.fromString(originalString);
        final var path = Signature.fromString(pathString);
        final var expected = Signature.fromString(expectedString);

        final var result = original.traverseAlong(path);

        assertEquals(expected, result);
    }

}
