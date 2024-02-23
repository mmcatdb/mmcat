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

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.exception.OtherException;
//import cz.matfyz.evolution.Version;
import cz.matfyz.inference.algorithms.rba.Finalize;
import cz.matfyz.inference.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;
import cz.matfyz.inference.schemaconversion.CategoryMappingPair;
import cz.matfyz.inference.schemaconversion.SchemaConverter;
import cz.matfyz.wrappermongodb.MongoDBInferenceSchemaLessWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;

/**
 *
 * @author pavel.koupil
 */
public class MMInferOneInAll {

	public static final String PROPERTY_SPARK_MASTER = "baazizi.sparkMaster";
	private static final String sparkMaster = System.getProperty(PROPERTY_SPARK_MASTER, "local[*]");
	public static  String appName;
	public static String uri;
	public static String databaseName;
	public static String collectionName;
	
	public MMInferOneInAll input(String appName, String uri, String databaseName, String collectionName) {
		this.appName = appName;
		this.uri = uri;
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		
		return this;
	}
	
	public CategoryMappingPair run() {
		try {
			return innerRun();
		}
		catch (Exception e) { 
			throw new OtherException(e);
		}
	}
	public static CategoryMappingPair innerRun() throws IOException {
//		String sparkMaster = "localhost";
		
/*		String appName = "JSON Schema Inference, Record Based Algorithm";
		String uri = "localhost:3205";
		String databaseName = args[1];
		String collectionName = args[2];
		String checkpointDir = args[0]; */
		
		String checkpointDir = "C:\\Users\\alzbe\\Documents\\mff_mgr\\Diplomka\\Apps\\temp\\checkpoint"; //hard coded for now
		
		RecordBasedAlgorithm rba = new RecordBasedAlgorithm();

		AbstractInferenceWrapper wrapper = new MongoDBInferenceSchemaLessWrapper(sparkMaster, appName, uri, databaseName, collectionName, checkpointDir);
		AbstractRSDsReductionFunction merge = new DefaultLocalReductionFunction();
		Finalize finalize = null;
		long start = System.currentTimeMillis();
		RecordSchemaDescription rsd = rba.process(wrapper, merge, finalize);
		long end = System.currentTimeMillis();
		
		SchemaConverter scon = new SchemaConverter(rsd);
		
		CategoryMappingPair cmp = scon.convertToSchemaCategoryAndMapping();
		
		return cmp;
		
//		System.out.print("RESULT: ");
//		System.out.println(rsd);
//		System.out.println("RESULT_TIME TOTAL: " + (end - start) + "ms");
		
//		System.out.println(cmp.schemaCat());	
		
//		System.out.println("MY_DEBUG: -------------");
//		System.out.println("MY_DEBUG: SchemaCategory");
		
/*		Collection<SchemaObject> objs = cmp.schemaCat().allObjects();		
		System.out.println("MY_DEBUG objects in category: ");
		
		for (SchemaObject obj: objs) {
			  System.out.println("MY_DEBUG object name: " + obj.label());
			}
	
		System.out.println("MY_DEBUG: -------------");
		Collection<SchemaMorphism> morphs = cmp.schemaCat().allMorphisms();
		System.out.println("MY_DEBUG morphisms in category: ");
		
		for (SchemaMorphism morph: morphs) {
			  System.out.println("MY_DEBUG morphism rel: " + morph.dom().label() + " -> " + morph.cod().label() + " and the label is: " + morph.label);
			}
		*/	
		
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
