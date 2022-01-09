package cat.transformations.wrappers;

import cat.transformations.algorithms2.model.AbstractArrayProperty;
import cat.transformations.algorithms2.model.AbstractAttributeProperty;
import cat.transformations.algorithms2.model.AbstractIdentifier;
import cat.transformations.algorithms2.model.AbstractKind;
import cat.transformations.algorithms2.model.AbstractModel;
import cat.transformations.algorithms2.model.AbstractRecordProperty;
import cat.transformations.algorithms2.model.DocumentFactory;
//import cat.transformations.algorithms2.model.SimpleIdentifier;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

/**
 *
 * @author pavel.contos
 */
public class DocumentWrapper {

	private static final Logger LOGGER = Logger.getLogger(DocumentWrapper.class.getName());

	private String generateEID() {
		return "EID" + (System.currentTimeMillis() % 1000);
	}

	public void wrap(MongoDatabase database, AbstractModel model) {
		// list collections
		for (String name : database.listCollectionNames()) {
			System.out.println(name);
			AbstractKind kind = DocumentFactory.INSTANCE.createKind(name);

			// cursor
			MongoCollection<Document> collection = database.getCollection(name);
			try ( MongoCursor<Document> cur = collection.find().iterator()) {
				while (cur.hasNext()) {
					var doc = cur.next();

					System.out.println("BEGIN\t" + doc);

					AbstractRecordProperty record = buildRecord(name, doc, false);

					kind.add(record);

				}
			}
			model.putKind("cars", kind);
		}
	}

	private AbstractAttributeProperty buildAttribute(String name, Object value) {
		return DocumentFactory.INSTANCE.createProperty(name, value, false, false, false);
	}

	private AbstractRecordProperty buildRecord(String name, Document document, boolean nested) {
		System.out.println(String.format("\tbuildingRecord(%s, %s, %s)", name, document, nested));
		AbstractRecordProperty record = DocumentFactory.INSTANCE.createRecord(name);

		var embeddedSID = document.get(name + "_id");
		if (embeddedSID == null) {
			embeddedSID = generateEID();
//			System.out.println(String.format("\t\tGenerating embedded SID: %s", embeddedSID));
		}

		if (nested) {
			AbstractIdentifier superid = DocumentFactory.INSTANCE.createIdentifier();
			List<Object> identifier = new ArrayList<>();
			identifier.add(embeddedSID);
			superid.add(identifier);
			record.setIdentifier(superid);
		}

		for (Map.Entry<String, Object> property : document.entrySet()) {

			if (!nested) {
				if (property.getKey().equals("_id")) {
					AbstractIdentifier superid = DocumentFactory.INSTANCE.createIdentifier();
					List<Object> identifier = new ArrayList<>();
					identifier.add(property.getValue());
					superid.add(identifier);
					record.setIdentifier(superid);
					System.out.println(String.format("\t\tRetrieved superid %s", record.getIdentifier()));
				}
			}

			if (property.getValue() instanceof List) {
				System.out.println(String.format("\t\tARRAY Case %s\tKEY: %s\tVALUE: %s", property.getClass(), property.getKey(), property.getValue()));
				AbstractArrayProperty arrayProperty = processArray(property.getKey(), (List) property.getValue());
				record.putProperty(property.getKey(), arrayProperty);
			} else if (property.getValue() instanceof Document) {
				System.out.println(String.format("\t\tDOCUMENT Case %s\tKEY: %s\tVALUE: %s", property.getClass(), property.getKey(), property.getValue()));
				AbstractRecordProperty childRecord = buildRecord(property.getKey(), (Document) property.getValue(), true);
				record.putProperty(property.getKey(), childRecord);
			} else {
				System.out.println(String.format("\t\tATTRIBUTE Case %s\tKEY: %s\tVALUE: %s", property.getClass(), property.getKey(), property.getValue()));
				AbstractAttributeProperty attribute = buildAttribute(property.getKey(), property.getValue());
				record.putProperty(property.getKey(), attribute);
			}

		}

		return record;
	}

	private AbstractArrayProperty processArray(String name, List array) {
		LOGGER.log(Level.INFO, String.format("Beginning of processing array %s with length %s", name, array.size()));
		if (array.isEmpty()) {
			LOGGER.log(Level.SEVERE, "Array is empty -> created empty array. Je to v poradku?");
			return DocumentFactory.INSTANCE.createArray(name);
		}

		AbstractArrayProperty arrayProperty = DocumentFactory.INSTANCE.createArray(name);

		for (var element : array) {
			if (element instanceof List) {
				LOGGER.log(Level.SEVERE, "TODO: Process nested array of arrays");
//					processArray(result, key, (List) element, entity, sid, queue, queueOfNames);
			} else if (element instanceof Document) {
				AbstractRecordProperty record = buildRecord(name + ".items", (Document) element, true);
				arrayProperty.add(record);
				LOGGER.log(Level.INFO, String.format("Added record %s to array %s", record.getName(), arrayProperty.getName()));
			} else {
				AbstractAttributeProperty attribute = buildAttribute(name, element);
				arrayProperty.add(attribute);
				LOGGER.log(Level.INFO, String.format("Added attribute %s to array %s", attribute.getValue(), arrayProperty.getName()));
			}
		}

		return arrayProperty;

	}

}
