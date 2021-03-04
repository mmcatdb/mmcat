/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms;

import cat.transformations.category.InstanceCategory;
import cat.transformations.model.RelationalInstance;
import cat.transformations.model.RelationalTable;
import cat.transformations.model.Schema;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/**
 *
 * @author pavel.koupil
 */
public class TransformationInstToDoc {

	public void process(InstanceCategory category, MongoDatabase database) {

// ----- BEGINNING -----------------------------------------------------------------------------------------------------
		var objectNames = category.getObjectNames();
		for (var objectName : objectNames) {
//			System.out.println("Processing " + objectName);
			if (!category.isEntity(objectName)) {
//				System.out.println("NOT ENTITY!");
				continue;
			}

			var object = category.getObject(objectName);

			MongoCollection<Document> collection = database.getCollection(objectName);
			for (int index = 0; index < object.size(); ++index) {
				Map<String, Object> record = category.getDocumentRecord(objectName, index);

				// process document - moznosti list, map, other
				Document document = processRecord(record);

				if (document != null) {
					collection.insertOne(document);
				}
			}

		}
// ----- END -----------------------------------------------------------------------------------------------------------
//		return relationalInstance;
	}

	private Document processRecord(Map<String, Object> record) {
		Document document = new Document(record);
		return document;
	}

}
