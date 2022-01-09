package cz.cuni.matfyz.editor.view.cell;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

//CubicCurve cubiccurve = new CubicCurve(
//   startX, startY, controlX1, controlY1, controlX2, controlY2, endX, endY);
/**
 *
 * @author pavel.koupil
 */
public class ERWeakIdentifierCell extends Cell {

	private static final double SIZE = 10;

	public ERWeakIdentifierCell(String id, String name, double x, double y) {
		super(id);

//        Text text = new Text(name);
//        text.setFont(Font.font("DejaVu Sans Mono", 16));
//        double height = text.getBoundsInLocal().getHeight();
//        text.relocate(25, -(height / 2 - SIZE));
		Circle shape = new Circle(SIZE, SIZE, SIZE);
		shape.setUserData("aaa");
		shape.setStroke(Color.BLACK);
		shape.setFill(Color.BLACK);
		shape.setStrokeWidth(2);
		
		Circle shape2 = new Circle(SIZE, SIZE, SIZE);
//		shape2.setUserData("aaa");
		shape2.setStroke(Color.BLACK);
		shape2.setFill(Color.BLACK);
		shape2.setStrokeWidth(2);
		shape2.relocate(90, -40);

		CubicCurve curve = new CubicCurve(10, 10, 10, -30, 10, -30, 100, -30);
		curve.setStroke(Color.BLACK);
		curve.setFill(Color.TRANSPARENT);
		curve.setStrokeWidth(2);

		setView(shape);
		setView(curve);
		setView(shape2);
//        setView(text);
		relocate(x, y);
	}

}
