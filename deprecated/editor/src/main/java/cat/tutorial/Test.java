package cat.tutorial;

/**
 *
 * @author pavel.koupil
 */
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author kachna
 */
public class Test extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        VBox root = new VBox();
        root.getChildren().addAll(new Button("button1"), new Button("button2"), new Button("button3"));
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        ScrollPane sp = new ScrollPane();
        sp.setContent(root);
        sp.setPannable(true); // it means that the user should be able to pan the viewport by using the mouse.
        Scene scene = new Scene(sp, 100, 100);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
