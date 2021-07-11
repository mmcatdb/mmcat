/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.dummy;

import cat.editor.view.cell.CellType;
import cat.editor.view.edge.EdgeType;
import cat.editor.view.Graph;
import cat.editor.view.Model;

/**
 *
 * @author pavel.koupil
 */
public enum DummyGraphScenario {
    INSTANCE;

    public void buildSchemaCategory(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildER(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.ER_ENTITY);
        model.addCell("101", "Id", 100, 400, CellType.ER_IDENTIFIER);
        model.addCell("110", "Orders", 100, 200, CellType.ER_RELATIONSHIP);
        model.addCell("111", "Order", 200, 200, CellType.ER_ENTITY);
        model.addCell("112", "Number", 200, 100, CellType.ER_ATTRIBUTE);
        model.addCell("113", "Contact", 300, 200, CellType.ER_RELATIONSHIP);
        model.addCell("114", "Type", 400, 200, CellType.ER_ENTITY);
        model.addCell("115", "Name", 400, 100, CellType.ER_IDENTIFIER);
        model.addCell("116", "Value", 300, 100, CellType.ER_ATTRIBUTE);
        model.addCell("117", "Items", 200, 300, CellType.ER_RELATIONSHIP);
        model.addCell("118", "Quantity", 300, 300, CellType.ER_ATTRIBUTE);
        model.addCell("121", "Product", 200, 400, CellType.ER_ENTITY);
        model.addCell("122", "Id", 200, 500, CellType.ER_IDENTIFIER);
        model.addCell("123", "Name", 300, 500, CellType.ER_ATTRIBUTE);
        model.addCell("124", "Price", 300, 400, CellType.ER_ATTRIBUTE);

        model.addEdge("100", "101", EdgeType.ER);
        model.addEdge("110", "100", EdgeType.ER);
        model.addEdge("110", "111", EdgeType.ER);
        model.addEdge("113", "111", EdgeType.ER);
        model.addEdge("111", "112", EdgeType.ER);
        model.addEdge("113", "114", EdgeType.ER);
        model.addEdge("113", "116", EdgeType.ER);
        model.addEdge("114", "115", EdgeType.ER);
        model.addEdge("117", "111", EdgeType.ER);
        model.addEdge("117", "118", EdgeType.ER);
        model.addEdge("117", "121", EdgeType.ER);
        model.addEdge("121", "122", EdgeType.ER);
        model.addEdge("121", "123", EdgeType.ER);
        model.addEdge("121", "124", EdgeType.ER);
        graph.endUpdate();
    }

//    private void addGraphComponents() {
//        buildER();
//        buildSchemaCategory();
//    }
    private void addCategoryArrows(Model model) {
        model.addEdge("100", "101");
        model.addEdge("101", "100");

        model.addEdge("100", "110");
        model.addEdge("110", "100");
        model.addEdge("111", "110");
        model.addEdge("110", "111");

        model.addEdge("113", "111");
        model.addEdge("111", "113");
        model.addEdge("111", "112");
        model.addEdge("112", "111");

        model.addEdge("113", "114");
        model.addEdge("114", "113");
        model.addEdge("113", "116");
        model.addEdge("116", "113");

        model.addEdge("114", "115");
        model.addEdge("115", "114");

        model.addEdge("117", "111");
        model.addEdge("111", "117");
        model.addEdge("117", "118");
        model.addEdge("118", "117");
        model.addEdge("117", "121");
        model.addEdge("121", "117");

        model.addEdge("121", "122");
        model.addEdge("122", "121");
        model.addEdge("121", "123");
        model.addEdge("123", "121");
        model.addEdge("121", "124");
        model.addEdge("124", "121");
    }

