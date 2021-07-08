/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import cat.editor.view.cell.CellType;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.Model;
import cat.editor.view.Layout;
import cat.editor.view.RandomLayout;

public class Main extends Application {

    Graph graph = new Graph();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        graph = new Graph();

        root.setCenter(graph.getScrollPane());

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        addGraphComponents();

        Layout layout = new RandomLayout(graph);
        layout.execute();

    }

    private void addGraphComponents() {

        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("Cell A", "X", 0, 0, CellType.RECTANGLE);
        model.addCell("Cell B", "X", 0, 0, CellType.RECTANGLE);
        model.addCell("Cell C", "X", 0, 0, CellType.RECTANGLE);
        model.addCell("Cell D", "X", 0, 0, CellType.TRIANGLE);
        model.addCell("Cell E", "X", 0, 0, CellType.TRIANGLE);
        model.addCell("Cell F", "X", 0, 0, CellType.RECTANGLE);
        model.addCell("Cell G", "X", 0, 0, CellType.RECTANGLE);

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

        graph.endUpdate();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
