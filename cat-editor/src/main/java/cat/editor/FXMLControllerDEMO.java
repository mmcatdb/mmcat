/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor;

import cat.dummy.DummyGraphScenario;
import cat.editor.view.cell.CellType;
import cat.editor.view.Graph;
import cat.editor.view.Model;
import cat.editor.view.Layout;
import cat.editor.view.RandomLayout;
import cat.editor.view.edge.EdgeType;
import cat.tutorial.AddPersonDialogController;
import java.io.IOException;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    @FXML
    private Tab diagramTab;

    @FXML
    private Tab styleTab;

    @FXML
    private Tab textTab;

    @FXML
    private Tab positionTab;

    @FXML
    private TreeView treeView;

    @FXML
    private Button componentButton;

    private Graph graph = new Graph();

//    @FXML
//    private void handleButtonAction(ActionEvent event) {
//        System.out.println("You clicked me!");
//        label.setText("Hello World!");
//    }
    @FXML
    void onOpenDialog(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("dialogDatabaseComponent.fxml"));
        Parent parent = fxmlLoader.load();
        AddPersonDialogController dialogController = fxmlLoader.<AddPersonDialogController>getController();
//        dialogController.setAppMainObservableList(tvObservableList);

        Scene scene = new Scene(parent, 480, 300);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void initComponentButton() {

        //Creating a dialog
        Dialog<String> dialog = new Dialog<String>();
        //Setting the title
        dialog.setTitle("Dialog");
        ButtonType type = new ButtonType("Ok", ButtonData.OK_DONE);
        //Setting the content of the dialog
        dialog.setContentText("This is a sample dialog");
        //Adding buttons to the dialog pane
        dialog.getDialogPane().getButtonTypes().add(type);
        //Setting the label
        Text txt = new Text("Click the button to show the dialog");
        Font font = Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 12);
        txt.setFont(font);
        //Creating a button
//        Button button = new Button("Show Dialog");
        //Showing the dialog on clicking the button
        componentButton.setOnAction(e -> {
            dialog.showAndWait();
        });

//        componentButton.setOnAction(new EventHandler<ActionEvent>() {
//            public void handle(ActionEvent event) {
//                Parent root;
//                try {
//                    root = FXMLLoader.load(getClass().getClassLoader().getResource("dialogDatabaseComponent.fxml"), resources);
//                    Stage stage = new Stage();
//                    stage.setTitle("My New Stage Title");
//                    stage.setScene(new Scene(root, 450, 450));
//                    stage.show();
//                    // Hide this current window (if this is what you want)
//                    ((Node) (event.getSource())).getScene().getWindow().hide();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    private void initTreeView() {
        //Creating tree items
        TreeItem root1 = new TreeItem("Schema");
        TreeItem item1 = new TreeItem("ER");
        TreeItem item2 = new TreeItem("Categorical");
        TreeItem item3 = new TreeItem("Categorical_2");
        //Adding elements to root1
        root1.getChildren().addAll(item1, item2, item3);
        TreeItem root2 = new TreeItem("Mapping");
        TreeItem root3 = new TreeItem("PostgreSQL");
        TreeItem root4 = new TreeItem("Neo4j");
        TreeItem root5 = new TreeItem("MongoDB");
        TreeItem root6 = new TreeItem("RiakKV");
        TreeItem root7 = new TreeItem("Cassandra");
        //Adding elements to root2
        root2.getChildren().addAll(root3, root4, root5, root6, root7);

        TreeItem item4 = new TreeItem("Customer");
        TreeItem item5 = new TreeItem("Orders");
        TreeItem item6 = new TreeItem("Order");
        TreeItem item7 = new TreeItem("Items");
        TreeItem item8 = new TreeItem("Product");
        TreeItem item82 = new TreeItem("Product2");
        TreeItem item83 = new TreeItem("Product3");
        TreeItem item9 = new TreeItem("Contact");
        TreeItem item10 = new TreeItem("Type");
        root3.getChildren().addAll(item4, item5, item6, item7, item8, item82, item83, item9, item10);

        TreeItem root8 = new TreeItem("Transformations");
        TreeItem root9 = new TreeItem("Instance");

        //Adding elements to root2
