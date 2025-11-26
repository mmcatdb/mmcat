package cz.matfyz.tests.querying;

import cz.matfyz.core.identifiers.Signature;

import java.util.List;
import java.util.stream.Stream;

public class FilterQueryFiller {
    private final FilterValueGenerator valueGenerator;

    public FilterQueryFiller(FilterValueGenerator valueGenerator) {
        this.valueGenerator = valueGenerator;
    }

    public static record SignatureVariablePair(Signature signature, String varName) { }

    public String fillQuery(String queryString) {

        final String whereStart = "WHERE {";
        final var fromIdx = queryString.indexOf(whereStart) + whereStart.length();
        final var filtersIdx = queryString.indexOf("FILTER", fromIdx);
        final var toIdx = queryString.indexOf("}", fromIdx);

        if (filtersIdx == -1) return queryString; // no filters, OK

        final var whereClauses = queryString.substring(fromIdx, filtersIdx);
        final List<SignatureVariablePair> whereClausePairs = Stream.of(whereClauses.split("\\.|;"))
            .map(clause -> clause.trim().split(" "))
            .filter(clause -> clause.length > 1)
            .map(elts -> new SignatureVariablePair(
                Signature.fromString(elts[elts.length - 2], "/"), elts[elts.length - 1]
            )).toList();

        final var filters = queryString.substring(filtersIdx, toIdx);

        final var result = new StringBuilder(queryString.substring(0, filtersIdx));

        for (final var fstring : filters.split("FILTER\\(")) {
            final var closeIdx = fstring.indexOf(")");
            if (closeIdx == -1) continue;
            final var innerFilter = fstring.substring(0, closeIdx).trim();
            if (innerFilter.length() == 0) continue;

            final var elts = innerFilter.split(" ");
            final var variable = elts[0];
            final var operator = elts[1];
            // final var constant = elts[2];

            var found = false;
            for (final var pair : whereClausePairs) {
                if (pair.varName.equals(variable)) {
                    found = true;
                    valueGenerator.registerForQueriedValue(pair.signature);
                    result.append("    FILTER(")
                        .append(variable).append(" ").append(operator).append(" \"")
                        .append(valueGenerator.generateValue(pair.signature))
                        .append("\")\n");
                    break;
                }
            }
            if (!found) throw new RuntimeException("Could not find filtered variable (malformed query?)");
        }

        return result.append("}").toString();
    }
}
