/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor;

import cat.editor.view.cell.CellType;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.Model;
import cat.editor.view.Layout;
import cat.editor.view.RandomLayout;
import cat.editor.view.edge.EdgeType;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
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

    @FXML
    private ChoiceBox<String> zoom;

    private Graph graph = new Graph();

//    @FXML
//    private void handleButtonAction(ActionEvent event) {
//        System.out.println("You clicked me!");
//        label.setText("Hello World!");
//    }
    public void initialize() {

        zoom.setValue("100%");
        //Retrieving the observable list
        ObservableList<String> list = zoom.getItems();
        //Adding items to the list
        list.add("50%");
        list.add("75%");
        list.add("100%");
        list.add("150%");
        list.add("200%");
        list.add("300%");
        list.add("400%");

        zoom.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                String selected = zoom.getItems().get((Integer) number2);
                selected = selected.replace("%", "");
                double value = Double.parseDouble(selected);
                value /= 100.0;
                graph.getScrollPane().zoomTo(value);
//                System.out.println("ZOOMED TO VALUE: " + value + " :::: " + selected);
            }
        });

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

    private void buildSchemaCategory() {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        model.addEdge("100", "101");
        model.addEdge("101", "100");

        model.addEdge("100", "110");
        model.addEdge("110", "100");
        model.addEdge("111", "110");
        model.addEdge("110", "111");

        model.addEdge("113", "111");
        model.addEdge("111", "113");
        model.addEdge("111", "112");
        model.addEdge("112", "111");

        model.addEdge("113", "114");
        model.addEdge("114", "113");
        model.addEdge("113", "116");
        model.addEdge("116", "113");

        model.addEdge("114", "115");
        model.addEdge("115", "114");

        model.addEdge("117", "111");
        model.addEdge("111", "117");
        model.addEdge("117", "118");
        model.addEdge("118", "117");
        model.addEdge("117", "121");
        model.addEdge("121", "117");

        model.addEdge("121", "122");
        model.addEdge("122", "121");
        model.addEdge("121", "123");
        model.addEdge("123", "121");
        model.addEdge("121", "124");
        model.addEdge("124", "121");
        graph.endUpdate();
    }

    private void buildER() {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.ER_ENTITY);
        model.addCell("101", "Id", 100, 400, CellType.ER_IDENTIFIER);
        model.addCell("110", "Orders", 100, 200, CellType.ER_RELATIONSHIP);
        model.addCell("111", "Order", 200, 200, CellType.ER_ENTITY);
        model.addCell("112", "Number", 200, 100, CellType.ER_ATTRIBUTE);
        model.addCell("113", "Contact", 300, 200, CellType.ER_RELATIONSHIP);
        model.addCell("114", "Type", 400, 200, CellType.ER_ENTITY);
        model.addCell("115", "Name", 400, 100, CellType.ER_IDENTIFIER);
        model.addCell("116", "Value", 300, 100, CellType.ER_ATTRIBUTE);
        model.addCell("117", "Items", 200, 300, CellType.ER_RELATIONSHIP);
        model.addCell("118", "Quantity", 300, 300, CellType.ER_ATTRIBUTE);
        model.addCell("121", "Product", 200, 400, CellType.ER_ENTITY);
        model.addCell("122", "Id", 200, 500, CellType.ER_IDENTIFIER);
        model.addCell("123", "Name", 300, 500, CellType.ER_ATTRIBUTE);
        model.addCell("124", "Price", 300, 400, CellType.ER_ATTRIBUTE);

        model.addEdge("100", "101", EdgeType.ER);
        model.addEdge("110", "100", EdgeType.ER);
        model.addEdge("110", "111", EdgeType.ER);
        model.addEdge("113", "111", EdgeType.ER);
        model.addEdge("111", "112", EdgeType.ER);
        model.addEdge("113", "114", EdgeType.ER);
        model.addEdge("113", "116", EdgeType.ER);
        model.addEdge("114", "115", EdgeType.ER);
        model.addEdge("117", "111", EdgeType.ER);
        model.addEdge("117", "118", EdgeType.ER);
        model.addEdge("117", "121", EdgeType.ER);
        model.addEdge("121", "122", EdgeType.ER);
        model.addEdge("121", "123", EdgeType.ER);
        model.addEdge("121", "124", EdgeType.ER);
        graph.endUpdate();
    }

    private void addGraphComponents() {
        buildER();

//        buildSchemaCategory();
    }
}
