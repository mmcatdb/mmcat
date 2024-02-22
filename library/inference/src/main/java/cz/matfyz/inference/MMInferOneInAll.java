/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package cz.matfyz.inference;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.evolution.Version;
import cz.matfyz.inference.algorithms.rba.Finalize;
import cz.matfyz.inference.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;
import cz.matfyz.inference.model.RecordSchemaDescription;
import cz.matfyz.inference.schemaconversion.AccessTreeNode;
import cz.matfyz.inference.schemaconversion.SchemaConverter;
import cz.matfyz.inference.wrappers.AbstractWrapper;
import cz.matfyz.inference.wrappers.MongoDBSchemaLessWrapper;
import cz.matfyz.server.builder.MetadataContext;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.Position;

/**
 *
 * @author pavel.koupil
 */
public class MMInferOneInAll {

	public static final String PROPERTY_SPARK_MASTER = "baazizi.sparkMaster";
	private static final String sparkMaster = System.getProperty(PROPERTY_SPARK_MASTER, "local[*]");

	public static void main(String[] args) throws IOException {
//		String sparkMaster = "localhost";
		String appName = "JSON Schema Inference, Record Based Algorithm";
		String uri = "localhost:3205";
		String databaseName = args[1];
		String collectionName = args[2];
		String checkpointDir = args[0];

		RecordBasedAlgorithm rba = new RecordBasedAlgorithm();

		AbstractWrapper wrapper = new MongoDBSchemaLessWrapper(sparkMaster, appName, uri, databaseName, collectionName, checkpointDir);
		AbstractRSDsReductionFunction merge = new DefaultLocalReductionFunction();
		Finalize finalize = null;
		long start = System.currentTimeMillis();
		RecordSchemaDescription rsd = rba.process(wrapper, merge, finalize);
		long end = System.currentTimeMillis();
//		System.out.print("RESULT: ");
//		System.out.println(rsd);
//		System.out.println("RESULT_TIME TOTAL: " + (end - start) + "ms");
		
		
		SchemaConverter scon = new SchemaConverter(rsd);
		scon.convertToSchemaCategory();
		SchemaCategory sc = scon.sc;
		
//		System.out.println("MY_DEBUG: -------------");
//		System.out.println("MY_DEBUG: SchemaCategory");
		
//		Collection<SchemaObject> objs = sc.allObjects();		
//		System.out.println("MY_DEBUG objects in category: ");
/*		
		for (SchemaObject obj: objs) {
			  System.out.println("MY_DEBUG object name: " + obj.label());
			}
	
		System.out.println("MY_DEBUG: -------------");
		Collection<SchemaMorphism> morphs = sc.allMorphisms();
		System.out.println("MY_DEBUG morphisms in category: ");
		
		for (SchemaMorphism morph: morphs) {
			  System.out.println("MY_DEBUG morphism rel: " + morph.dom().label() + " -> " + morph.cod().label() + " and the label is: " + morph.label);
			}
*/		
		Mapping mapping = scon.createMapping(sc, "Bussiness");
//		System.out.println("MY_DEBUG mapping:" + mapping);
		
		
		// *Create SchemaCategoryWrapper from SchemaCategory*
    	MetadataContext context = new MetadataContext();
    	
    	//Create a special id, under which you later load this category  
    	Id id = new Id("schm_from_rsd"); //now hard coded special id 
    	context.setId(id);
    	
    	Version version = Version.generateInitial(); // can I use this?
    	context.setVersion(version);
    	
    	Position initialPosition = new Position(0.0, 0.0);
    	
    	for (SchemaObject so : sc.allObjects()) {
    		Key key = so.key();
    		context.setPosition(key, initialPosition);
    	}  	
	    	
    	SchemaCategoryWrapper scw = SchemaCategoryWrapper.fromSchemaCategory(sc, context);
    	//System.out.println("This is the SchemaCategoryWrapper: " + scw);
    	System.out.println("It all went well");
    	
		/*
		// *Serialize wrapper to a json* (make sure to use jackson)
		ObjectMapper mapper = new ObjectMapper();
		String jsonPayload = mapper.writeValueAsString(scw);
		System.out.println("This is the SchemaCategoryWrapper serialized: " + jsonPayload);
		
		// *Send the wrapper as a request to mmcat*
		// 1) *With HttpURLConnection*
		//URL from the frontend in the .env file
		String appAUrl = "http://localhost:3201/api/v1/schema-categories/store";

		URL url = new URL(appAUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		con.setDoOutput(true);

		try (OutputStream os = con.getOutputStream()) {
		    byte[] input = jsonPayload.getBytes("utf-8");
		    os.write(input, 0, input.length);
		}

		try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println("Response from mmcat: " + response.toString());
        }
		*/
	}
}
