/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.wrappers;

import cat.transformations.algorithms2.model.AbstractArrayProperty;
import cat.transformations.algorithms2.model.AbstractAttributeProperty;
import cat.transformations.algorithms2.model.AbstractIdentifier;
import cat.transformations.algorithms2.model.AbstractKind;
import cat.transformations.algorithms2.model.AbstractModel;
import cat.transformations.algorithms2.model.AbstractRecordProperty;
import cat.transformations.algorithms2.model.DocumentArray;
import cat.transformations.algorithms2.model.DocumentKind;
import cat.transformations.algorithms2.model.DocumentProperty;
import cat.transformations.algorithms2.model.DocumentRecord;
import cat.transformations.algorithms2.model.DocumentSimpleValue;
import cat.transformations.algorithms2.model.SimpleIdentifier;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/**
 *
 * @author pavel.koupil
 */
public class DocumentWrapper {

	public void wrap(MongoDatabase database, AbstractModel model) {
		// list collections
		for (String name : database.listCollectionNames()) {
			System.out.println(name);
			AbstractKind kind = new DocumentKind(name);

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

	private static AbstractAttributeProperty buildAttribute(String name, Object value) {
		return new DocumentProperty(name, value, false, false, false);
	}

	private static AbstractRecordProperty buildRecord(String name, Document document, boolean nested) {
		System.out.println(String.format("\tbuildingRecord(%s, %s, %s)", name, document, nested));
		AbstractRecordProperty record = new DocumentRecord(name);

		var embeddedSID = document.get(name + "_id");
		if (embeddedSID != null) {
			embeddedSID = System.currentTimeMillis() + "TODO-EMBEDDED_ID!";
			System.out.println(String.format("\t\tGenerating embedded SID: %s", embeddedSID));
		}

		if (nested) {
			AbstractIdentifier superid = new SimpleIdentifier();
			List<Object> identifier = new ArrayList<>();
			identifier.add(embeddedSID);
			superid.add(identifier);
			record.setIdentifier(superid);
		}

		for (Map.Entry<String, Object> property : document.entrySet()) {

			if (!nested) {
				if (property.getKey().equals("_id")) {
					AbstractIdentifier superid = new SimpleIdentifier();
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

	private static AbstractArrayProperty processArray(String name, List array) {
		if (array.isEmpty()) {
			System.out.println("TODO: processArray - empty array");
			return new DocumentArray();	// TODO: neni dobre, mas vkladat prazdne pole!
		}

		AbstractArrayProperty arrayProperty = new DocumentArray();

		for (var element : array) {
			if (element instanceof List) {
				System.out.println("TODO: processArray - nested array");
//					processArray(result, key, (List) element, entity, sid, queue, queueOfNames);
			} else if (element instanceof Document) {
				System.out.println("TODO: processArray - array of documents");
//					processRecord(result, key, (Document) element, entity, sid, queue, queueOfNames);
			} else {
//					processAttribute(result, key, element, entity, sid);
				arrayProperty.add(new DocumentSimpleValue(name, element));
				System.out.println("TODO: processArray - array of properties");
			}
		}

		return arrayProperty;

	}

}
