/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor;

import com.fxgraph.graph.CellType;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.Model;
import com.fxgraph.layout.base.Layout;
import com.fxgraph.layout.random.RandomLayout;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author pavel.koupil
 */
public class FXMLControllerDEMO {
    
//    @FXML
//    private Label label;

    @FXML
    private BorderPane borderPane;

    private Graph graph = new Graph();

//    @FXML
//    private void handleButtonAction(ActionEvent event) {
//        System.out.println("You clicked me!");
//        label.setText("Hello World!");
//    }

    public void initialize() {

        graph = new Graph();

        borderPane.setCenter(graph.getScrollPane());

//        Scene scene = new Scene(root, 1024, 768);
//        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//        primaryStage.setScene(scene);
//        primaryStage.show();
        addGraphComponents();

        Layout layout = new RandomLayout(graph);
        layout.execute();
        // TODO
    }

    private void addGraphComponents() {

        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("Cell A", CellType.RECTANGLE);
        model.addCell("Cell B", CellType.RECTANGLE);
        model.addCell("Cell C", CellType.RECTANGLE);
        model.addCell("Cell D", CellType.CIRCLE);
        model.addCell("Cell E", CellType.TRIANGLE);
        model.addCell("Cell F", CellType.RECTANGLE);
        model.addCell("Cell G", CellType.CIRCLE);
        model.addCell("Cell H", CellType.CIRCLE);
        model.addCell("Cell I", CellType.RECTANGLE);
        model.addCell("Cell J", CellType.TRIANGLE);
        model.addCell("Cell K", CellType.RECTANGLE);
        model.addCell("Cell L", CellType.CIRCLE);

//        model.addCell("Cell A", CellType.RECTANGLE);
//        model.addCell("Cell B", CellType.BUTTON);
//        model.addCell("Cell C", CellType.IMAGE);
//        model.addCell("Cell D", CellType.TRIANGLE);
//        model.addCell("Cell E", CellType.LABEL);
//        model.addCell("Cell F", CellType.TITLEDPANE);
//        model.addCell("Cell G", CellType.RECTANGLE);
        model.addEdge("Cell A", "Cell B");
        model.addEdge("Cell A", "Cell C");
        model.addEdge("Cell B", "Cell C");
        model.addEdge("Cell C", "Cell D");
        model.addEdge("Cell B", "Cell E");
        model.addEdge("Cell D", "Cell F");
        model.addEdge("Cell D", "Cell G");
        model.addEdge("Cell G", "Cell H");
        model.addEdge("Cell H", "Cell I");
        model.addEdge("Cell B", "Cell J");
        model.addEdge("Cell C", "Cell K");
        

        graph.endUpdate();

    }
}