    public void buildProductKind(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_SELECTED_KIND);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildProductKind2(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_SELECTED_KIND);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_PROPERTY);
        model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildProductKind3(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_KIND);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_PROPERTY);
        model.addCell("123", "Name", 300, 500, CellType.MAPPING_PROPERTY);
        model.addCell("124", "Price", 300, 400, CellType.MAPPING_PROPERTY);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildProductItems(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.MAPPING_KIND);
        model.addCell("118", "Quantity", 300, 300, CellType.MAPPING_PROPERTY);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_PROPERTY);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildProductCustomer(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.MAPPING_KIND);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildProductOrders(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.MAPPING_KIND);
        model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildProductOrder(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildProductContact(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_KIND);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_PROPERTY);
        model.addCell("116", "Value", 300, 100, CellType.MAPPING_PROPERTY);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildProductType(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.MAPPING_KIND);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_PROPERTY);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildMongoOrder_0(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_AVAILABLE);
        model.addCell("110", "Orders", 100, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_AVAILABLE);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("114", "Type", 400, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_AVAILABLE);
        model.addCell("116", "Value", 300, 100, CellType.MAPPING_AVAILABLE);
        model.addCell("117", "Items", 200, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("118", "Quantity", 300, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_AVAILABLE);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildMongoOrder_1_GroupingId(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        // tady se udela groupingId
        model.addCell("100", "Customer", 100, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_AVAILABLE);
        model.addCell("110", "Orders", 100, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_AVAILABLE_KIND);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_AVAILABLE);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("114", "Type", 400, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_AVAILABLE);
        model.addCell("116", "Value", 300, 100, CellType.MAPPING_AVAILABLE);
        model.addCell("117", "Items", 200, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("118", "Quantity", 300, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_AVAILABLE);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildMongoOrder_2_CompleteId(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        // tady se udela groupingId
        model.addCell("100", "Customer", 100, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_SELECTED_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("114", "Type", 400, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_AVAILABLE);
        model.addCell("116", "Value", 300, 100, CellType.MAPPING_AVAILABLE);
        model.addCell("117", "Items", 200, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("118", "Quantity", 300, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_AVAILABLE);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildMongoOrder_3_Contact(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_AVAILABLE_KIND);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_SELECTED_PROPERTY);
        model.addCell("114", "Type", 400, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_AVAILABLE);
        model.addCell("116", "Value", 300, 100, CellType.MAPPING_AVAILABLE);
        model.addCell("117", "Items", 200, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("118", "Quantity", 300, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_AVAILABLE);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildMongoOrder_4_ContactTypeName(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_PROPERTY);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_AVAILABLE);
        model.addCell("116", "Value", 300, 100, CellType.MAPPING_SELECTED_PROPERTY);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildMongoOrder_5_ContactTypeSelectedName(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_PROPERTY);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_SELECTED_NAME);
        model.addCell("116", "Value", 300, 100, CellType.MAPPING_PROPERTY);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildMongoOrder_60_Items(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_SELECTED_KIND);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("114", "Type", 400, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_AVAILABLE_NAME);
        model.addCell("116", "Value", 300, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("117", "Items", 200, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("118", "Quantity", 300, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_AVAILABLE);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildMongoOrder_6_Items(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_AVAILABLE_KIND);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("114", "Type", 400, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_AVAILABLE_NAME);
        model.addCell("116", "Value", 300, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("117", "Items", 200, 300, CellType.MAPPING_SELECTED_PROPERTY);
        model.addCell("118", "Quantity", 300, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_AVAILABLE);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildMongoOrder_7_InliningProduct(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.MAPPING_AVAILABLE);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_AVAILABLE_KIND);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("114", "Type", 400, 200, CellType.MAPPING_AVAILABLE);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_AVAILABLE_NAME);
        model.addCell("116", "Value", 300, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
        model.addCell("117", "Items", 200, 300, CellType.MAPPING_PROPERTY);
        model.addCell("118", "Quantity", 300, 300, CellType.MAPPING_PROPERTY);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_AVAILABLE);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_SELECTED_PROPERTY);
        model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
        model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildMongoOrder_8_Complete(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.MAPPING_PROPERTY);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
        model.addCell("112", "Number", 200, 100, CellType.MAPPING_PROPERTY);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_PROPERTY);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.MAPPING_NAME);
        model.addCell("116", "Value", 300, 100, CellType.MAPPING_PROPERTY);
        model.addCell("117", "Items", 200, 300, CellType.MAPPING_PROPERTY);
        model.addCell("118", "Quantity", 300, 300, CellType.MAPPING_PROPERTY);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.MAPPING_PROPERTY);
        model.addCell("123", "Name", 300, 500, CellType.MAPPING_PROPERTY);
        model.addCell("124", "Price", 300, 400, CellType.MAPPING_PROPERTY);

        addCategoryArrows(model);
        graph.endUpdate();
    }
    
    public void buildPostgreSQLKinds(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.MAPPING_KIND);
        model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("110", "Orders", 100, 200, CellType.MAPPING_KIND);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
        model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_KIND);
        model.addCell("114", "Type", 400, 200, CellType.MAPPING_KIND);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.MAPPING_KIND);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_KIND);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }
    
    public void buildMongoDBKinds(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
        model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }
    
    public void buildNeo4jKinds(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.MAPPING_KIND);
        model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("110", "Orders", 100, 200, CellType.MAPPING_KIND);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
        model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }
    
    public void buildPostgreSQLInstance(Graph graph) {
        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("100", "Customer", 100, 300, CellType.MAPPING_KIND);
        model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
        model.addCell("110", "Orders", 100, 200, CellType.MAPPING_KIND);
        model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
        model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("113", "Contact", 300, 200, CellType.MAPPING_KIND);
        model.addCell("114", "Type", 400, 200, CellType.MAPPING_KIND);
        model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
        model.addCell("117", "Items", 200, 300, CellType.MAPPING_KIND);
        model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
        model.addCell("121", "Product", 200, 400, CellType.MAPPING_KIND);
        model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
        model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);

        addCategoryArrows(model);
        graph.endUpdate();
    }

    public void buildNeo4jInstance(Graph graph) {
        System.out.println("TODO");
    }

    public void buildMongoDBInstance(Graph graph) {
        System.out.println("TODO");
    }

    public void buildRiakKVInstance(Graph graph) {
        System.out.println("TODO");
    }

    public void buildCassandraInstance(Graph graph) {
        System.out.println("TODO");
    }

}