//      root3.getChildren().addAll(item7, item8, item9);
        //list View for educational qualification
        TreeItem<String> base = new TreeItem<>("Project NAME");
        base.setExpanded(true);
        base.getChildren().addAll(root1, root2, root8, root9);
        //Creating a TreeView item
        treeView.setRoot(base);

//      view.setPrefHeight(300);
        treeView.setOnMouseClicked(new EventHandler<>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    TreeItem item = (TreeItem) treeView.getSelectionModel().getSelectedItem();
                    System.out.println("Selected Text : " + item.getValue());

                    String value = (String) item.getValue();

                    if (value.equals("ER")) {

                        graph = new Graph();
                        borderPane.setCenter(graph.getScrollPane());
                        DummyGraphScenario.INSTANCE.buildER(graph);
                        Layout layout = new RandomLayout(graph);
                        layout.execute();

                    } else if (value.equals("Categorical")) {
                        graph = new Graph();
                        borderPane.setCenter(graph.getScrollPane());
                        DummyGraphScenario.INSTANCE.buildSchemaCategory(graph);
                        Layout layout = new RandomLayout(graph);
                        layout.execute();
                    } else if (value.equals("Product")) {
                        graph = new Graph();
                        borderPane.setCenter(graph.getScrollPane());
                        DummyGraphScenario.INSTANCE.buildProductKind(graph);
                        Layout layout = new RandomLayout(graph);
                        layout.execute();
                    } else if (value.equals("Product2")) {
                        graph = new Graph();
                        borderPane.setCenter(graph.getScrollPane());
                        DummyGraphScenario.INSTANCE.buildProductKind2(graph);
                        Layout layout = new RandomLayout(graph);
                        layout.execute();
                    } else if (value.equals("Product3")) {
                        graph = new Graph();
                        borderPane.setCenter(graph.getScrollPane());
                        DummyGraphScenario.INSTANCE.buildProductKind3(graph);
                        Layout layout = new RandomLayout(graph);
                        layout.execute();
                    }
                    
                    
                    else if (value.equals("Customer")) {
                        graph = new Graph();
                        borderPane.setCenter(graph.getScrollPane());
                        DummyGraphScenario.INSTANCE.buildProductCustomer(graph);
                        Layout layout = new RandomLayout(graph);
                        layout.execute();
                    }
                    
                    else if (value.equals("Orders")) {
                        graph = new Graph();
                        borderPane.setCenter(graph.getScrollPane());
                        DummyGraphScenario.INSTANCE.buildProductOrders(graph);
                        Layout layout = new RandomLayout(graph);
                        layout.execute();
                    }
                    
                    else if (value.equals("Order")) {
                        graph = new Graph();
                        borderPane.setCenter(graph.getScrollPane());
                        DummyGraphScenario.INSTANCE.buildProductOrder(graph);
                        Layout layout = new RandomLayout(graph);
                        layout.execute();
                    }
                    
                    else if (value.equals("Items")) {
                        graph = new Graph();
                        borderPane.setCenter(graph.getScrollPane());
                        DummyGraphScenario.INSTANCE.buildProductItems(graph);
                        Layout layout = new RandomLayout(graph);
                        layout.execute();
                    }
                    
                    else if (value.equals("Contact")) {
                        graph = new Graph();
                        borderPane.setCenter(graph.getScrollPane());
                        DummyGraphScenario.INSTANCE.buildProductContact(graph);
                        Layout layout = new RandomLayout(graph);
                        layout.execute();
                    }
                    
                    else if (value.equals("Type")) {
                        graph = new Graph();
                        borderPane.setCenter(graph.getScrollPane());
                        DummyGraphScenario.INSTANCE.buildProductType(graph);
                        Layout layout = new RandomLayout(graph);
                        layout.execute();
                    }
                }
            }
        });
    }

    public void initialize() {

//        initComponentButton();
        initTreeView();

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

//        graph = new Graph();
//        borderPane.setCenter(graph.getScrollPane());
//        Scene scene = new Scene(root, 1024, 768);
//        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//        primaryStage.setScene(scene);
//        primaryStage.show();
//        addGraphComponents();
//        Layout layout = new RandomLayout(graph);
//        layout.execute();
        // TODO
    }

}
