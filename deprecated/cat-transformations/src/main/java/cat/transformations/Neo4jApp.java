package cat.transformations;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.Result;

import java.util.Map;
import java.io.File;
import org.neo4j.graphdb.QueryExecutionException;

/**
 *
 *
 */
public class Neo4jApp {

	private static enum MyType implements RelationshipType {
		KNOW,
		PLAY
	}

	public static class MyEvaluator1 implements Evaluator {

		@Override
		public Evaluation evaluate(Path path) {
			if (path.length() == 0) {
				return Evaluation.EXCLUDE_AND_CONTINUE;
			} else {
				return Evaluation.INCLUDE_AND_CONTINUE;
			}
		}
	}

	public static class MyEvaluator2 implements Evaluator {

		@Override
		public Evaluation evaluate(Path path) {
			if (path.endNode().hasLabel(Label.label("MOVIE"))) {
				return Evaluation.EXCLUDE_AND_CONTINUE;
			} else {
				return Evaluation.INCLUDE_AND_CONTINUE;
			}
		}
	}

	public static void main(String[] args) {

		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File("MyNeo4jDB"));

		Transaction tx = db.beginTx();
		try {

			Node actor1 = db.createNode();
			actor1.setProperty("id", "trojan");
			actor1.setProperty("name", "Ivan Trojan");
			actor1.setProperty("year", 1964);
			actor1.addLabel(Label.label("ACTOR"));

			Node actor2 = db.createNode();
			actor2.setProperty("id", "machacek");
			actor2.setProperty("name", "Jiří Macháček");
			actor2.setProperty("year", 1966);
			actor2.addLabel(Label.label("ACTOR"));

			Node actor3 = db.createNode();
			actor3.setProperty("id", "schneiderova");
			actor3.setProperty("name", "Jitka Schneiderová");
			actor3.setProperty("year", 1973);
			actor3.addLabel(Label.label("ACTOR"));

			Node actor4 = db.createNode();
			actor4.setProperty("id", "sverak");
			actor4.setProperty("name", "Zdeněk Svěrák");
			actor4.setProperty("year", 1936);
			actor4.addLabel(Label.label("ACTOR"));

			actor1.createRelationshipTo(actor2, MyType.KNOW);
			actor1.createRelationshipTo(actor3, MyType.KNOW);
			actor2.createRelationshipTo(actor1, MyType.KNOW);
			actor2.createRelationshipTo(actor3, MyType.KNOW);
			actor4.createRelationshipTo(actor2, MyType.KNOW);

			Node movie1 = db.createNode();
			movie1.setProperty("id", "samotari");
			movie1.setProperty("title", "Samotáři");
			movie1.setProperty("year", 2000);
			movie1.addLabel(Label.label("MOVIE"));

			Node movie2 = db.createNode();
			movie2.setProperty("id", "medvidek");
			movie2.setProperty("title", "Medvídek");
			movie2.setProperty("year", 2007);
			movie2.addLabel(Label.label("MOVIE"));

			Node movie3 = db.createNode();
			movie3.setProperty("id", "vratnelahve");
			movie3.setProperty("title", "Vratné lahve");
			movie3.setProperty("year", 2006);
			movie3.addLabel(Label.label("MOVIE"));

			movie1.createRelationshipTo(actor1, MyType.PLAY);
			movie1.createRelationshipTo(actor2, MyType.PLAY);
			movie1.createRelationshipTo(actor3, MyType.PLAY);
			movie2.createRelationshipTo(actor1, MyType.PLAY);
			movie3.createRelationshipTo(actor4, MyType.PLAY);

			System.out.println("TRAVERSAL #1");

			TraversalDescription td1 = db.traversalDescription()
					.breadthFirst()
					.relationships(MyType.KNOW, Direction.BOTH)
					.evaluator(Evaluators.excludeStartPosition())
					.uniqueness(Uniqueness.NODE_GLOBAL);

			Traverser t1 = td1.traverse(actor1);
			for (Path p : t1) {
				System.out.println(p.endNode().getProperty("name"));
			}

			System.out.println("TRAVERSAL #2");

			TraversalDescription td2 = db.traversalDescription()
					.breadthFirst()
					.relationships(MyType.PLAY, Direction.OUTGOING)
					.relationships(MyType.KNOW, Direction.BOTH)
					.evaluator(new MyEvaluator1())
					.uniqueness(Uniqueness.NODE_GLOBAL);
			Traverser t2 = td2.traverse(movie2);
			for (Path p : t2) {
				System.out.println(p.endNode().getProperty("name"));
			}

			System.out.println("CYPHER #1");

			Result result = db.execute("MATCH (n:MOVIE) RETURN n");
			while (result.hasNext()) {
				Map<String, Object> row = result.next();
				Node n = (Node) row.get("n");
				System.out.println(n.getProperty("title"));
			}

			tx.success();

		} catch (QueryExecutionException ex) {

			tx.failure();

		} finally {

			tx.close();

		}

		db.shutdown();

	}

}
