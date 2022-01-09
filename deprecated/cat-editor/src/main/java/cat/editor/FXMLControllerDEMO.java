package cat.editor;

import cat.mockup.scenario.DummyDDLScenario;
import cat.mockup.scenario.DummyGraphScenario;
import cat.mockup.scenario.DummyMappingScenario;
import cat.dummy.entity.Contact;
import cat.dummy.entity.Kind;
import cat.dummy.entity.MigratedKind;
import cat.editor.view.Graph;
import cat.editor.view.Layout;
import cat.editor.view.RandomLayout;
import cat.editor.view.cell.ERAttributeCell;
import cat.editor.view.cell.EREntityCell;
import cat.editor.view.cell.ERIdentifierCell;
import cat.editor.view.cell.ERRelationshipCell;
import cat.mockup.scenario.DummyObservableData;
import cat.utils.Constants;
import java.io.IOException;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
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
import javafx.util.Callback;

/**
 *
 * @author pavel.koupil
 */
public class FXMLControllerDEMO {

	private static final Logger LOGGER = Logger.getLogger(FXMLControllerDEMO.class.getName());

//    @FXML
//    private Label label;
	@FXML
	private ScrollPane scrollPane;

	@FXML
	private ScrollPane scrollPane2;

	@FXML
	private ScrollPane scrollPane3;

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
	private Tab migrationPrimaryTab;

//	@FXML
//	private Tab graphTab;
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
	private Tab secondTab;

//	@FXML
//	private Tab ddlTab;
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

	@FXML
	private ChoiceBox sourceBox;

	@FXML
	private ChoiceBox targetBox;

	@FXML
	private TableView sourceView;

	@FXML
	private TableView targetView;

	private Graph graph = new Graph();
	private Graph graph2 = new Graph();
	private Graph graph3 = new Graph();

	private double backupProjectDividerPosition = 0.0;
	private double backupDetailDividerPosition = 0.0;

	private double currentProjectDividerPosition;
	private double currentDetailDividerPosition;

