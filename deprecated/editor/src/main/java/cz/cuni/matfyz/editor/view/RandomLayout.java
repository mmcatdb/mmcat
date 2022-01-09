package cz.cuni.matfyz.editor.view;

import java.util.List;
import java.util.Random;

import cz.cuni.matfyz.editor.view.cell.Cell;

public class RandomLayout extends Layout {

    private final Graph graph;

    private final Random rnd = new Random();

    public RandomLayout(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void execute() {

        List<Cell> cells = graph.getModel().getAllCells();

        cells.forEach(cell -> {
            double x = 20 + rnd.nextDouble() * 500;
            double y = 20 + rnd.nextDouble() * 500;

//            cell.relocate(x, y);
        });

    }

}
