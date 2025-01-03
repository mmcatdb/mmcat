package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.adminer.DocumentResponse;
import cz.matfyz.core.adminer.KindNameResponse;
import cz.matfyz.core.adminer.ForeignKey;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.Name;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.querying.QueryStructure;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.querying.queryresult.ResultLeaf;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.querying.queryresult.ResultMap;
import cz.matfyz.core.querying.queryresult.ResultNode;
import cz.matfyz.core.record.AdminerFilter;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RecordName;
import cz.matfyz.core.record.RootRecord;
import cz.matfyz.inference.adminer.MongoDBAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;

import com.mongodb.client.FindIterable;
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
            return getDocumentIteratorFromKindName(kindNameQuery);

        if (query instanceof final MongoDBQuery mongoQuery)
            return provider.getDatabase().getCollection(mongoQuery.collection).aggregate(mongoQuery.pipeline).iterator();

        throw PullForestException.invalidQuery(this, query);
    }

    private MongoCursor<Document> getDocumentIteratorFromKindName(KindNameQuery query) {
        var find = provider.getDatabase().getCollection(query.kindName).find();
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
                getDataFromDocument(rootRecord, document, path);
                forest.addRecord(rootRecord);
            }

            return forest;
        }
        catch (Exception e) {
            throw PullForestException.innerException(e);
        }
    }

    private void getDataFromDocument(ComplexRecord parentRecord, Document document, ComplexProperty path) {
        boolean hasSubpathWithDynamicName = false;

        for (final AccessPath subpath : path.subpaths()) {
            if (subpath.name() instanceof StaticName staticName)
                getFieldFromObjectForSubpath(parentRecord, document, staticName.getStringName(), subpath);
            else
                hasSubpathWithDynamicName = true;
        }

        if (hasSubpathWithDynamicName)
            getDataFromDynamicFieldsOfObject(parentRecord, document, path);
    }

    private void getDataFromDynamicFieldsOfObject(ComplexRecord parentRecord, Document document, ComplexProperty path) {
        // First we find all names that belong to the subpaths with non-dynamic names and also the subpath with the dynamic name.
        AccessPath subpathWithDynamicName = null;
        final Set<String> otherSubpathNames = new TreeSet<>();

        for (AccessPath subpath : path.subpaths()) {
            if (subpath.name() instanceof StaticName staticName)
                otherSubpathNames.add(staticName.getStringName());
            else
                subpathWithDynamicName = subpath;
        }

        // For all keys in the object where the key is not a known static name do ...
        for (final String key : document.keySet()) {
            if (!otherSubpathNames.contains(key))
                getFieldFromObjectForSubpath(parentRecord, document, key, subpathWithDynamicName);
        }
    }

    private void getFieldFromObjectForSubpath(ComplexRecord parentRecord, Document document, String key, AccessPath subpath) {
        final var value = document.get(key);
        if (value == null)
            return;

        if (subpath instanceof final ComplexProperty complexSubpath)
            getFieldFromObjectForComplexSubpath(parentRecord, key, value, complexSubpath);
        else if (subpath instanceof final SimpleProperty simpleSubpath)
            getFieldFromObjectForSimpleSubpath(parentRecord, key, value, simpleSubpath);
    }

    private void getFieldFromObjectForComplexSubpath(ComplexRecord parentRecord, String key, Object value, ComplexProperty complexSubpath) {
        if (value instanceof final ArrayList<?> childArray) {
            for (int i = 0; i < childArray.size(); i++)
                if (childArray.get(i) instanceof Document childDocument)
                    addComplexValueToRecord(parentRecord, childDocument, key, complexSubpath);
        }
        else if (value instanceof Document childDocument)
            addComplexValueToRecord(parentRecord, childDocument, key, complexSubpath);
    }

    private void getFieldFromObjectForSimpleSubpath(ComplexRecord parentRecord, String key, Object value, SimpleProperty simpleSubpath) {
        if (value instanceof final ArrayList<?> simpleArray) {
            final var values = new ArrayList<String>();

            for (int i = 0; i < simpleArray.size(); i++)
                values.add(simpleArray.get(i).toString());

            parentRecord.addSimpleArrayRecord(toRecordName(simpleSubpath.name(), key), simpleSubpath.signature(), values);
        }
        else {
            final RecordName recordName = toRecordName(simpleSubpath.name(), key);
            parentRecord.addSimpleValueRecord(recordName, simpleSubpath.signature(), value.toString());
        }
    }

    private void addComplexValueToRecord(ComplexRecord parentRecord, Document value, String key, ComplexProperty complexProperty) {
        // If the path is an auxiliary property, we skip it and move all it's childrens' values to the parent node.
        // We do so by passing the parent record instead of creating a new one.
        if (complexProperty.isAuxiliary())
            getDataFromDocument(parentRecord, value, complexProperty);
        else {
            ComplexRecord childRecord = parentRecord.addComplexRecord(toRecordName(complexProperty.name(), key), complexProperty.signature());
            getDataFromDocument(childRecord, value, complexProperty);
        }
    }

    private RecordName toRecordName(Name name, String valueIfDynamic) {
        if (name instanceof DynamicName dynamicName)
            return dynamicName.toRecordName(valueIfDynamic);

        return ((StaticName) name).toRecordName();
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
     * @param limit The maximum number of results to return.
     * @param offsetString The number of results to skip.
     * @return A {@link KindNameResponse} containing the list of collection names.
     * @throws PullForestException if an error occurs while querying the database.
     */
    @Override public KindNameResponse getKindNames(String limit, String offsetString) {
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

            return new KindNameResponse(data);
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
    }

    /**
     * Creates a MongoDB filter based on a list of filters.
     *
     * @param filters The list of filters to apply.
     * @return A {@link Bson} filter object.
     * @throws UnsupportedOperationException if an unsupported operator is encountered.
     */
    private Bson createFilter(List<AdminerFilter> filters) {
        List<Bson> filterList = new ArrayList<>();

        for (AdminerFilter filter : filters) {
            var columnName = filter.columnName();
            var operator = filter.operator();
            var columnValue = filter.columnValue();
            var value = "_id".equals(columnName) ? new ObjectId(columnValue) : columnValue;

            BiFunction<String, Object, Bson> filterFunction = MongoDBAlgorithms.OPERATORS.get(operator);
            filterList.add(filterFunction.apply(columnName, value));
        }

        return Filters.and(filterList);
    }

    /**
     * Retrieves documents from a collection based on filters, pagination parameters and kind name.
     *
     * @param kindName The name of the kind to query.
     * @param limit The maximum number of results to return.
     * @param offsetString The number of results to skip.
     * @param filters The list of filters to apply (optional).
     * @return A {@link DocumentResponse} containing the retrieved documents, item count, and property names.
     * @throws PullForestException if an error occurs while querying the database.
     */
    @Override public DocumentResponse getKind(String kindName, String limit, String offsetString, @Nullable List<AdminerFilter> filters){
        try {
            List<Map<String, Object>> data = new ArrayList<>();
            MongoCollection<Document> collection = provider.getDatabase().getCollection(kindName);
            FindIterable<Document> documents = filters == null ? collection.find() : collection.find(createFilter(filters));

            int lim = Integer.parseInt(limit);
            int offset = Integer.parseInt(offsetString);

            int itemCount = 0;
            int count = 0;

            for (Document document : documents) {
                if (itemCount >= offset && count < lim) {
                    Map<String, Object> item = new HashMap<>();

                    document.forEach((key, value) -> {
                        if (value instanceof String stringValue) {
                            item.put(key, stringValue.replace("\"", ""));
                        } else {
                            item.put(key, value);
                        }
                    });

                    data.add(item);
                    count++;
                }

                itemCount++;
            }

            Set<String> propertyNames = MongoDBAlgorithms.getPropertyNames(collection);

            return new DocumentResponse(data, itemCount, propertyNames);
        }
        catch (Exception e){
            throw PullForestException.innerException(e);
        }
    }

    /**
     * Unsupported method for fetching foreign keys in MongoDB.
     *
     * @param kindName The name of the kind.
     * @throws UnsupportedOperationException as this operation is not implemented.
     */
    @Override public List<ForeignKey> getForeignKeys(String kindName) {
        throw new UnsupportedOperationException("MongoDBPullWrapper.getForeignKeys not implemented.");
    }

}