	@FXML
	private void magicButton(ActionEvent event) {
//		secondaryTabPane.setPrefWidth(500);
//		mainTabPane.setPrefWidth(426);

//		graph2.getScrollPane().zoomTo(0.75);
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
	private void handleICAction(ActionEvent event) {
		statementArea.setText("""
                        ALTER TABLE contact
                          ADD FOREIGN KEY (id, number)
                            REFERENCES orders(id, number);
                        
                        ALTER TABLE contact 
                          ADD FOREIGN KEY (name)
                            REFERENCES type(name);
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

	private void initMongoDBTableView() {
		ObservableList<Kind> mongoDBKinds
				= FXCollections.observableArrayList(
						new Kind("orders", "111", "No", "No")
				);
		kindsTable.setItems(mongoDBKinds);
	}

	private void initTreeView() {
		treeView.setRoot(MOCKUP.buildProjectTree());

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
							MOCKUP.selectPrimaryTabPane_Diagram("ER Schema");
							DummyGraphScenario.INSTANCE.buildERSchema(graph);
							MOCKUP.selectEditorTabs();
							selectedER();
							initERPalette();

						}
						case "Categorical Schema" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Categorical Schema");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildCategoricalSchema(graph);
							MOCKUP.selectEditorTabs();

							selectedER();
							initERPalette();
						}
						case "MongoDB" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("MongoDB");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildMongoDB(graph);
							DummyDDLScenario.INSTANCE.createMongoKinds(statementArea);
							MOCKUP.selectComponentTabs();
							initMongoDBTableView();
						}
						case "OrdersM1" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildMongoDBOrder_0(graph);
							DummyMappingScenario.INSTANCE.buildMongoDBOrder_0(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "Inherit", true, "");
						}
						case "OrdersM2" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildMongoDBOrder_1(graph);
							DummyMappingScenario.INSTANCE.buildMongoDBOrder_1(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(true, "User Defined", false, "_id");
						}
						case "OrdersM3" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildMongoDBOrder_2(graph);
							DummyMappingScenario.INSTANCE.buildMongoDBOrder_2(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "User Defined", false, "_id");
						}
						case "OrdersM4" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildMongoDBOrder_3(graph);
							DummyMappingScenario.INSTANCE.buildMongoDBOrder_3(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "User Defined", false, "_id");
						}
						case "OrdersM5" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildMongoDBOrder_4(graph);
							DummyMappingScenario.INSTANCE.buildMongoDBOrder_4(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "User Defined", false, "number");
						}
						case "OrdersM6" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildMongoDBOrder_5(graph);
							DummyMappingScenario.INSTANCE.buildMongoDBOrder_5(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "Dynamic", true, "");
						}
						case "OrdersM7" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildMongoDBOrder_6(graph);
							DummyMappingScenario.INSTANCE.buildMongoDBOrder_6(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "User Defined", false, "items");
						}
						case "OrdersM8" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildMongoDBOrder_7(graph);
							DummyMappingScenario.INSTANCE.buildMongoDBOrder_7(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "User Defined", false, "id");
						}
						case "Orders" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildMongoDBOrder_8(graph);
							DummyMappingScenario.INSTANCE.buildMongoDBOrder_8(mappingTextArea);
							DummyDDLScenario.INSTANCE.createMongoKinds(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(true, "Inherit", true, "");
						}
						case "PostgreSQL" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("PostgreSQL");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildPostgreSQL(graph);
							DummyDDLScenario.INSTANCE.createPostgreSQLKinds(ddlStatementTextArea);
							MOCKUP.selectComponentTabs();
							DummyObservableData.INSTANCE.setItemsPostgreSQLKinds(kindsTable);
							MOCKUP.buildMappingTab_name(false, "Inherit", true, "");
						}
						case "Contact" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Contact");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildContact(graph);
							DummyMappingScenario.INSTANCE.buildContact(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildContact(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "Inherit", true, "");
						}
						case "Customer" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Customer");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildCustomer(graph);
							DummyMappingScenario.INSTANCE.buildCustomer(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildCustomer(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "Inherit", true, "");
						}
						case "Orders1" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_0(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_0(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_0(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "Inherit", true, "");
						}
						case "Orders2" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_1(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_1(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_1(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "Inherit", true, "");
						}
						case "Orders3" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_2(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_2(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_2(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "Inherit", true, "");
						}
						case "Orders4" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_3(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_3(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_3(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "Inherit", true, "");
						}
						case "Orders5" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_4(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_4(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_4(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "Inherit", true, "");
						}
						case "Orders_WARNING_TODO!" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Orders");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildPostgreSQLOrder_5(graph);
							DummyMappingScenario.INSTANCE.buildPostgreSQLOrder_5(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildPostgreSQLOrder_5(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "Inherit", true, "");
						}
						case "Type" -> {
							MOCKUP.selectPrimaryTabPane_Diagram("Type");
							MOCKUP.selectSecondaryTabPane_ER();
							DummyGraphScenario.INSTANCE.buildType(graph);
							DummyMappingScenario.INSTANCE.buildType(mappingTextArea);
							DummyDDLScenario.INSTANCE.buildType(ddlStatementTextArea);
							MOCKUP.selectMappingTabs();
							MOCKUP.buildMappingTab_name(false, "Inherit", true, "");
						}
						case "Data Migrations" -> {

						}
						case "My Migration" -> {
							MOCKUP.selectPrimaryTabPane_Migration("My Migration");
							MOCKUP.selectSecondaryTabPane_Migration();
							MOCKUP.selectComponentTabs();
							DummyDDLScenario.INSTANCE.createPostgreSQLKinds(ddlStatementTextArea);
							DummyObservableData.INSTANCE.setItemsPostgreSQLKinds(kindsTable);
						}
						case "Instance" -> {
							MOCKUP.selectPrimaryTabPane_Instance("Instance");
						}
						case "PostgreSQL-Inst" -> {
							MOCKUP.selectPrimaryTabPane_Instance("Contact");
							MOCKUP.selectSecondaryTabPane_PostgreSQLInstance();
							DummyGraphScenario.INSTANCE.buildPostgreSQL(graph);
							MOCKUP.selectInstanceTabs();
							MOCKUP.buildContactInstanceTableView();

						}
					}
					Layout layout = new RandomLayout(graph);
					layout.execute();

				}
			}

		});
	}

	public void initialize() {
		MOCKUP.mockupMigrationSource();
		MOCKUP.mockupMigrationTarget();

		TableColumn nameColumn = (TableColumn) kindsTable.getColumns().get(0);
		TableColumn rootColumn = (TableColumn) kindsTable.getColumns().get(1);
		TableColumn modifiedColumn = (TableColumn) kindsTable.getColumns().get(2);
		TableColumn materializedColumn = (TableColumn) kindsTable.getColumns().get(3);

		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		rootColumn.setCellValueFactory(new PropertyValueFactory<>("root"));
		modifiedColumn.setCellValueFactory(new PropertyValueFactory<>("modified"));
		materializedColumn.setCellValueFactory(new PropertyValueFactory<>("materialized"));

		currentProjectDividerPosition = splitPane.getDividerPositions()[0];
		currentDetailDividerPosition = splitPane.getDividerPositions()[2];

		splitPane.getDividers().get(0).positionProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				currentProjectDividerPosition = (double) t1;
//				System.out.println("PROJECT: " + currentProjectDividerPosition + ":::" + t);
			}
		});

		splitPane.getDividers().get(1).positionProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				currentDetailDividerPosition = (double) t1;
//				System.out.println("DETAIL: " + currentDetailDividerPosition + ":::" + t);
			}
		});

		initTreeView();

		zoom.setValue("100%");
		DummyObservableData.INSTANCE.setZoomItems(zoom);

		nameChoiceBox.setValue(Constants.PropertyName.INHERIT.name());
		ObservableList<String> nameList = nameChoiceBox.getItems();
		nameList.add(Constants.PropertyName.USER_DEFINED.name());
		nameList.add(Constants.PropertyName.INHERIT.name());
		nameList.add(Constants.PropertyName.DYNAMIC.name());

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

	private final Mockup MOCKUP = new Mockup();

	private class Mockup {

		private void mockupMigrationSource() {
			sourceBox.setValue("PostgreSQL");
			ObservableList<String> items = sourceBox.getItems();
			items.add("MongoDB");
			items.add("PostgreSQL");

			TableColumn<MigratedKind, String> nameColumn = (TableColumn) sourceView.getColumns().get(1);
			TableColumn<MigratedKind, String> rootColumn = (TableColumn) sourceView.getColumns().get(2);
			TableColumn<MigratedKind, Boolean> checkedColumn = (TableColumn) sourceView.getColumns().get(0);

			checkedColumn.setCellValueFactory(
					new Callback<CellDataFeatures<MigratedKind, Boolean>, ObservableValue<Boolean>>() {
				@Override
				public ObservableValue<Boolean> call(CellDataFeatures<MigratedKind, Boolean> param) {
					return param.getValue().getCheckedProperty();
				}
			});

			checkedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkedColumn));

			nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
			rootColumn.setCellValueFactory(new PropertyValueFactory<>("root"));

			ObservableList<MigratedKind> postgreSQLBKinds
					= FXCollections.observableArrayList(
							new MigratedKind(true, "Contact", "113"),
							new MigratedKind(false, "Customer", "100"),
							new MigratedKind(true, "Orders", "111"),
							new MigratedKind(false, "Type", "114")
					);
			sourceView.setItems(postgreSQLBKinds);
		}

		private void mockupMigrationTarget() {
			targetBox.setValue("MongoDB");
			ObservableList<String> items = targetBox.getItems();
			items.add("MongoDB");
			items.add("PostgreSQL");

			TableColumn nameColumn = (TableColumn) targetView.getColumns().get(1);
			TableColumn rootColumn = (TableColumn) targetView.getColumns().get(2);
			TableColumn checkedColumn = (TableColumn) targetView.getColumns().get(0);

			checkedColumn.setCellValueFactory(
					new Callback<CellDataFeatures<MigratedKind, Boolean>, ObservableValue<Boolean>>() {
				@Override
				public ObservableValue<Boolean> call(CellDataFeatures<MigratedKind, Boolean> param) {
					return param.getValue().getCheckedProperty();
				}
			});

			checkedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkedColumn));

			nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
			rootColumn.setCellValueFactory(new PropertyValueFactory<>("root"));

			ObservableList<MigratedKind> postgreSQLBKinds
					= FXCollections.observableArrayList(
							new MigratedKind(true, "Orders", "113")
					);
			targetView.setItems(postgreSQLBKinds);
		}

		private void selectPrimaryTabPane_Diagram(String name) {
			mainTabPane.getTabs().clear();

			mainTabPane.getTabs().add(0, diagramMainTab);
			mainTabPane.getTabs().get(0).setText(name);
		}

		private void selectPrimaryTabPane_Instance(String name) {
			mainTabPane.getTabs().clear();

			mainTabPane.getTabs().add(0, instanceMainTab);
			mainTabPane.getTabs().get(0).setText(name);
		}

		private void selectPrimaryTabPane_Migration(String name) {
			mainTabPane.getTabs().clear();

			mainTabPane.getTabs().add(0, migrationPrimaryTab);
			mainTabPane.getTabs().get(0).setText(name);
		}

		private void selectSecondaryTabPane_ER() {
			buildDiagramTab("ER Schema", graph2, scrollPane2, erTab);
			secondaryTabPane.getTabs().clear();
			secondaryTabPane.getTabs().add(0, erTab);
		}

		private void selectSecondaryTabPane_PostgreSQLInstance() {
			buildDiagramTab("PostgreSQL-Inst", graph2, scrollPane2, erTab);

			secondaryTabPane.getTabs().clear();
			secondaryTabPane.getTabs().add(0, erTab);
		}

		private void selectSecondaryTabPane_Migration() {
			buildDiagramTab("MongoDB", graph2, scrollPane2, erTab);
			buildDiagramTab("PostgreSQL", graph3, scrollPane3, secondTab);

			secondaryTabPane.getTabs().clear();
			secondaryTabPane.getTabs().add(0, erTab);
			secondaryTabPane.getTabs().add(1, secondTab);
		}

		private void buildDiagramTab(String name, Graph graph, ScrollPane scrollPane, Tab tab) {
			scrollPane.setContent(graph.getScrollPane());

			Image image = createImage();
			ImageView view = new ImageView();
			view.setImage(image);
			graph.getCellLayer().getChildren().add(view);

			graph.getScrollPane().minWidthProperty().bind(Bindings.createDoubleBinding(()
					-> scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));
			graph.getScrollPane().minHeightProperty().bind(Bindings.createDoubleBinding(()
					-> scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()));

			switch (name) {
				case "ER Schema" ->
					DummyGraphScenario.INSTANCE.buildERSchema(graph);
				case "MongoDB" ->
					DummyGraphScenario.INSTANCE.buildMongoDB(graph);
				case "PostgreSQL" ->
					DummyGraphScenario.INSTANCE.buildPostgreSQL(graph);
				case "PostgreSQL-Inst" ->
					DummyGraphScenario.INSTANCE.buildPostgreSQLInstance(graph);
			}

			tab.setText(name);
			Layout layout = new RandomLayout(graph);
			layout.execute();
		}

		private void selectComponentTabs() {
			tabPane.getTabs().clear();
			tabPane.getTabs().add(0, diagramTab);
			tabPane.getTabs().add(1, componentTab);

			tabPane.getSelectionModel().select(componentTab);
		}

		private void selectEditorTabs() {
			tabPane.getTabs().clear();
			tabPane.getTabs().add(0, diagramTab);
			tabPane.getTabs().add(1, elementTab);
			tabPane.getTabs().add(2, styleTab);
			tabPane.getTabs().add(3, textTab);
			tabPane.getTabs().add(4, positionTab);

			tabPane.getSelectionModel().select(diagramTab);
		}

		private void selectMappingTabs() {
			tabPane.getTabs().clear();
			tabPane.getTabs().add(0, diagramTab);
			tabPane.getTabs().add(1, mappingTab);
			tabPane.getTabs().add(2, accessPathTab);

			tabPane.getSelectionModel().select(accessPathTab);
		}

		private void selectInstanceTabs() {
			tabPane.getTabs().clear();
			tabPane.getTabs().add(0, diagramTab);
			tabPane.getTabs().add(1, instanceTab);
			tabPane.getSelectionModel().select(instanceTab);
		}

		private void buildMappingTab_name(boolean choiceDisabled, String choiceValue, boolean textDisabled, String textValue) {
			nameChoiceBox.setDisable(choiceDisabled);
			nameChoiceBox.setValue(choiceValue);
			nameTextField.setDisable(textDisabled);
			nameTextField.setText(textValue);
		}

		private TreeItem buildProjectTree() {
			TreeItem<String> root = new TreeItem<>("MyProject");
			TreeItem item1 = new TreeItem("ER Schema");
			TreeItem item2 = new TreeItem("Categorical Schema");
			TreeItem item3 = new TreeItem("Database Components");
			TreeItem item31 = new TreeItem("MongoDB");
			TreeItem item33 = new TreeItem("PostgreSQL");

			TreeItem item310 = new TreeItem("Orders");
			TreeItem item311 = new TreeItem("OrdersM1");
			TreeItem item312 = new TreeItem("OrdersM2");
			TreeItem item313 = new TreeItem("OrdersM3");
			TreeItem item314 = new TreeItem("OrdersM4");
			TreeItem item315 = new TreeItem("OrdersM5");
			TreeItem item316 = new TreeItem("OrdersM6");
			TreeItem item317 = new TreeItem("OrdersM7");
			TreeItem item318 = new TreeItem("OrdersM8");

			TreeItem item331 = new TreeItem("Contact");
			TreeItem item332 = new TreeItem("Customer");

			TreeItem item335 = new TreeItem("Orders");
			TreeItem item3351 = new TreeItem("Orders1");
			TreeItem item3352 = new TreeItem("Orders2");
			TreeItem item3353 = new TreeItem("Orders3");
			TreeItem item3354 = new TreeItem("Orders4");
			TreeItem item3355 = new TreeItem("Orders5");

			TreeItem item337 = new TreeItem("Type");

			TreeItem item4 = new TreeItem("Data Migrations");
			TreeItem item40 = new TreeItem("My Migration");
			TreeItem item5 = new TreeItem("Instance");
			TreeItem item53 = new TreeItem("PostgreSQL-Inst");

			root.setExpanded(true);
			root.getChildren().addAll(item1, item2, item3, item4, item5);
			item3.getChildren().addAll(item31, item33);
			item31.getChildren().addAll(item310, item311, item312, item313, item314, item315, item316, item317, item318);
			item33.getChildren().addAll(item331, item332, item335, item3351, item3352, item3353, item3354, item3355, item337);
			item4.getChildren().addAll(item40);
			item5.getChildren().addAll(item53);

			return root;
		}

		private void buildContactInstanceTableView() {
			instanceTable.getColumns().clear();
			TableColumn idColumn = new TableColumn("id");
			TableColumn numberColumn = new TableColumn("number");
			TableColumn nameColumn = new TableColumn("name");

			instanceTable.getColumns().add(idColumn);
			instanceTable.getColumns().add(numberColumn);
			instanceTable.getColumns().add(nameColumn);

			idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
			numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
			nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

			DummyObservableData.INSTANCE.setItemsContactInstance(instanceTable);
		}

	}

}
