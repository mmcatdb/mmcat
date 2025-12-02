package cz.matfyz.tests.querying;

import cz.matfyz.core.identifiers.Signature;

import java.util.List;
import java.util.stream.Stream;

public class FilterQueryFiller {
    // While we could identify variable domains with objexes, signatures are easier to use when creating a query to obtain existing values in the DB; also, scalar values should only have one signature connected to them anyway, so there is no problem with non-1:1 mapping and such

    public static record ParametrizedQuery(FilterValueGenerator generator, List<String> queryParts, List<Signature> variableDomains) {
        // The spaces between query parts will be joined by values from variableDomains
        // ergo queryParts.size() == variableDomains.size() + 1

        public String generateQuery(String... variableValues) {
            if (queryParts.size() == 1) return queryParts.get(0);

            assert variableValues.length == queryParts.size() - 1;

            final var sb = new StringBuilder();
            for (int i = 0; i < variableValues.length; i++) {
                sb.append(queryParts.get(i));
                sb.append(variableValues[i]);
            }
            sb.append(queryParts.get(queryParts.size() - 1));
            return sb.toString();
        }

        public String generateQuery() {
            final var values = new String[variableDomains.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = generator.generateValue(variableDomains.get(i));
            }
            return generateQuery(values);
        }
    }



    private final FilterValueGenerator valueGenerator;

    public FilterQueryFiller(FilterValueGenerator valueGenerator) {
        this.valueGenerator = valueGenerator;
    }



    public ParametrizedQuery fillQuery(String queryString) {
        final var varIndicator = "#";

        if (queryString.indexOf(varIndicator) == -1) return new ParametrizedQuery(valueGenerator, List.of(queryString), List.of()); // nothing to parametrize

        final var splitQuery = queryString.split(varIndicator);

        final var queryPartArray = new String[splitQuery.length];
        queryPartArray[0] = splitQuery[0];
        for (int i = 1; i < queryPartArray.length; i++) {
            queryPartArray[i] = splitQuery[i].substring(splitQuery[i].indexOf('"'));
        }

        final var queryParts = List.of(queryPartArray);
        final var variableDomains = Stream.of(splitQuery).skip(1).map(part -> Signature.fromString(part.substring(0, part.indexOf('"')), "/")).toList();

        for (final var sig : variableDomains) valueGenerator.ensureValuesForSignature(sig);

        return new ParametrizedQuery(valueGenerator, queryParts, variableDomains);
    }
}
