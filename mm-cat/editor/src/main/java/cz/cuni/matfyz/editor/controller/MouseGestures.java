/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.editor.controller;

//import cz.cuni.matfyz.editor.view.Graph;
//import cz.cuni.matfyz.editor.view.cell.Cell;
//import cz.cuni.matfyz.editor.model.Widget;
import cz.cuni.matfyz.editor.representation.WidgetRepresentation;
import cz.cuni.matfyz.editor.view.ZoomableScrollPane;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;

/**
 *
 * @author pavel.koupil
 */
public class MouseGestures {

	private final DragContext dragContext = new DragContext();

	private ZoomableScrollPane pane;

	private Paint save;

	public MouseGestures(ZoomableScrollPane pane) {
		this.pane = pane;
	}

	public void makeDraggable(Node node) {
		node.setOnMousePressed(onMousePressedEventHandler);
		node.setOnMouseDragged(onMouseDraggedEventHandler);
		node.setOnMouseReleased(onMouseReleasedEventHandler);

	}

	private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

		@Override
		public void handle(MouseEvent event) {

			Node node = (Node) event.getSource();
			WidgetRepresentation representation = (WidgetRepresentation) event.getSource();

			double scale = pane.getScaleValue();

//            System.out.println((event.getSource() instanceof Cell) ? "YES" : "NO");
//            System.out.println(event.getSource());
//            cell.getView();
//            System.out.println("size:  " + cell.getChildren().size());
//            var xxx = cell.getChildren().get(0);
			Shape shape = (Shape) representation.getChildren().get(0);
			save = shape.getStroke();
			shape.setStroke(Color.YELLOW);
			shape.setStrokeWidth(5);

//            System.out.println(xxx.getClass());
//            node.getViewOrder();
			dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
			dragContext.y = node.getBoundsInParent().getMinY() * scale - event.getScreenY();

		}
	};

	private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

		@Override
		public void handle(MouseEvent event) {

			Node node = (Node) event.getSource();

			double offsetX = event.getScreenX() + dragContext.x;
			double offsetY = event.getScreenY() + dragContext.y;

			// adjust the offset in case we are zoomed
			double scale = pane.getScaleValue();

			offsetX /= scale;
			offsetY /= scale;

//            System.out.println(node);
//            System.out.println("before: " + node.getBoundsInParent());
			node.relocate(offsetX, offsetY);
//            System.out.println("after: " + node.getBoundsInParent());

		}
	};

	private EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

		@Override
		public void handle(MouseEvent event) {

//            Node node = (Node) event.getSource();
			WidgetRepresentation representation = (WidgetRepresentation) event.getSource();

//            double scale = graph.getScale();
//            System.out.println((event.getSource() instanceof Cell) ? "YES" : "NO");
//            System.out.println(event.getSource());
//            cell.getView();
//            System.out.println("size:  " + cell.getChildren().size());
//            var xxx = cell.getChildren().get(0);
			Shape shape = (Shape) representation.getChildren().get(0);
//            save = shape.getStroke();
			shape.setStroke(save);
			shape.setStrokeWidth(2);

//            System.out.println(xxx.getClass());
//            node.getViewOrder();
//            dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
//            dragContext.y = node.getBoundsInParent().getMinY() * scale - event.getScreenY();
		}
	};

	private static class DragContext {

		double x;
		double y;

	}

}
