package cat.editor.view;

import cat.editor.view.Graph;
import cat.editor.view.cell.Cell;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;

public class MouseGestures {

    final DragContext dragContext = new DragContext();

    Graph graph;

    public MouseGestures(Graph graph) {
        this.graph = graph;
    }

    public void makeDraggable(final Node node) {

        node.setOnMousePressed(onMousePressedEventHandler);
        node.setOnMouseDragged(onMouseDraggedEventHandler);
        node.setOnMouseReleased(onMouseReleasedEventHandler);

    }

    private Paint save;
    
    EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            Node node = (Node) event.getSource();
            Cell cell = (Cell) event.getSource();

            double scale = graph.getScale();

//            System.out.println((event.getSource() instanceof Cell) ? "YES" : "NO");
//            System.out.println(event.getSource());
//            cell.getView();
//            System.out.println("size:  " + cell.getChildren().size());
//            var xxx = cell.getChildren().get(0);
            
            Shape shape = (Shape) cell.getChildren().get(0);
            save = shape.getStroke();
            shape.setStroke(Color.YELLOW);
            shape.setStrokeWidth(5);
            
//            System.out.println(xxx.getClass());
//            node.getViewOrder();
            dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
            dragContext.y = node.getBoundsInParent().getMinY() * scale - event.getScreenY();

        }
    };

    EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            Node node = (Node) event.getSource();

            double offsetX = event.getScreenX() + dragContext.x;
            double offsetY = event.getScreenY() + dragContext.y;

            // adjust the offset in case we are zoomed
            double scale = graph.getScale();

            offsetX /= scale;
            offsetY /= scale;

//            System.out.println(node);
//            System.out.println("before: " + node.getBoundsInParent());
            node.relocate(offsetX, offsetY);
//            System.out.println("after: " + node.getBoundsInParent());

        }
    };

    EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

//            Node node = (Node) event.getSource();
            Cell cell = (Cell) event.getSource();

//            double scale = graph.getScale();

//            System.out.println((event.getSource() instanceof Cell) ? "YES" : "NO");
//            System.out.println(event.getSource());
//            cell.getView();
//            System.out.println("size:  " + cell.getChildren().size());
//            var xxx = cell.getChildren().get(0);
            
            Shape shape = (Shape) cell.getChildren().get(0);
//            save = shape.getStroke();
            shape.setStroke(save);
            shape.setStrokeWidth(2);
            
//            System.out.println(xxx.getClass());
//            node.getViewOrder();
//            dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
//            dragContext.y = node.getBoundsInParent().getMinY() * scale - event.getScreenY();
            
        }
    };

    class DragContext {

        double x;
        double y;

    }
}
