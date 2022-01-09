package cat.editor.view.cell;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class ERRelationshipCell extends Cell {

	public ERRelationshipCell(String id, String name, double x, double y) {
		super(id);

		Text text = new Text(name);
		text.setFont(Font.font("DejaVu Sans Mono", 16));

		double textWidth = text.getBoundsInLocal().getWidth();
		double textHeight = text.getBoundsInLocal().getHeight();

		double shapeWidth = 100;
		double shapeHeight = 50;

		shapeWidth = shapeWidth > textWidth ? shapeWidth : textWidth + 30;
		shapeHeight = shapeHeight > textHeight ? shapeHeight : textHeight + 30;

		double diffWidth = shapeWidth - textWidth;
		double diffHeight = shapeHeight - textHeight;
		text.relocate(diffWidth / 2, diffHeight / 2);

//		System.out.println("[" + shapeWidth / 2 + "," + 0 + "][" + shapeWidth + "," + shapeHeight / 2 + "][" + shapeWidth / 2 + "," + shapeHeight + "][" + 0 + "," + shapeHeight / 2 + "]");
		Polygon shape = new Polygon(
				shapeWidth / 2, 0,
				shapeWidth, shapeHeight / 2,
				shapeWidth / 2, shapeHeight,
				0, shapeHeight / 2);
		shape.setUserData("aaa");
		shape.setStroke(Color.BLACK);
		shape.setFill(Color.WHITE);
		shape.setStrokeWidth(2);
		setView(shape);

		double dx = (shape.getBoundsInLocal().getWidth() - shapeWidth) / 2.0;
		double dy = (shape.getBoundsInLocal().getHeight() - shapeHeight) / 2.0;

		dx = dx > 0 ? dx : 0;
		dy = dy > 0 ? dy : 0;

//		System.out.println(dx + ":::" + dy);
		setView(text);
		relocate(x - dx, y - dy);
	}
}
