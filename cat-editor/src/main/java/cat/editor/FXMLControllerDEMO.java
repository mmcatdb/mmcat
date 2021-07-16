/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor;

import cat.dummy.DummyGraphScenario;
import cat.dummy.DummyMappingScenario;
import cat.editor.view.Graph;
import cat.editor.view.Layout;
import cat.editor.view.RandomLayout;
import cat.tutorial.AddPersonDialogController;
import java.io.IOException;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
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
	private ScrollPane scrollPane;

	@FXML
	private ChoiceBox<String> zoom;

	@FXML
	private Tab diagramTab;

	@FXML
	private Tab elementTab;

	@FXML
	private Tab styleTab;

	@FXML
	private Tab textTab;

	@FXML
	private Tab positionTab;

	@FXML
	private Tab mappingTab;

	@FXML
	private Tab componentTab;

	@FXML
	private Tab instanceTab;

	@FXML
	private TextArea mappingTextArea;

	@FXML
	private TextArea ddlStatementTextArea;

	@FXML
	private TextArea statementArea;

	@FXML
	private TreeView treeView;

	@FXML
	private Button componentButton;

	@FXML
	private TabPane tabPane;

	@FXML
	private TableView instanceTable;

	@FXML
	private SplitPane splitPane;

	@FXML
	private Pagination shapesPagination;

	@FXML
	private CheckBox elementNameCheckbox;

	@FXML
	private CheckBox arrowNameCheckbox;

	@FXML
	private CheckBox elementSignatureCheckbox;

	@FXML
	private CheckBox arrowSignatureCheckbox;

	@FXML
	private CheckBox nontrivialCardinalitiesCheckbox;

	@FXML
	private CheckBox trivialCardinalitiesCheckbox;

	@FXML
	private CheckBox superidentifierCheckbox;

	@FXML
	private CheckBox idsCheckbox;

	@FXML
	private CheckBox componentsCheckbox;

	@FXML
	private CheckBox kindsCheckbox;

	private Graph graph = new Graph();

	private double backupProjectDividerPosition = 0.0;
	private double backupDetailDividerPosition = 0.0;

	private double currentProjectDividerPosition;
	private double currentDetailDividerPosition;

	@FXML
	private void openCloseProjectAction(ActionEvent event) {
		if (currentProjectDividerPosition > 0.01) {
			backupProjectDividerPosition = currentProjectDividerPosition;
			currentProjectDividerPosition = 0.0;
		} else {
			currentProjectDividerPosition = backupProjectDividerPosition;
		}

		splitPane.setDividerPosition(0, currentProjectDividerPosition);
	}

	@FXML
	private void openCloseDetailAction(ActionEvent event) {
		if (currentDetailDividerPosition < 0.99) {
			backupDetailDividerPosition = currentDetailDividerPosition;
			currentDetailDividerPosition = 1.0;
		} else {
			currentDetailDividerPosition = backupDetailDividerPosition;
		}

		splitPane.setDividerPosition(1, currentDetailDividerPosition);
	}

	@FXML
	private void handleStatementAction(ActionEvent event) {
//        System.out.println("You clicked me!");
		statementArea.setText("""
                              CREATE TABLE Customer ( id TEXT);
                              CREATE TABLE Orders (id TEXT, number TEXT);
                              CREATE TABLE Order (id TEXT, number TEXT);
                              CREATE TABLE Items (id TEXT, number TEXT, productId TEXT, quantity TEXT);
                              CREATE TABLE Product (id TEXT, name TEXT, price TEXT);
                              CREATE TABLE Contact (id TEXT, number TEXT, name TEXT, value TEXT);
                              CREATE TABLE Type (Name TEXT);""");
	}

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

	}

	private Image createImage() {
		Rectangle rect = new Rectangle(1600, 900, Color.CORNFLOWERBLUE);//.snapshot(null, null);
		rect.setFill(createGridPattern());
		return rect.snapshot(null, null);
	}

	public ImagePattern createGridPattern() {

		double w = 20;//gridSize;
		double h = 20;//gridSize;

		Canvas canvas = new Canvas(w, h);
		GraphicsContext gc = canvas.getGraphicsContext2D();

//        Line line = new Line(0,0,1,1);
		gc.setImageSmoothing(false);
		gc.setStroke(Color.GRAY.deriveColor(1, 1, 1, 0.8));
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, 1, 1);
//        gc.fillRect(0, 0, w, h);
		gc.strokeRect(0, 0, 1, 1);

