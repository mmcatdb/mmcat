package cat.tutorial;

/**
 *
 * @author pavel.koupil
 */
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import java.awt.Dimension;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author jeffreyguenther
 */
public class GraphVisTester extends Application{

    @Override
    public void start(Stage stage) throws Exception {
        Graph<String, Number> demoGraph = TestGraphs.getDemoGraph();
        System.out.println(demoGraph);
        Layout<String, Number> layout = new CircleLayout<>(demoGraph);
        layout.setSize(new Dimension(800, 800));
        GraphViz<String, Number> viewer = new GraphViz<>(layout);
        
        stage.setScene(new Scene(new Group(viewer)));
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    
}
