package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameFilterQuery;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.DocumentResponse;
import cz.matfyz.core.adminer.KindNamesResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;
import cz.matfyz.core.querying.QueryStructure;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.querying.queryresult.ResultLeaf;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.querying.queryresult.ResultMap;
import cz.matfyz.core.querying.queryresult.ResultNode;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import org.checkerframework.checker.nullness.qual.Nullable;

public class MongoDBPullWrapper implements AbstractPullWrapper {

    private MongoDBProvider provider;

    public MongoDBPullWrapper(MongoDBProvider provider) {
        this.provider = provider;
    }

    private MongoCursor<Document> getDocumentIterator(QueryContent query) {
        if (query instanceof final KindNameQuery kindNameQuery)
            return getDocumentIteratorFromKindName(kindNameQuery, null);

        if (query instanceof final KindNameFilterQuery kindNameFilterQuery)
            return getDocumentIteratorFromKindName(kindNameFilterQuery.kindNameQuery, kindNameFilterQuery.getFilters());

        if (query instanceof final MongoDBQuery mongoQuery)
            return provider.getDatabase().getCollection(mongoQuery.collection).aggregate(mongoQuery.pipeline).iterator();

        throw PullForestException.invalidQuery(this, query);
    }

    private MongoCursor<Document> getDocumentIteratorFromKindName(KindNameQuery query, @Nullable List<AdminerFilter> filters) {
        var find = provider.getDatabase().getCollection(query.kindName).find();
        if (filters != null && !filters.isEmpty())
            find = find.filter(createFilter(filters));
        if (query.hasOffset())
            find = find.skip(query.getOffset());
        if (query.hasLimit())
            find = find.limit(query.getLimit());

        return find.iterator();
    }

    private Map<DynamicName, DynamicNameReplacement> replacedNames;

    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        final var forest = new ForestOfRecords();
        replacedNames = path.copyWithoutDynamicNames().replacedNames();

