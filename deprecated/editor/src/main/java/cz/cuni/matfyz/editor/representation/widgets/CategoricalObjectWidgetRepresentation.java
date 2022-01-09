package cz.cuni.matfyz.editor.representation.widgets;

import cz.cuni.matfyz.editor.model.widgets.CategoricalObjectWidget;
import cz.cuni.matfyz.editor.representation.WidgetRepresentation;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class CategoricalObjectWidgetRepresentation extends WidgetRepresentation {

	private static final double SIZE = 15;

	public CategoricalObjectWidgetRepresentation(CategoricalObjectWidget model) {
		super(model);

		Text text = new Text(model.getName());
		text.setFont(Font.font("DejaVu Sans Mono", 16));
		double height = text.getBoundsInLocal().getHeight();
		text.relocate(30, -(height / 2 - SIZE) - SIZE);

		relocate(model.getX(), model.getY());

		Text idText = new Text(model.getId());
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
		shape.setStroke(Color.BLACK);
		shape.setFill(Color.WHITE);
		shape.setStrokeWidth(2);

		setView(shape);
		setView(text);
		setView(idText);
		relocate(model.getX(), model.getY());
	}

}
