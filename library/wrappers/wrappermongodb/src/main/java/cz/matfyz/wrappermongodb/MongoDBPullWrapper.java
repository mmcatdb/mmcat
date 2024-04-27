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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

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
        final var forest = new ForestOfRecords();

        try (
            var iterator = getDocumentIterator(query);
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

        for (AccessPath subpath : path.subpaths()) {
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

        var value = document.get(key);

        if (subpath instanceof ComplexProperty complexSubpath)
            getFieldFromObjectForComplexSubpath(parentRecord, key, value, complexSubpath);
        else if (subpath instanceof SimpleProperty simpleSubpath)
            getFieldFromObjectForSimpleSubpath(parentRecord, key, value, simpleSubpath);
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

}
