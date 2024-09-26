package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
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
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RecordName;
import cz.matfyz.core.record.RootRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MongoDBPullWrapper implements AbstractPullWrapper {

    private MongoDBProvider provider;

    public MongoDBPullWrapper(MongoDBProvider provider) {
        this.provider = provider;
    }

    private MongoCursor<Document> getDocumentIterator(QueryContent query) {
        if (query instanceof KindNameQuery kindNameQuery)
            return getDocumentIteratorFromKindName(kindNameQuery);

        if (query instanceof MongoDBQuery mongoQuery)
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
        System.out.println("mongo pullwrapper");
        System.out.println("query: " + query);
        final var forest = new ForestOfRecords();

        try (
            var iterator = getDocumentIterator(query);
        ) {
            while (iterator.hasNext()) {
                final Document document = iterator.next();
                System.out.println("document to json: " + document.toJson());
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

        for (AccessPath subpath : path.subpaths()) {
            System.out.println("getDataFromDocu name of path: " + subpath.name());
            if (subpath.name() instanceof StaticName staticName) {
                System.out.println(subpath.name() + "is about to enter getFieldFromObjectForSubpath");
                getFieldFromObjectForSubpath(parentRecord, document, staticName.getStringName(), subpath);
            }
            else
                hasSubpathWithDynamicName = true;
        }

        if (hasSubpathWithDynamicName)
            getDataFromDynamicFieldsOfObject(parentRecord, document, path);
    }

    private void getDataFromDynamicFieldsOfObject(ComplexRecord parentRecord, Document document, ComplexProperty path) {
        // First we find all names that belong to the subpaths with non-dynamic names and also the subpath with the dynamic name.
        AccessPath subpathWithDynamicName = null;
        Set<String> otherSubpathNames = new TreeSet<>();

        for (AccessPath subpath : path.subpaths()) {
            if (subpath.name() instanceof StaticName staticName)
                otherSubpathNames.add(staticName.getStringName());
            else
                subpathWithDynamicName = subpath;
        }

        // For all keys in the object where the key is not a known static name do ...
        for (String key : document.keySet()) {
            if (!otherSubpathNames.contains(key))
                getFieldFromObjectForSubpath(parentRecord, document, key, subpathWithDynamicName);
        }
    }

    private void getFieldFromObjectForSubpath(ComplexRecord parentRecord, Document document, String key, AccessPath subpath) {
        // TODO Check for the null values if necessary.
        /*
        if (document.isNull(key)) // Returns if the value is null or if the value doesn't exist.
            return;
        */

        //var value = document.get(key);
        //if (key.equals("monday")) {System.out.println("changing up that key, girl"); key = "Monday";}
        var value = document.get(key);
        System.out.println("getFieldFromObjectForSubpath key:" + key);
        System.out.println("getFieldFromObjectForSubpath value: " + value);

        if (subpath instanceof ComplexProperty complexSubpath) {
            System.out.println(subpath.name() + "is about to enter getFieldFromObjectForComplexSubpath");
            getFieldFromObjectForComplexSubpath(parentRecord, key, value, complexSubpath); }
        else if (subpath instanceof SimpleProperty simpleSubpath) {
            System.out.println(subpath.name() + "is about to enter getFieldFromObjectForSimpleSubpath");
            getFieldFromObjectForSimpleSubpath(parentRecord, key, value, simpleSubpath); }
    }

    private void getFieldFromObjectForComplexSubpath(ComplexRecord parentRecord, String key, Object value, ComplexProperty complexSubpath) {
        if (value instanceof ArrayList<?> childArray) {
            for (int i = 0; i < childArray.size(); i++)
                if (childArray.get(i) instanceof Document childDocument)
                    addComplexValueToRecord(parentRecord, childDocument, key, complexSubpath);
        }
        else if (value instanceof Document childDocument)
            addComplexValueToRecord(parentRecord, childDocument, key, complexSubpath);
    }

    private void getFieldFromObjectForSimpleSubpath(ComplexRecord parentRecord, String key, Object value, SimpleProperty simpleSubpath) {
        if (value instanceof ArrayList<?> simpleArray) {
            System.out.println(simpleSubpath.name() + "is simpleArray");
            var values = new ArrayList<String>();

            for (int i = 0; i < simpleArray.size(); i++)
                values.add(simpleArray.get(i).toString());

            parentRecord.addSimpleArrayRecord(toRecordName(simpleSubpath.name(), key), simpleSubpath.signature(), values);
        }
        else {
            RecordName recordName = toRecordName(simpleSubpath.name(), key);
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

        var staticName = (StaticName) name;
        return staticName.toRecordName();
    }

    public String readCollectionAsStringForTests(String kindName) {
        var database = provider.getDatabase();
        MongoCollection<Document> collection = database.getCollection(kindName);
        Iterator<Document> iterator = collection.find().iterator();

        var output = new StringBuilder();
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

    private JSONObject getResultObject(int rowCount, JSONArray resultData) throws JSONException {
        JSONObject metadata = new JSONObject();
        metadata.put("rowCount", rowCount);

        JSONObject result = new JSONObject();
        result.put("metadata", metadata);
        result.put("data", resultData);

        return result;
    }

    @Override public JSONObject getTableNames(String limit, String offsetString) {
        try {
            MongoIterable<String> tableNames = provider.getDatabase().listCollectionNames();
            JSONArray resultData = new JSONArray();

            int lim = Integer.parseInt(limit);
            int offset = Integer.parseInt(offsetString);

            int index = 0;
            int count = 0;

            for (String tableName : tableNames) {
                if (index >= offset && count < lim) {
                    resultData.put(tableName);
                    count++;
                }

                index++;
            }

            return getResultObject(index, resultData);
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
    }

    @Override public JSONObject getTable(String tableName, String limit, String offsetString){
        try {
            JSONArray resultData = new JSONArray();
            MongoCollection<Document> collection = provider.getDatabase().getCollection(tableName);
            FindIterable<Document> documents = collection.find();

            int lim = Integer.parseInt(limit);
            int offset = Integer.parseInt(offsetString);

            int index = 0;
            int count = 0;

            for (Document document : documents) {
                if (index >= offset && count < lim) {
                    JSONObject jsonObject = new JSONObject(document.toJson());
                    JSONObject dataWithoutQuotationMarks = new JSONObject();
                    Iterator<String> keys = jsonObject.keys();

                    while(keys.hasNext()) {
                        String key = keys.next();
                        Object value = jsonObject.get(key);
                        if (value instanceof String) {
                            dataWithoutQuotationMarks.put(key, ((String) value).replace("\"", ""));
                        } else {
                            dataWithoutQuotationMarks.put(key, value);
                        }
                    }

                    resultData.put(dataWithoutQuotationMarks);
                    count++;
                }

                index++;
            }

            return getResultObject(index, resultData);
        }
        catch (Exception e){
            throw PullForestException.innerException(e);
        }
    }

    private Bson createFilter(String columnName, String operator, String columnValue) {
        var value = "_id".equals(columnName) ? new ObjectId(columnValue) : columnValue;

        switch (operator) {
            case "=":
                return Filters.eq(columnName, value);
            case "<>":
                return Filters.ne(columnName, value);
            case "<":
                return Filters.lt(columnName, value);
            case ">":
                return Filters.gt(columnName, value);
            case "<=":
                return Filters.lte(columnName, value);
            case ">=":
                return Filters.gte(columnName, value);
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + operator);
        }
    }

    @Override public JSONObject getRows(String tableName, String columnName, String columnValue, String operator, String limit, String offsetString){
        try {
            JSONArray resultData = new JSONArray();
            MongoCollection<Document> collection = provider.getDatabase().getCollection(tableName);
            Bson filter = createFilter(columnName, operator, columnValue);
            FindIterable<Document> documents = collection.find(filter);

            int lim = Integer.parseInt(limit);
            int offset = Integer.parseInt(offsetString);

            int index = 0;
            int count = 0;

            for (Document document : documents) {
                if (index >= offset && count < lim) {
                    JSONObject jsonObject = new JSONObject(document.toJson());
                    JSONObject dataWithoutQuotationMarks = new JSONObject();
                    Iterator<String> keys = jsonObject.keys();

                    while(keys.hasNext()) {
                        String key = keys.next();
                        Object value = jsonObject.get(key);
                        if (value instanceof String) {
                            dataWithoutQuotationMarks.put(key, ((String) value).replace("\"", ""));
                        } else {
                            dataWithoutQuotationMarks.put(key, value);
                        }
                    }

                    resultData.put(dataWithoutQuotationMarks);
                    count++;
                }

                index++;
            }

            return getResultObject(index, resultData);
        }
        catch (Exception e){
            throw PullForestException.innerException(e);
        }
    }

}
