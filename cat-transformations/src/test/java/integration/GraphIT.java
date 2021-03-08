/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integration;

import cat.transformations.algorithms.Algorithms;
import cat.transformations.algorithms.TransformationDocToInst;
import cat.transformations.algorithms.TransformationInstToDoc;
import cat.transformations.category.InstanceCategory;
import cat.transformations.commons.Constants;
import cat.transformations.model.AbstractTable;
import cat.transformations.model.CSVTable;
import cat.transformations.model.RelationalInstance;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author pavel.contos
 */
@RunWith(MockitoJUnitRunner.class)
public class GraphIT {

//    private static final Logger LOGGER = LoggerFactory.getLogger(CidServiceTest.class);
//    @InjectMocks
//    private final CidService service = new CidService();
//    @Mock
//    private RepositoryService repository;
//    @Mock
//    private VersionValidator versionValidator;
	public GraphIT() {
	}

	@BeforeClass
	public static void setUpClass() {

	}

	@AfterClass
	public static void tearDownClass() {

	}

	@Before
	public void setUp() {
	}

//    @After
	public void tearDown() {
	}

	private void printTestHeader(String text) {
		StringBuilder builder = new StringBuilder();
		builder.append(Constants.ANSI_BLUE);
		builder.append("------------------------------------------------------------------------------------------------------------------------");
		builder.append("\n");
		builder.append("\t");
		builder.append(text);
		builder.append("\n");
		builder.append("------------------------------------------------------------------------------------------------------------------------");
		builder.append("\n");
		builder.append(Constants.ANSI_RESET);
		System.out.println(builder);
	}

	@Test
//	@Ignore
	public void demo() {
		printTestHeader("DocumentIT -> Demo");

	}

	@Test
//	@Ignore
	public void scenario1() {
		printTestHeader("DocumentIT -> Scenario1");

	}
}
