/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor.view.cell;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class SelectedCell extends Cell {

	private static final double SIZE = 15;

	public SelectedCell(String id, String name, double x, double y) {
		super(id);

		Text text = new Text(name);
		text.setFont(Font.font("DejaVu Sans Mono", 16));
		double height = text.getBoundsInLocal().getHeight();
		text.relocate(30, -(height / 2 - SIZE) - SIZE);

		relocate(x, y);

		Text idText = new Text(id);
		idText.setFont(Font.font("DejaVu Sans Mono", 12));

		double textWidth = idText.getBoundsInLocal().getWidth();
		double textHeight = idText.getBoundsInLocal().getHeight();

		double shapeWidth = SIZE * 2;
		double shapeHeight = SIZE * 2;

		shapeWidth = shapeWidth > textWidth ? shapeWidth : textWidth + 10;
		shapeHeight = shapeHeight > textHeight ? shapeHeight : textHeight + 10;

		double diffWidth = shapeWidth - textWidth;
		double diffHeight = shapeHeight - textHeight;
		idText.relocate(diffWidth / 2, diffHeight / 2);

		Circle shape = new Circle(SIZE, SIZE, SIZE);
		shape.setUserData("aaa");
		shape.setStroke(Color.YELLOW);
		shape.setFill(Color.WHITE);
		shape.setStrokeWidth(5);

		setView(shape);
		setView(text);
		setView(idText);
		relocate(x, y);
	}
}