        try (
            final var iterator = getDocumentIterator(query);
        ) {
            while (iterator.hasNext()) {
                final Document document = iterator.next();
                final var rootRecord = new RootRecord();
                addKeysToRecord(rootRecord, path, document);
                forest.addRecord(rootRecord);
            }

            return forest;
        }
        catch (Exception e) {
            throw PullForestException.innerException(e);
        }
    }

    private void addKeysToRecord(ComplexRecord record, ComplexProperty path, Document object) {
        for (final var entry : object.entrySet()) {
            final var key = entry.getKey();
            final var value = entry.getValue();
            if (value == null)
                continue;

            final var property = path.findSubpathByName(key);
            if (property == null)
                continue;

            if (!(property.name() instanceof final DynamicName dynamicName)) {
                addValueToRecord(record, property, value);
                continue;
            }

            // Replace the dynamically named property with an object containing both name and value properties.
            final var replacement = replacedNames.get(dynamicName);
            final var replacer = record.addDynamicReplacer(replacement.prefix(), replacement.name(), key);
            addValueToRecord(replacer, replacement.value(), value);
        }
    }

    private void addValueToRecord(ComplexRecord parentRecord, AccessPath property, Object value) {
        if (value instanceof final ArrayList<?> array) {
            // If it's array, we flatten it.
            for (int i = 0; i < array.size(); i++)
                addValueToRecord(parentRecord, property, array.get(i));
            return;
        }

        if (property instanceof final SimpleProperty simpleProperty) {
            // If it's a simple value, we add it to the record.
            parentRecord.addSimpleRecord(simpleProperty.signature(), value.toString());
            return;
        }

        final var complexProperty = (ComplexProperty) property;
        final var object = (Document) value;

        // If the path is an auxiliary property, we skip it and move all it's childrens' values to the parent node.
        // We do so by passing the parent record instead of creating a new one.
        final ComplexRecord childRecord = complexProperty.isAuxiliary()
            ? parentRecord
            : parentRecord.addComplexRecord(complexProperty.signature());

        addKeysToRecord(childRecord, complexProperty, object);
    }

    public String readCollectionAsStringForTests(String kindName) {
        final var database = provider.getDatabase();
        final MongoCollection<Document> collection = database.getCollection(kindName);
        final Iterator<Document> iterator = collection.find().iterator();

        final var output = new StringBuilder();
        while (iterator.hasNext())
            output.append(iterator.next().toString());

        return output.toString();
    }

    @Override public QueryResult executeQuery(QueryStatement query) {
        final var output = new ArrayList<ResultMap>();

        try (
            var iterator = getDocumentIterator(query.content());
        ) {
            while (iterator.hasNext()) {
                final Document document = iterator.next();
                output.add(getResultFromDocument(document, query.structure()));
            }

            final var dataResult = new ResultList(output);

            return new QueryResult(dataResult, query.structure());
        }
        catch (Exception e) {
            throw PullForestException.innerException(e);
        }
    }

    private ResultMap getResultFromDocument(Document document, QueryStructure structure) {
        final var output = new TreeMap<String, ResultNode>();

        for (final var child : structure.children())
            output.put(child.name, getResultFromChild(document, child));

        return new ResultMap(output);
    }

    private ResultNode getResultFromChild(Document document, QueryStructure child) {
        if (child.isLeaf()) {
            // This child is a leaf - it's value has to be either a string or an array of strings.
            if (child.isArray) {
                final List<ResultLeaf> childList = ((ArrayList<String>) document.get(child.name))
                    .stream()
                    .map(childString -> new ResultLeaf(childString))
                    .toList();

                return new ResultList(childList);
            }
            else {
                final var childString = document.get(child.name, String.class);

                return new ResultLeaf(childString);
            }
        }
        else {
            if (child.isArray) {
                // An array of arrays is not supported yet.
                final List<ResultMap> childList = ((ArrayList<Document>) document.get(child.name))
                    .stream()
                    .map(childDocument -> getResultFromDocument(childDocument, child))
                    .toList();

                return new ResultList(childList);
            }
            else {
                final var childDocument = document.get(child.name, Document.class);

                return getResultFromDocument(childDocument, child);
            }
        }
    }

    /**
     * Retrieves a list of kind names with support for pagination.
     *
     * @throws PullForestException if an error occurs while querying the database.
     */
    @Override public KindNamesResponse getKindNames(String limit, String offsetString) {
        try {
            MongoIterable<String> kindNames = provider.getDatabase().listCollectionNames();
            List<String> data = new ArrayList<>();

            int lim = Integer.parseInt(limit);
            int offset = Integer.parseInt(offsetString);

            int index = 0;
            int count = 0;

            for (String kindName : kindNames) {
                if (index >= offset && count < lim) {
                    data.add(kindName);
                    count++;
                }

                index++;
            }

            return new KindNamesResponse(data);
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
    }

    /**
     * Creates a MongoDB filter based on a list of filters.
     *
     * @throws UnsupportedOperationException if an unsupported operator is encountered.
     */
    private Bson createFilter(List<AdminerFilter> filters) {
        List<Bson> filterList = new ArrayList<>();

        for (AdminerFilter filter : filters) {
            var columnName = filter.propertyName();
            var operator = filter.operator();
            var columnValue = filter.propertyValue();

            Object value;
            if ("_id".equals(columnName)) {
                value = new ObjectId(columnValue);
            } else if (BOOLEAN_PATTERN.matcher(columnValue).matches()) {
                value = Boolean.parseBoolean(columnValue);
            } else if (NUMBER_PATTERN.matcher(columnValue).matches()) {
                value = columnValue.contains(".")
                    ? Double.parseDouble(columnValue)
                    : Long.parseLong(columnValue);
            } else {
                value = columnValue;
            }

            BiFunction<String, Object, Bson> filterFunction = OPERATORS.get(operator);
            filterList.add(filterFunction.apply(columnName, value));
        }

        return Filters.and(filterList);
    }

    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(true|false)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");

    /**
     * Retrieves documents from a collection based on kind name, pagination parameters and optional filters.
     *
     * @throws PullForestException if an error occurs while querying the database.
     */
    @Override public DocumentResponse getKind(String kindName, String limit, String offset, @Nullable List<AdminerFilter> filters) {
        KindNameQuery kindNameQuery = new KindNameQuery(kindName, Integer.parseInt(limit), Integer.parseInt(offset));

        if (filters == null){
            return getQueryResult(kindNameQuery);
        }

        return getQueryResult(new KindNameFilterQuery(kindNameQuery, filters));
    }

    /**
     * Retrieves a list of references for a specified kind.
     */
    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        // No foreign keys can be fetched right from MongoDB
        return new ArrayList<>();
    }

    /**
     * Retrieves the result of the given query.
     */
    @Override public DocumentResponse getQueryResult(QueryContent query) {
        try {
            if (query instanceof final StringQuery stringQuery){
                Document parsedQuery = Document.parse(stringQuery.content);
                Document result = provider.getDatabase().runCommand(parsedQuery);

                Document cursor = (Document) result.get("cursor");
                List<Document> documents = (List<Document>) cursor.get("firstBatch");
                long itemCount = documents.size();

                List<String> propertyNames = new ArrayList<>();
                if (parsedQuery.containsKey("find")) {
                    String kindName = parsedQuery.getString("find");
                    MongoCollection<Document> collection = provider.getDatabase().getCollection(kindName);
                    propertyNames = MongoDBUtils.getPropertyNames(collection);
                }

                return new DocumentResponse(documents, itemCount, propertyNames);
            }

            List<Document> data = new ArrayList<>();
            MongoCursor<Document> iterator = getDocumentIterator(query);
            long itemCount = 0;

            while (iterator.hasNext()) {
                data.add(iterator.next());

                itemCount++;
            }

            List<String> propertyNames;
            if (query instanceof final KindNameQuery knQuery) {
                MongoCollection<Document> collection = provider.getDatabase().getCollection(knQuery.kindName);
                propertyNames = MongoDBUtils.getPropertyNames(collection);
            }
            else if (query instanceof final KindNameFilterQuery knfQuery) {
                MongoCollection<Document> collection = provider.getDatabase().getCollection(knfQuery.kindNameQuery.kindName);
                propertyNames = MongoDBUtils.getPropertyNames(collection);
            }
            else
                throw PullForestException.invalidQuery(this, query);

            return new DocumentResponse(data, itemCount, propertyNames);
        }
        catch (Exception e){
            throw PullForestException.innerException(e);
        }
    }

    /**
     * Parses a string into a list of strings.
     *
     * @throws InvalidParameterException if the input is not properly enclosed.
     */
    private static List<String> parseStringToList(String value) {
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .map(str -> str.substring(1, str.length() - 1))
            .toList();
    }

    /**
     * Defines a mapping between operator names and their corresponding MongoDB filter functions.
     */
    private static Map<String, BiFunction<String, Object, Bson>> defineOperators() {
        final var ops = new TreeMap<String, BiFunction<String, Object, Bson>>();

        ops.put("Equal", Filters::eq);
        ops.put("NotEqual", Filters::ne);
        ops.put("Less", Filters::lt);
        ops.put("LessOrEqual", Filters::lte);
        ops.put("Greater", Filters::gt);
        ops.put("GreaterOrEqual", Filters::gte);

        ops.put("In", (column, value) -> Filters.in(column, parseStringToList((String) value)));
        ops.put("NotIn", (column, value) -> Filters.nin(column, parseStringToList((String) value)));

        ops.put("MatchRegEx", (column, value) -> Filters.regex(column, (String) value));

        return ops;
    }

    /**
     * A map of operator names to MongoDB filter functions.
     */
    private static final Map<String, BiFunction<String, Object, Bson>> OPERATORS = defineOperators();

}
