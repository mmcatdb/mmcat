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
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Name.TypedName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.querying.LeafResult;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.MapResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.querying.ResultNode;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ComplexRecord.ArrayCollector;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

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
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MongoDBPullWrapper implements AbstractPullWrapper {

    private final MongoDBProvider provider;

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

    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        final var forest = new ForestOfRecords();

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
            throw PullForestException.inner(e);
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

            if (property.name() instanceof DynamicName) {
                final var dynamicRecord = record.addDynamicRecord(property, key);
                final var valueProperty = ((ComplexProperty) property).getTypedSubpath(TypedName.VALUE);
                addValueToRecord(dynamicRecord, valueProperty, value);
                continue;
            }

            addValueToRecord(record, property, value);
        }
    }

    private void addValueToRecord(ComplexRecord parentRecord, AccessPath property, Object value) {
        if (property.signature().hasDual())
            addArrayToRecord(parentRecord, property, (List<?>) value);
        else
            addScalarValueToRecord(parentRecord, property, value);
    }

    private void addScalarValueToRecord(ComplexRecord parentRecord, AccessPath property, Object value) {
        if (property instanceof final SimpleProperty simpleProperty) {
            // If it's a simple value, we add it to the record.
            parentRecord.addSimpleRecord(simpleProperty.signature(), value.toString());
            return;
        }

        final ComplexRecord childRecord = parentRecord.addComplexRecord(property.signature());
        addKeysToRecord(childRecord, (ComplexProperty) property, (Document) value);
    }

    private void addArrayToRecord(ComplexRecord parentRecord, AccessPath property, List<?> array) {
        if (!(property instanceof final ComplexProperty complexProperty) || complexProperty.getIndexSubpaths().isEmpty()) {
            for (int i = 0; i < array.size(); i++)
                addScalarValueToRecord(parentRecord, property, array.get(i));
            return;
        }

        final var collector = new ArrayCollector(parentRecord, complexProperty);
        processArrayDimension(collector, array);
    }

    private void processArrayDimension(ArrayCollector collector, List<?> array) {
        final var isValueDimension = collector.nextDimension();
        for (int i = 0; i < array.size(); i++) {
            collector.setIndex(i);
            if (isValueDimension)
                addValueToRecord(collector.addIndexedRecord(), collector.valueSubpath, array.get(i));
            else
                processArrayDimension(collector, (ArrayList<?>) array.get(i));
        }
        collector.prevDimension();
    }

    public String readCollectionAsStringForTests(String kindName) {
        final var database = provider.getDatabase();
        final MongoCollection<Document> collection = database.getCollection(kindName);
        final Iterator<Document> iterator = collection.find().iterator();

        final var sb = new StringBuilder();
        while (iterator.hasNext())
            sb.append(iterator.next().toString());

        return sb.toString();
    }

    // #region Querying

    @Override public QueryResult executeQuery(QueryStatement query) {
        final var output = new ArrayList<MapResult>();

        try (
            var iterator = getDocumentIterator(query.content());
        ) {
            while (iterator.hasNext()) {
                final Document document = iterator.next();
                output.add(getResultFromDocument(document, query.structure()));
            }

            final var dataResult = new ListResult(output);

            return new QueryResult(dataResult, query.structure());
        }
        catch (Exception e) {
            throw PullForestException.inner(e);
        }
    }

    private MapResult getResultFromDocument(Document document, ResultStructure structure) {
        final var output = new TreeMap<String, ResultNode>();

        for (final var child : structure.children())
            output.put(child.name, getResultFromChild(document, child));

        return new MapResult(output);
    }

    private ResultNode getResultFromChild(Document document, ResultStructure child) {
        if (child.isLeaf()) {
            // This child is a leaf - it's value has to be either a string or an array of strings.
            if (child.isArray) {
                final List<LeafResult> childList = ((List<String>) document.get(child.name))
                    .stream()
                    .map(childString -> new LeafResult(childString))
                    .toList();

                return new ListResult(childList);
            }
            else {
                final var childString = document.get(child.name, String.class);

                return new LeafResult(childString);
            }
        }
        else {
            if (child.isArray) {
                // An array of arrays is not supported yet.
                final List<MapResult> childList = ((List<Document>) document.get(child.name))
                    .stream()
                    .map(childDocument -> getResultFromDocument(childDocument, child))
                    .toList();

                return new ListResult(childList);
            }
            else {
                final var childDocument = document.get(child.name, Document.class);

                return getResultFromDocument(childDocument, child);
            }
        }
    }

    @Override public List<String> getKindNames() {
        return provider.getDatabase().listCollectionNames().into(new ArrayList<>());
    }

    /**
     * Creates a MongoDB filter based on a list of filters.
     */
    private Bson createFilter(List<AdminerFilter> filters) {
        List<Bson> filterList = new ArrayList<>();

        for (AdminerFilter filter : filters) {
            var columnName = filter.propertyName();
            var operator = filter.operator();
            var columnValue = filter.propertyValue();

            Object value;
            if ("_id".equals(columnName))
                value = new ObjectId(columnValue);
            else if (BOOLEAN_PATTERN.matcher(columnValue).matches())
                value = Boolean.parseBoolean(columnValue);
            else if (NUMBER_PATTERN.matcher(columnValue).matches())
                value = columnValue.contains(".") ? Double.parseDouble(columnValue) : Long.parseLong(columnValue);
            else
                value = columnValue;

            BiFunction<String, Object, Bson> filterFunction = OPERATORS.get(operator);
            filterList.add(filterFunction.apply(columnName, value));
        }

        return Filters.and(filterList);
    }

    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(true|false)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");

    @Override public DocumentResponse getRecords(String kindName, @Nullable Integer limit, @Nullable Integer offset, @Nullable List<AdminerFilter> filters) {
        KindNameQuery kindNameQuery = new KindNameQuery(kindName, limit, offset);
        if (filters == null)
            return getQueryResult(kindNameQuery);

        return getQueryResult(new KindNameFilterQuery(kindNameQuery, filters));
    }

    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        // No foreign keys can be fetched right from MongoDB
        return new ArrayList<>();
    }

    @Override public DocumentResponse getQueryResult(QueryContent query) {
        try {
            if (query instanceof final StringQuery stringQuery) {
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
        catch (Exception e) {
            throw PullForestException.inner(e);
        }
    }

    /**
     * A map of operator names to MongoDB filter functions.
     */
    private static final Map<String, BiFunction<String, Object, Bson>> OPERATORS = defineOperators();

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
     * Parses a string into a list of strings.
     */
    private static List<String> parseStringToList(String value) {
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .map(str -> str.substring(1, str.length() - 1))
            .toList();
    }

    // #endregion

}
