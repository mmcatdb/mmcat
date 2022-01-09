package cz.cuni.matfyz.editor.view.cell;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class EREntityCell extends Cell {

	public EREntityCell(String id, String name, double x, double y) {
		super(id);

		Text text = new Text(name);
		text.setFont(Font.font("DejaVu Sans Mono", 16));

		double textWidth = text.getBoundsInLocal().getWidth();
		double textHeight = text.getBoundsInLocal().getHeight();

		double shapeWidth = 100;
		double shapeHeight = 40;

		shapeWidth = shapeWidth > textWidth ? shapeWidth : textWidth + 20;
		shapeHeight = shapeHeight > textHeight ? shapeHeight : textHeight + 20;

		double diffWidth = shapeWidth - textWidth;
		double diffHeight = shapeHeight - textHeight;
		text.relocate(diffWidth / 2, diffHeight / 2);

		Rectangle shape = new Rectangle(shapeWidth, shapeHeight);
		shape.setUserData("aaa");
		Color color = Color.color(Math.random(), Math.random(), Math.random());
		shape.setStroke(Color.BLACK);
		shape.setFill(Color.WHITE);
		shape.setStrokeWidth(2);
		setView(shape);

		setView(text);
		relocate(x, y);
	}
}