//        gc.strokeRect(0, 0, w, h);
		Image image = canvas.snapshot(new SnapshotParameters(), null);

		ImagePattern pattern = new ImagePattern(image, 0, 0, w, h, false);

		return pattern;

	}

	private void selectedCategory() {
		elementNameCheckbox.setSelected(true);
		arrowNameCheckbox.setSelected(false);
		elementSignatureCheckbox.setSelected(true);
		arrowSignatureCheckbox.setSelected(true);
		nontrivialCardinalitiesCheckbox.setSelected(false);
		trivialCardinalitiesCheckbox.setSelected(false);
		superidentifierCheckbox.setSelected(false);
		idsCheckbox.setSelected(false);
		componentsCheckbox.setSelected(false);
		kindsCheckbox.setSelected(false);
	}

	private void selectedER() {
		elementNameCheckbox.setSelected(true);
		arrowNameCheckbox.setSelected(false);
		elementSignatureCheckbox.setSelected(false);
		arrowSignatureCheckbox.setSelected(false);
		nontrivialCardinalitiesCheckbox.setSelected(false);
		trivialCardinalitiesCheckbox.setSelected(false);
		superidentifierCheckbox.setSelected(false);
		idsCheckbox.setSelected(false);
		componentsCheckbox.setSelected(false);
		kindsCheckbox.setSelected(false);
	}

	private void selectEditorTabs() {
		tabPane.getTabs().add(0, diagramTab);
		tabPane.getTabs().add(1, elementTab);
		tabPane.getTabs().add(2, styleTab);
		tabPane.getTabs().add(3, textTab);
		tabPane.getTabs().add(4, positionTab);
		tabPane.getTabs().remove(mappingTab);
		tabPane.getTabs().remove(componentTab);
		tabPane.getTabs().remove(instanceTab);

		tabPane.getSelectionModel().select(diagramTab);
	}

	private void selectMappingTabs() {
		tabPane.getTabs().remove(diagramTab);
		tabPane.getTabs().remove(elementTab);
		tabPane.getTabs().remove(styleTab);
		tabPane.getTabs().remove(textTab);
		tabPane.getTabs().remove(positionTab);
		tabPane.getTabs().add(0, mappingTab);
		tabPane.getTabs().remove(componentTab);
		tabPane.getTabs().remove(instanceTab);
		tabPane.getSelectionModel().select(mappingTab);
	}

	private void selectComponentTabs() {
		tabPane.getTabs().remove(diagramTab);
		tabPane.getTabs().remove(elementTab);
		tabPane.getTabs().remove(styleTab);
		tabPane.getTabs().remove(textTab);
		tabPane.getTabs().remove(positionTab);
		tabPane.getTabs().remove(mappingTab);
		tabPane.getTabs().add(0, componentTab);
		tabPane.getTabs().remove(instanceTab);
		tabPane.getSelectionModel().select(componentTab);
	}

	private void selectInstanceTabs() {
		tabPane.getTabs().remove(diagramTab);
		tabPane.getTabs().remove(elementTab);
		tabPane.getTabs().remove(styleTab);
		tabPane.getTabs().remove(textTab);
		tabPane.getTabs().remove(positionTab);
		tabPane.getTabs().remove(mappingTab);
		tabPane.getTabs().remove(componentTab);
		tabPane.getTabs().add(0, instanceTab);
		tabPane.getSelectionModel().select(instanceTab);

	}

	private void initTreeView() {
		//Creating tree items
		TreeItem item1 = new TreeItem("ER Schema");
		TreeItem item2 = new TreeItem("Categorical Schema");
		TreeItem item3 = new TreeItem("Database Components");
		TreeItem item31 = new TreeItem("MongoDB");
		TreeItem item32 = new TreeItem("Neo4j");
		TreeItem item33 = new TreeItem("PostgreSQL");

		TreeItem item311 = new TreeItem("OrderCollection");
		TreeItem item312 = new TreeItem("Order1");
		TreeItem item313 = new TreeItem("Order2");
//		TreeItem item314 = new TreeItem("Order3");
//		TreeItem item315 = new TreeItem("Order4");
//		TreeItem item316 = new TreeItem("Order5");
		TreeItem item317 = new TreeItem("Order60");
		TreeItem item318 = new TreeItem("Order6");
		TreeItem item319 = new TreeItem("Order7");
		TreeItem item3110 = new TreeItem("Order8");

		TreeItem item321 = new TreeItem("CustomerNode");
		TreeItem item322 = new TreeItem("OrderNode");
		TreeItem item323 = new TreeItem("OrdersEdge");

		TreeItem item331 = new TreeItem("Contact");
		TreeItem item332 = new TreeItem("Customer");
		TreeItem item333 = new TreeItem("Items");
		TreeItem item334 = new TreeItem("Order");
		TreeItem item335 = new TreeItem("Orders");
		TreeItem item336 = new TreeItem("Product");
		TreeItem item337 = new TreeItem("Type");
//		TreeItem item82 = new TreeItem("Product2");
//		TreeItem item83 = new TreeItem("Product3");

		TreeItem item4 = new TreeItem("Data Migrations");
		TreeItem item5 = new TreeItem("Instance");
		TreeItem item51 = new TreeItem("MongoDB-Inst");
		TreeItem item52 = new TreeItem("Neo4j-Inst");
		TreeItem item53 = new TreeItem("PostgreSQL-Inst");

//      root3.getChildren().addAll(item7, item8, item9);
		//list View for educational qualification
		TreeItem<String> base = new TreeItem<>("MyProject");
		base.setExpanded(true);
		base.getChildren().addAll(item1, item2, item3, item4, item5);
		item3.getChildren().addAll(item31, item32, item33);
		item31.getChildren().addAll(item311, item312, item313, item317, item318, item319, item3110);
		item32.getChildren().addAll(item321, item322, item323);
		item33.getChildren().addAll(item331, item332, item333, item334, item335, item336, item337);
		item5.getChildren().addAll(item51, item52, item53);
		//Creating a TreeView item
		treeView.setRoot(base);

//      view.setPrefHeight(300);
		treeView.setOnMouseClicked(new EventHandler<>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 1) {
					TreeItem item = (TreeItem) treeView.getSelectionModel().getSelectedItem();

					String value = (String) item.getValue();

					graph = new Graph();
					scrollPane.setContent(graph.getScrollPane());

					Image image = createImage();
					ImageView view = new ImageView();
					view.setImage(image);
					view.setImage(image);
					graph.getCellLayer().getChildren().add(view);

					graph.getScrollPane().minWidthProperty().bind(Bindings.createDoubleBinding(()
							-> scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));
					graph.getScrollPane().minHeightProperty().bind(Bindings.createDoubleBinding(()
							-> scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()));

					switch (value) {
						case "ER Schema" -> {
							DummyGraphScenario.INSTANCE.buildERSchema(graph);
							selectEditorTabs();
							selectedER();
						}
						case "Categorical Schema" -> {
							DummyGraphScenario.INSTANCE.buildCategoricalSchema(graph);
							selectEditorTabs();
							selectedCategory();
						}
						case "MongoDB" -> {
							DummyGraphScenario.INSTANCE.buildMongoDB(graph);
//                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
							selectComponentTabs();
						}
						case "OrderCollection" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection(mappingTextArea);
							selectMappingTabs();
						}
						case "Order1" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection_GroupingId(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection_GroupingId(mappingTextArea);
							selectMappingTabs();
						}
						case "Order2" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection_CompleteId(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection_CompleteId(mappingTextArea);
							selectMappingTabs();
						}
//						case "Order3" -> {
//							DummyGraphScenario.INSTANCE.buildMongoOrder_3_Contact(graph);
//							DummyMappingScenario.INSTANCE.buildMongoOrder_3_Contact(mappingTextArea);
//							selectMappingTabs();
//						}
//						case "Order4" -> {
//							DummyGraphScenario.INSTANCE.buildMongoOrder_4_ContactTypeName(graph);
//							DummyMappingScenario.INSTANCE.buildMongoOrder_4_ContactTypeName(mappingTextArea);
//							selectMappingTabs();
//						}
//						case "Order5" -> {
//							DummyGraphScenario.INSTANCE.buildMongoOrder_5_ContactTypeSelectedName(graph);
//							DummyMappingScenario.INSTANCE.buildMongoOrder_5_ContactTypeSelectedName(mappingTextArea);
//							selectMappingTabs();
//						}
						case "Order60" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection_Items(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection_Items(mappingTextArea);
							selectMappingTabs();
						}
						case "Order6" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection_Items2(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection_Items2(mappingTextArea);
							selectMappingTabs();
						}
						case "Order7" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection_InliningProduct(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection_InliningProduct(mappingTextArea);
							selectMappingTabs();
						}
						case "Order8" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection_Complete(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection_Complete(mappingTextArea);
							selectMappingTabs();
						}
						case "Neo4j" -> {
							DummyGraphScenario.INSTANCE.buildNeo4j(graph);
//                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
							selectComponentTabs();
						}
						case "CustomerNode" -> {
							DummyGraphScenario.INSTANCE.buildCustomerNode(graph);
							DummyMappingScenario.INSTANCE.buildCustomerNode(mappingTextArea);
							selectMappingTabs();
						}
						case "OrderNode" -> {
							DummyGraphScenario.INSTANCE.buildOrderNode(graph);
							DummyMappingScenario.INSTANCE.buildOrderNode(mappingTextArea);
							selectMappingTabs();
						}
						case "OrdersEdge" -> {
							DummyGraphScenario.INSTANCE.buildOrdersEdge(graph);
							DummyMappingScenario.INSTANCE.buildOrdersEdge(mappingTextArea);
							selectMappingTabs();
						}

						case "PostgreSQL" -> {
							DummyGraphScenario.INSTANCE.buildPostgreSQL(graph);
//                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
							selectComponentTabs();
						}
						case "Contact" -> {
							DummyGraphScenario.INSTANCE.buildContact(graph);
							DummyMappingScenario.INSTANCE.buildContact(mappingTextArea);
							selectMappingTabs();
						}
						case "Customer" -> {
							DummyGraphScenario.INSTANCE.buildCustomer(graph);
							DummyMappingScenario.INSTANCE.buildCustomer(mappingTextArea);
							selectMappingTabs();
						}
						case "Items" -> {
							DummyGraphScenario.INSTANCE.buildItems(graph);
							DummyMappingScenario.INSTANCE.buildItems(mappingTextArea);
							selectMappingTabs();
						}
						case "Order" -> {
							DummyGraphScenario.INSTANCE.buildOrder(graph);
							DummyMappingScenario.INSTANCE.buildOrder(mappingTextArea);
							selectMappingTabs();
						}
						case "Orders" -> {
							DummyGraphScenario.INSTANCE.buildOrders(graph);
							DummyMappingScenario.INSTANCE.buildOrders(mappingTextArea);
							selectMappingTabs();
						}
						case "Product" -> {
							DummyGraphScenario.INSTANCE.buildProduct(graph);
							DummyMappingScenario.INSTANCE.buildProduct(mappingTextArea);
//							DummyGraphScenario.INSTANCE.buildProductKind2(graph);
//							DummyMappingScenario.INSTANCE.buildProductKind2(mappingTextArea);
//							DummyGraphScenario.INSTANCE.buildProductKind3(graph);
//							DummyMappingScenario.INSTANCE.buildProductKind3(mappingTextArea);
							selectMappingTabs();
						}
						case "Type" -> {
							DummyGraphScenario.INSTANCE.buildType(graph);
							DummyMappingScenario.INSTANCE.buildType(mappingTextArea);
							selectMappingTabs();
						}
						case "Data Migrations" -> {
						}
						case "Instance" -> {
						}
						case "MongoDB-Inst" -> {
							DummyGraphScenario.INSTANCE.buildMongoDBInstance(graph);
//                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
							selectInstanceTabs();
						}
						case "Neo4j-Inst" -> {
							DummyGraphScenario.INSTANCE.buildNeo4jInstance(graph);
//                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
							selectInstanceTabs();
						}
						case "PostgreSQL-Inst" -> {
							DummyGraphScenario.INSTANCE.buildPostgreSQLInstance(graph);
//                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
							selectInstanceTabs();
						}
					}
					Layout layout = new RandomLayout(graph);
					layout.execute();

				}
			}

		});
	}

	public void initialize() {

		splitPane.setDividerPosition(0, 0.2);
		splitPane.setDividerPosition(1, 0.75);

		currentProjectDividerPosition = splitPane.getDividerPositions()[0];
		currentDetailDividerPosition = splitPane.getDividerPositions()[1];

		splitPane.getDividers().get(0).positionProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				currentProjectDividerPosition = (double) t1;
			}
		});

		splitPane.getDividers().get(1).positionProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				currentDetailDividerPosition = (double) t1;
			}
		});

		initTreeView();

		zoom.setValue("100%");
		ObservableList<String> list = zoom.getItems();
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
			}
		});

	}

}
