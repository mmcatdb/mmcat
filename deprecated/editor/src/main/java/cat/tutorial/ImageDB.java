package cat.tutorial;

/**
 *
 * @author pavel.koupil
 */
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class ImageDB extends Application
{
  @Override
  public void start(Stage root)
  {
//    Image image = new Image("0.jpg");
    Image image = createImage();
    ImageView view = new ImageView();
    view.setImage(image);
    StackPane imageHolder = new StackPane(view);

    ScrollPane scroll = new ScrollPane();
    scroll.setPrefViewportHeight(800);
    scroll.setPrefViewportWidth(600);
    scroll.setContent(imageHolder);

    GridPane grid = new GridPane();

    imageHolder.minWidthProperty().bind(Bindings.createDoubleBinding(() -> 
        scroll.getViewportBounds().getWidth(), scroll.viewportBoundsProperty()));
    imageHolder.minHeightProperty().bind(Bindings.createDoubleBinding(() -> 
        scroll.getViewportBounds().getHeight(), scroll.viewportBoundsProperty()));
    grid.getChildren().add(imageHolder);

//    grid.setAlignment(Pos.TOP_CENTER);

    BorderPane border = new BorderPane();
    border.setCenter(scroll);

    root.setScene(new Scene(border));
//    root.setMaximized(true);
    root.show();
  }

  private Image createImage() {
      return new Rectangle(400, 200, Color.CORNFLOWERBLUE).snapshot(null, null);
  }

  public static void main(String[] args) {
      launch(args);
  }
}
