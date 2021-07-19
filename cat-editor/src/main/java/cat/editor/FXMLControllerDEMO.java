/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor;

import cat.dummy.DummyDDLScenario;
import cat.dummy.DummyGraphScenario;
import cat.dummy.DummyMappingScenario;
import cat.editor.view.Graph;
import cat.editor.view.Layout;
import cat.editor.view.RandomLayout;
import cat.editor.view.cell.ERAttributeCell;
import cat.editor.view.cell.EREntityCell;
import cat.editor.view.cell.ERIdentifierCell;
import cat.editor.view.cell.ERRelationshipCell;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
	private ScrollPane scrollPane2;

	@FXML
	private ChoiceBox<String> zoom;

	@FXML
	private ChoiceBox<String> nameChoiceBox;

	@FXML
	private Tab diagramTab;

	@FXML
	private Tab elementTab;

	@FXML
	private Tab styleTab;

	@FXML
	private Tab textTab;

	@FXML
	private Tab documentTab;

	@FXML
	private Tab graphTab;

	@FXML
	private Tab positionTab;

	@FXML
	private Tab mappingTab;

	@FXML
	private Tab componentTab;

	@FXML
	private Tab instanceTab;

	@FXML
	private Tab accessPathTab;

	@FXML
	private Tab erTab;

	@FXML
	private Tab ddlTab;

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
	private TabPane mainTabPane;

	@FXML
	private TabPane secondaryTabPane;

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

	@FXML
	private TextField nameTextField;

	@FXML
	private TableView kindsTable;

	@FXML
	private ChoiceBox signatureChoiceBox;

	@FXML
	private ChoiceBox usernameChoiceBox;

	@FXML
	private ChoiceBox elementChoiceBox;

	@FXML
	private Tab diagramMainTab;

	@FXML
	private Tab instanceMainTab;

	private Graph graph = new Graph();
	private Graph graph2 = new Graph();

	private double backupProjectDividerPosition = 0.0;
	private double backupDetailDividerPosition = 0.0;

	private double currentProjectDividerPosition;
	private double currentDetailDividerPosition;

	@FXML
	private void magicButton(ActionEvent event) {
		secondaryTabPane.setPrefWidth(500);
		mainTabPane.setPrefWidth(426);

//		var children = parent.getChildren();
//		children.clear();
//
//		TabPane erTabPane = new TabPane();
//		Tab erTab = new Tab();
//		AnchorPane anchor = new AnchorPane();
//		erTab.setContent(anchor);
//
//		ScrollPane scrollPane2 = new ScrollPane();
//		anchor.getChildren().add(scrollPane2);
//
//		AnchorPane.setTopAnchor(scrollPane2, 0.0);
//		AnchorPane.setLeftAnchor(scrollPane2, 0.0);
//		AnchorPane.setRightAnchor(scrollPane2, 0.0);
//		AnchorPane.setBottomAnchor(scrollPane2, 0.0);
		graph2 = new Graph();
		scrollPane2.setContent(graph2.getScrollPane());

		Image image = createImage();
		ImageView view = new ImageView();
		view.setImage(image);
		graph2.getCellLayer().getChildren().add(view);

		graph2.getScrollPane().minWidthProperty().bind(Bindings.createDoubleBinding(()
				-> scrollPane2.getViewportBounds().getWidth(), scrollPane2.viewportBoundsProperty()));
		graph2.getScrollPane().minHeightProperty().bind(Bindings.createDoubleBinding(()
				-> scrollPane2.getViewportBounds().getHeight(), scrollPane2.viewportBoundsProperty()));

		DummyGraphScenario.INSTANCE.buildERSchema(graph2);
//							selectEditorTabs();
//							selectedER();
//							initERPalette();
//		secondaryTabPane.getTabs().add(0, erTab);
		secondaryTabPane.getTabs().get(0).setText("ER Schema");
//		erTabPane.getTabs().remove(instanceMainTab);
//		erTabPane.getTabs().remove(documentTab);
//		erTabPane.getTabs().remove(graphTab);
		Layout layout = new RandomLayout(graph2);
		layout.execute();

//		HBox box = new HBox();
//		box.setPrefWidth(Region.USE_COMPUTED_SIZE);
//		box.setPrefHeight(Region.USE_COMPUTED_SIZE);
//		box.getChildren().add(erTabPane);
//		box.getChildren().add(mainTabPane);
//
//		erTabPane.setPrefWidth(500);
//		erTabPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
//		mainTabPane.setPrefWidth(426);
//		mainTabPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
//
////		box.getChildren().add(mainTabPane);
//		boolean add = children.add(box);
//		System.out.println("added: " + add);
	}

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
                              CREATE TABLE Customer (
                              id TEXT
                              );
                        
                              CREATE TABLE Contact (
                              id TEXT,
                              number TEXT,
                              name TEXT,
                              value TEXT
                              );
                   
                              CREATE TABLE Orders (
                              id TEXT,
                              number TEXT,
                              items JSONB
                              );
                                                
                        CREATE TABLE Type (
                              Name TEXT
                              );
                        """);
	}

	@FXML
	void onOpenDialog(ActionEvent event) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("dialogDatabaseComponent.fxml"));
		Parent parent = fxmlLoader.load();
		FXMLControllerAddDatabaseDialog dialogController = fxmlLoader.<FXMLControllerAddDatabaseDialog>getController();
//        dialogController.setAppMainObservableList(tvObservableList);

		Scene scene = new Scene(parent, 480, 428);
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

	private void initCategoryPalette() {
		shapesPagination.setPageCount(2);
		shapesPagination.setCurrentPageIndex(0);
		shapesPagination.setMaxPageIndicatorCount(2);

		shapesPagination.setPageFactory((pageIndex) -> {

			Palette palette = new Palette();

			palette.getChildren().add(new Rectangle(288, 200, Color.WHITE));
//			palette.addElement(new EREntityCell("Entity Type", "", 50, 50));

			return palette;
		});
	}

	private void initERPalette() {
		shapesPagination.setPageCount(6);
		shapesPagination.setCurrentPageIndex(0);
		shapesPagination.setMaxPageIndicatorCount(6);

		shapesPagination.setPageFactory((pageIndex) -> {

			Palette palette = new Palette();

			palette.getChildren().add(new Rectangle(288, 200, Color.WHITE));

			palette.addElement(new EREntityCell("Entity Type", "Entity", 30, 22));
			palette.addElement(new ERRelationshipCell("Relationship Type", "Relationship", 174, 17));
			palette.addElement(new ERAttributeCell("Attribute", "Attribute", 178, 100));
			palette.addElement(new ERIdentifierCell("Identifier", "Identifier", 38, 100));

			return palette;
		});
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
		nontrivialCardinalitiesCheckbox.setSelected(true);
		trivialCardinalitiesCheckbox.setSelected(true);
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
		tabPane.getTabs().remove(accessPathTab);
		tabPane.getTabs().remove(ddlTab);

		tabPane.getSelectionModel().select(diagramTab);
	}

	private void selectMappingTabs() {
		tabPane.getTabs().add(0, diagramTab);
		tabPane.getTabs().remove(elementTab);
		tabPane.getTabs().remove(styleTab);
		tabPane.getTabs().remove(textTab);
		tabPane.getTabs().remove(positionTab);
		tabPane.getTabs().add(1, mappingTab);
		tabPane.getTabs().remove(componentTab);
		tabPane.getTabs().remove(instanceTab);
		tabPane.getTabs().add(2, accessPathTab);
		tabPane.getTabs().add(3, ddlTab);

		tabPane.getSelectionModel().select(accessPathTab);
	}

	private void selectDDLTabs() {
		tabPane.getTabs().add(0, diagramTab);
		tabPane.getTabs().remove(elementTab);
		tabPane.getTabs().remove(styleTab);
		tabPane.getTabs().remove(textTab);
		tabPane.getTabs().remove(positionTab);
		tabPane.getTabs().add(1, mappingTab);
		tabPane.getTabs().remove(componentTab);
		tabPane.getTabs().remove(instanceTab);
		tabPane.getTabs().add(2, accessPathTab);
		tabPane.getTabs().add(3, ddlTab);

		tabPane.getSelectionModel().select(ddlTab);
	}

	private void selectComponentTabs() {
		tabPane.getTabs().add(0, diagramTab);
		tabPane.getTabs().remove(elementTab);
		tabPane.getTabs().remove(styleTab);
		tabPane.getTabs().remove(textTab);
		tabPane.getTabs().remove(positionTab);
		tabPane.getTabs().remove(mappingTab);
		tabPane.getTabs().add(1, componentTab);
		tabPane.getTabs().remove(instanceTab);
		tabPane.getTabs().remove(accessPathTab);
		tabPane.getTabs().remove(ddlTab);

		tabPane.getSelectionModel().select(componentTab);
	}

	private void selectInstanceTabs() {
		tabPane.getTabs().add(0, diagramTab);
		tabPane.getTabs().remove(elementTab);
		tabPane.getTabs().remove(styleTab);
		tabPane.getTabs().remove(textTab);
		tabPane.getTabs().remove(positionTab);
		tabPane.getTabs().remove(mappingTab);
		tabPane.getTabs().remove(componentTab);
		tabPane.getTabs().add(1, instanceTab);
		tabPane.getTabs().remove(accessPathTab);
		tabPane.getTabs().remove(ddlTab);
		tabPane.getSelectionModel().select(instanceTab);

	}

	private void initTreeView() {
		//Creating tree items
		TreeItem item1 = new TreeItem("ER Schema");
		TreeItem item2 = new TreeItem("Categorical Schema");
		TreeItem item3 = new TreeItem("Database Components");
		TreeItem item31 = new TreeItem("MongoDB");
//		TreeItem item32 = new TreeItem("Neo4j");
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

//		TreeItem item321 = new TreeItem("CustomerNode");
//		TreeItem item322 = new TreeItem("OrderNode");
//		TreeItem item323 = new TreeItem("OrdersEdge");
		TreeItem item331 = new TreeItem("Contact");
		TreeItem item332 = new TreeItem("Customer");
//		TreeItem item333 = new TreeItem("Items");
//		TreeItem item334 = new TreeItem("Order");
		TreeItem item335 = new TreeItem("Orders");
		TreeItem item3351 = new TreeItem("Orders1");
		TreeItem item3352 = new TreeItem("Orders2");
		TreeItem item3353 = new TreeItem("Orders3");
		TreeItem item3354 = new TreeItem("Orders4");
		TreeItem item3355 = new TreeItem("Orders5");
//		TreeItem item336 = new TreeItem("Product");
		TreeItem item337 = new TreeItem("Type");
//		TreeItem item82 = new TreeItem("Product2");
//		TreeItem item83 = new TreeItem("Product3");

		TreeItem item4 = new TreeItem("Data Migrations");
		TreeItem item5 = new TreeItem("Instance");
		TreeItem item51 = new TreeItem("MongoDB-Inst");
//		TreeItem item52 = new TreeItem("Neo4j-Inst");
		TreeItem item53 = new TreeItem("PostgreSQL-Inst");

//      root3.getChildren().addAll(item7, item8, item9);
		//list View for educational qualification
		TreeItem<String> base = new TreeItem<>("MyProject");
		base.setExpanded(true);
		base.getChildren().addAll(item1, item2, item3, item4, item5);
		item3.getChildren().addAll(item31, /*item32,*/ item33);
		item31.getChildren().addAll(item311, item312, item313, item317, item318, item319, item3110);
//		item32.getChildren().addAll(item321, item322, item323);
		item33.getChildren().addAll(item331, item332, /*item333, item334,*/ item335, item3351, item3352, item3353, item3354, item3355, /*item336,*/ item337);
		item5.getChildren().addAll(item51, /*item52,*/ item53);
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
							initERPalette();
							mainTabPane.getTabs().get(0).setText("ER Schema");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Categorical Schema" -> {
							DummyGraphScenario.INSTANCE.buildCategoricalSchema(graph);
							selectEditorTabs();
							selectedCategory();
							initCategoryPalette();
							mainTabPane.getTabs().get(0).setText("Categorical Schema");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "MongoDB" -> {
							DummyGraphScenario.INSTANCE.buildMongoDB(graph);
//                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(statementArea);
							selectComponentTabs();
							mainTabPane.getTabs().get(0).setText("MongoDB");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "OrderCollection" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							selectMappingTabs();
							mainTabPane.getTabs().get(0).setText("OrderCollection");
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Order1" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection_GroupingId(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection_GroupingId(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							selectMappingTabs();
							mainTabPane.getTabs().get(0).setText("OrderCollection");
							nameChoiceBox.setValue("User Defined");
							nameTextField.setDisable(false);
							nameTextField.setText("_id");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Order2" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection_CompleteId(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection_CompleteId(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							selectMappingTabs();
							mainTabPane.getTabs().get(0).setText("OrderCollection");
							nameChoiceBox.setValue("User Defined");
							nameTextField.setDisable(false);
							nameTextField.setText("number");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
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
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							selectMappingTabs();
							mainTabPane.getTabs().get(0).setText("OrderCollection");
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Order6" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection_Items2(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection_Items2(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							selectMappingTabs();
							mainTabPane.getTabs().get(0).setText("OrderCollection");
							nameChoiceBox.setValue("User Defined");
							nameTextField.setDisable(false);
							nameTextField.setText("items");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Order7" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection_InliningProduct(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection_InliningProduct(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							selectMappingTabs();
							mainTabPane.getTabs().get(0).setText("OrderCollection");
							nameChoiceBox.setValue("User Defined");
							nameTextField.setDisable(false);
							nameTextField.setText("id");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);

						}
						case "Order8" -> {
							DummyGraphScenario.INSTANCE.buildOrderCollection_Complete(graph);
							DummyMappingScenario.INSTANCE.buildOrderCollection_Complete(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							selectMappingTabs();
							mainTabPane.getTabs().get(0).setText("OrderCollection");
							nameChoiceBox.setDisable(true);
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
//						case "Neo4j" -> {
//							DummyGraphScenario.INSTANCE.buildNeo4j(graph);
////                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
//							selectComponentTabs();
//							mainTabPane.getTabs().get(0).setText("Neo4j");
//							mainTabPane.getTabs().remove(instanceMainTab);
//							mainTabPane.getTabs().remove(documentTab);
//							mainTabPane.getTabs().remove(graphTab);
//						}
//						case "CustomerNode" -> {
//							DummyGraphScenario.INSTANCE.buildCustomerNode(graph);
//							DummyMappingScenario.INSTANCE.buildCustomerNode(mappingTextArea);
//							selectMappingTabs();
//							mainTabPane.getTabs().get(0).setText("CustomerNode");
//							nameChoiceBox.setDisable(true);
//							nameChoiceBox.setValue("Inherit");
//							nameTextField.setDisable(true);
//							nameTextField.setText("");
//							mainTabPane.getTabs().remove(instanceMainTab);
//							mainTabPane.getTabs().remove(documentTab);
//							mainTabPane.getTabs().remove(graphTab);
//						}
//						case "OrderNode" -> {
//							DummyGraphScenario.INSTANCE.buildOrderNode(graph);
//							DummyMappingScenario.INSTANCE.buildOrderNode(mappingTextArea);
//							selectMappingTabs();
//							mainTabPane.getTabs().get(0).setText("OrderNode");
//							nameChoiceBox.setDisable(true);
//							nameChoiceBox.setValue("Inherit");
//							nameTextField.setDisable(true);
//							nameTextField.setText("");
//							mainTabPane.getTabs().remove(instanceMainTab);
//							mainTabPane.getTabs().remove(documentTab);
//							mainTabPane.getTabs().remove(graphTab);
//						}
//						case "OrdersEdge" -> {
//							DummyGraphScenario.INSTANCE.buildOrdersEdge(graph);
//							DummyMappingScenario.INSTANCE.buildOrdersEdge(mappingTextArea);
//							selectMappingTabs();
//							mainTabPane.getTabs().get(0).setText("OrdersEdge");
//							nameChoiceBox.setDisable(true);
//							nameChoiceBox.setValue("Inherit");
//							nameTextField.setDisable(true);
//							nameTextField.setText("");
//							mainTabPane.getTabs().remove(instanceMainTab);
//							mainTabPane.getTabs().remove(documentTab);
//							mainTabPane.getTabs().remove(graphTab);
//						}

						case "PostgreSQL" -> {
							DummyGraphScenario.INSTANCE.buildPostgreSQL(graph);
							DummyDDLScenario.INSTANCE.createPostgreSQLKinds(ddlStatementTextArea);
//                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
							selectComponentTabs();
							mainTabPane.getTabs().get(0).setText("PostgreSQL");
							nameChoiceBox.setDisable(true);
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Contact" -> {
							DummyGraphScenario.INSTANCE.buildContact(graph);
							DummyMappingScenario.INSTANCE.buildContact(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildContact(ddlStatementTextArea);
							selectDDLTabs();
							mainTabPane.getTabs().get(0).setText("Contact");
							nameChoiceBox.setDisable(true);
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Customer" -> {
							DummyGraphScenario.INSTANCE.buildCustomer(graph);
							DummyMappingScenario.INSTANCE.buildCustomer(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildCustomer(ddlStatementTextArea);
							selectDDLTabs();
							mainTabPane.getTabs().get(0).setText("Customer");
							nameChoiceBox.setDisable(true);
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
//						case "Items" -> {
//							DummyGraphScenario.INSTANCE.buildItems(graph);
//							DummyMappingScenario.INSTANCE.buildItems(mappingTextArea);
//							DummyDDLScenario.INSTANCE.buildItems(ddlStatementTextArea);
//							selectMappingTabs();
//							mainTabPane.getTabs().get(0).setText("Items");
//							nameChoiceBox.setDisable(true);
//							nameChoiceBox.setValue("Inherit");
//							nameTextField.setDisable(true);
//							nameTextField.setText("");
//							mainTabPane.getTabs().remove(instanceMainTab);
//							mainTabPane.getTabs().remove(documentTab);
//							mainTabPane.getTabs().remove(graphTab);
//						}
//						case "Order" -> {
//							DummyGraphScenario.INSTANCE.buildOrder(graph);
//							DummyMappingScenario.INSTANCE.buildOrder(mappingTextArea);
//							DummyDDLScenario.INSTANCE.buildOrder(mappingTextArea);
//							selectMappingTabs();
//							mainTabPane.getTabs().get(0).setText("Order");
//							nameChoiceBox.setDisable(true);
//							nameChoiceBox.setValue("Inherit");
//							nameTextField.setDisable(true);
//							nameTextField.setText("");
//							mainTabPane.getTabs().remove(instanceMainTab);
//							mainTabPane.getTabs().remove(documentTab);
//							mainTabPane.getTabs().remove(graphTab);
//						}
						case "Orders" -> {
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_0(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_0(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_0(ddlStatementTextArea);
							selectDDLTabs();
							mainTabPane.getTabs().get(0).setText("Orders");
							nameChoiceBox.setDisable(true);
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Orders1" -> {
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_1(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_1(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_1(ddlStatementTextArea);
							selectDDLTabs();
							mainTabPane.getTabs().get(0).setText("Orders");
							nameChoiceBox.setDisable(true);
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Orders2" -> {
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_2(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_2(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_2(ddlStatementTextArea);
							selectDDLTabs();
							mainTabPane.getTabs().get(0).setText("Orders");
							nameChoiceBox.setDisable(true);
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Orders3" -> {
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_3(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_3(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_3(ddlStatementTextArea);
							selectDDLTabs();
							mainTabPane.getTabs().get(0).setText("Orders");
							nameChoiceBox.setDisable(true);
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Orders4" -> {
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_4(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_4(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_4(ddlStatementTextArea);
							selectDDLTabs();
							mainTabPane.getTabs().get(0).setText("Orders");
							nameChoiceBox.setDisable(true);
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Orders5" -> {
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_5(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_5(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_5(ddlStatementTextArea);
							selectDDLTabs();
							mainTabPane.getTabs().get(0).setText("Orders");
							nameChoiceBox.setDisable(true);
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
//						case "Product" -> {
//							DummyGraphScenario.INSTANCE.buildProduct(graph);
//							DummyMappingScenario.INSTANCE.buildProduct(mappingTextArea);
//							DummyDDLScenario.INSTANCE.buildProduct(mappingTextArea);
////							DummyGraphScenario.INSTANCE.buildProductKind2(graph);
////							DummyMappingScenario.INSTANCE.buildProductKind2(mappingTextArea);
////							DummyGraphScenario.INSTANCE.buildProductKind3(graph);
////							DummyMappingScenario.INSTANCE.buildProductKind3(mappingTextArea);
//							selectMappingTabs();
//							mainTabPane.getTabs().get(0).setText("Product");
//							nameChoiceBox.setDisable(true);
//							nameChoiceBox.setValue("Inherit");
//							nameTextField.setDisable(true);
//							nameTextField.setText("");
//							mainTabPane.getTabs().remove(instanceMainTab);
//							mainTabPane.getTabs().remove(documentTab);
//							mainTabPane.getTabs().remove(graphTab);
//						}
						case "Type" -> {
							DummyGraphScenario.INSTANCE.buildType(graph);
							DummyMappingScenario.INSTANCE.buildType(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildType(mappingTextArea);
							selectMappingTabs();
							mainTabPane.getTabs().get(0).setText("Type");
							nameChoiceBox.setDisable(true);
							nameChoiceBox.setValue("Inherit");
							nameTextField.setDisable(true);
							nameTextField.setText("");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Data Migrations" -> {
							mainTabPane.getTabs().get(0).setText("Data Migrations");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "Instance" -> {
							mainTabPane.getTabs().get(0).setText("Instance");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);
						}
						case "MongoDB-Inst" -> {
							DummyGraphScenario.INSTANCE.buildMongoDB(graph);
							mainTabPane.getTabs().get(0).setText("MongoDB");
//                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
							selectInstanceTabs();
							mainTabPane.getTabs().add(1, documentTab);
							instanceMainTab.setText("OrderCollection");
							mainTabPane.getTabs().remove(instanceMainTab);
							mainTabPane.getTabs().remove(graphTab);

						}
//						case "Neo4j-Inst" -> {
//							DummyGraphScenario.INSTANCE.buildNeo4j(graph);
//							mainTabPane.getTabs().get(0).setText("Neo4j");
////							DummyGraphScenario.INSTANCE.buildNeo4jInstance(graph);
////                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
//							selectInstanceTabs();
//							mainTabPane.getTabs().add(1, graphTab);
//							instanceMainTab.setText("Graph");
//							mainTabPane.getTabs().remove(documentTab);
//							mainTabPane.getTabs().remove(instanceMainTab);
//						}
						case "PostgreSQL-Inst" -> {
							DummyGraphScenario.INSTANCE.buildPostgreSQL(graph);
							mainTabPane.getTabs().get(0).setText("PostgreSQL");
//                            DummyMappingScenario.INSTANCE.buildMongoOrder_8_Complete(mappingTextArea);
							selectInstanceTabs();
							mainTabPane.getTabs().add(1, instanceMainTab);
							instanceMainTab.setText("Contact");
							mainTabPane.getTabs().remove(documentTab);
							mainTabPane.getTabs().remove(graphTab);

						}
					}
					Layout layout = new RandomLayout(graph);
					layout.execute();

				}
			}

		});
	}

	public void initialize() {

		splitPane.setDividerPosition(0, 0.15);
		splitPane.setDividerPosition(1, 0.75);

		currentProjectDividerPosition = splitPane.getDividerPositions()[0];
		currentDetailDividerPosition = splitPane.getDividerPositions()[1];

		splitPane.getDividers().get(0).positionProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				currentProjectDividerPosition = (double) t1;
				System.out.println("PROJECT: " + currentProjectDividerPosition + ":::" + t);
			}
		});

		splitPane.getDividers().get(1).positionProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				currentDetailDividerPosition = (double) t1;
				System.out.println("DETAIL: " + currentDetailDividerPosition + ":::" + t);
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

		nameChoiceBox.setValue("Inherit");
		ObservableList<String> nameList = nameChoiceBox.getItems();
		nameList.add("User Defined");
		nameList.add("Inherit");
		nameList.add("Dynamic");

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
