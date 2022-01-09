package cat.editor.view.cell;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class ERAttributeCell extends Cell {

    private static final double SIZE = 10;

    public ERAttributeCell(String id, String name, double x, double y) {
        super(id);

        Text text = new Text(name);
        text.setFont(Font.font("DejaVu Sans Mono", 16));
        double height = text.getBoundsInLocal().getHeight();
//        System.out.println(height + " ::: height");
        text.relocate(25, -(height / 2 - SIZE));

        Circle shape = new Circle(SIZE, SIZE, SIZE);
        shape.setUserData("aaa");
        shape.setStroke(Color.BLACK);
        shape.setFill(Color.WHITE);
        shape.setStrokeWidth(2);

        setView(shape);
        setView(text);
        relocate(x, y);
    }
}
