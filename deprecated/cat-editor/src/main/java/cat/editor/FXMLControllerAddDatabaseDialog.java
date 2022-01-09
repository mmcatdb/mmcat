package cat.editor;

/**
 *
 * @author pavel.koupil
 */
import cat.tutorial.Person;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FXMLControllerAddDatabaseDialog {

	@FXML
	private TextField tfId;

	@FXML
	private TextField tfName;

	@FXML
	private TextField tfAge;

//	@FXML
//	private ChoiceBox choiceBox;

	private ObservableList<Person> appMainObservableList;

//	public FXMLControllerAddDatabaseDialog() {
////		System.out.println("X");
////		choiceBox.setValue("PostgreSQL");
////
////		choiceBox.setValue("Inherit");
////		ObservableList<String> list = choiceBox.getItems();
////		list.add("PostgreSQL");
////		list.add("MongoDB");
//	}
//	@FXML
//	private void testit(ActionEvent event) {
////        System.out.println("You clicked me!");
//
////		choiceBox.setValue("Inherit");
//		ObservableList<String> list = choiceBox.getItems();
//		list.add("PostgreSQL");
//		list.add("MongoDB");
//
//		choiceBox.setValue("PostgreSQL");
//		System.out.println("XXXX");
//	}

	@FXML
	void btnAddPersonClicked(ActionEvent event) {
		System.out.println("btnAddPersonClicked");
		int id = Integer.valueOf(tfId.getText().trim());
		String name = tfName.getText().trim();
		int iAge = Integer.valueOf(tfAge.getText().trim());

		Person data = new Person(id, name, iAge);
//        appMainObservableList.add(data);

		closeStage(event);
	}

	public void setAppMainObservableList(ObservableList<Person> tvObservableList) {
		this.appMainObservableList = tvObservableList;

	}

	private void closeStage(ActionEvent event) {
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

}
