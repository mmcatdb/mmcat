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

	public void buildERSchema(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 260, CellType.ER_ENTITY);
		model.addCell("101", "Id", 90, 370, CellType.ER_IDENTIFIER);
		model.addCell("110", "Orders", 50, 135, CellType.ER_RELATIONSHIP);
		model.addCell("111", "Order", 200, 140, CellType.ER_ENTITY);
		model.addCell("112", "Number", 240, 50, CellType.ER_ATTRIBUTE);
		model.addCell("113", "Contact", 350, 135, CellType.ER_RELATIONSHIP);
		model.addCell("114", "Type", 500, 140, CellType.ER_ENTITY);
		model.addCell("115", "Name", 540, 50, CellType.ER_IDENTIFIER);
		model.addCell("116", "Value", 390, 50, CellType.ER_ATTRIBUTE);
		model.addCell("117", "Items", 200, 255, CellType.ER_RELATIONSHIP);
		model.addCell("118", "Quantity", 350, 270, CellType.ER_ATTRIBUTE);
		model.addCell("121", "Product", 200, 380, CellType.ER_ENTITY);
		model.addCell("122", "Id", 200, 490, CellType.ER_IDENTIFIER);
		model.addCell("123", "Name", 280, 490, CellType.ER_ATTRIBUTE);
		model.addCell("124", "Price", 350, 390, CellType.ER_ATTRIBUTE);

		model.addEdge(null, "100", "101", EdgeType.ER);
		model.addEdge(null, "110", "100", EdgeType.ER);
		model.addEdge(null, "110", "111", EdgeType.ER);
		model.addEdge(null, "113", "111", EdgeType.ER);
		model.addEdge(null, "111", "112", EdgeType.ER);
		model.addEdge(null, "113", "114", EdgeType.ER);
		model.addEdge(null, "113", "116", EdgeType.ER);
		model.addEdge(null, "114", "115", EdgeType.ER);
		model.addEdge(null, "117", "111", EdgeType.ER);
		model.addEdge(null, "117", "118", EdgeType.ER);
		model.addEdge(null, "117", "121", EdgeType.ER);
		model.addEdge(null, "121", "122", EdgeType.ER);
		model.addEdge(null, "121", "123", EdgeType.ER);
		model.addEdge(null, "121", "124", EdgeType.ER);
		graph.endUpdate();
	}

	public void buildCategoricalSchema(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("101", "Id", 50, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("110", "Orders", 50, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("111", "Order", 170, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("112", "Number", 170, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("113", "Contact", 290, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("115", "Name", 410, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("116", "Value", 290, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("117", "Items", 170, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("118", "Quantity", 290, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 170, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryArrows(model);
		graph.endUpdate();
	}

	private void addCategoryArrows(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);

		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.CATEGORICAL);
		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);

		model.addEdge("8", "114", "115", EdgeType.CATEGORICAL);
		model.addEdge("-8", "115", "114", EdgeType.CATEGORICAL);

		model.addEdge("9", "117", "111", EdgeType.CATEGORICAL);
		model.addEdge("-9", "111", "117", EdgeType.CATEGORICAL);
		model.addEdge("10", "117", "118", EdgeType.CATEGORICAL);
		model.addEdge("-10", "118", "117", EdgeType.CATEGORICAL);
		model.addEdge("11", "117", "121", EdgeType.CATEGORICAL);
		model.addEdge("-11", "121", "117", EdgeType.CATEGORICAL);

		model.addEdge("12", "121", "122", EdgeType.CATEGORICAL);
		model.addEdge("-12", "122", "121", EdgeType.CATEGORICAL);
		model.addEdge("13", "121", "123", EdgeType.CATEGORICAL);
		model.addEdge("-13", "123", "121", EdgeType.CATEGORICAL);
		model.addEdge("14", "121", "124", EdgeType.CATEGORICAL);
		model.addEdge("-14", "124", "121", EdgeType.CATEGORICAL);
	}

	public void buildMongoDB(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();
		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("101", "Id", 50, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("110", "Orders", 50, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_KIND);
		model.addCell("112", "Number", 170, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("113", "Contact", 290, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("115", "Name", 410, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("116", "Value", 290, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("117", "Items", 170, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("118", "Quantity", 290, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 170, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryArrows(model);
		graph.endUpdate();
	}

	public void buildOrderCollection(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 170, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryArrows(model);
		graph.endUpdate();
	}

	public void buildOrderCollection_GroupingId(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_AVAILABLE_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 170, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryArrows(model);
		graph.endUpdate();
	}

	public void buildOrderCollection_CompleteId(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_SELECTED_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 170, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryArrows(model);
		graph.endUpdate();
	}

	public void buildOrderCollection_Items(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_SELECTED_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 170, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryArrows(model);
		graph.endUpdate();
	}

	public void buildOrderCollection_Items2(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_AVAILABLE_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_SELECTED_PROPERTY);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 170, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryArrows(model);
		graph.endUpdate();
	}

	public void buildOrderCollection_InliningProduct(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_AVAILABLE_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_PROPERTY);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_PROPERTY);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 170, 530, CellType.MAPPING_SELECTED_PROPERTY);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryArrows(model);
		graph.endUpdate();
	}

	public void buildOrderCollection_Complete(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("115", "Name", 410, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("116", "Value", 290, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_PROPERTY);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_PROPERTY);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 170, 530, CellType.MAPPING_PROPERTY);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_PROPERTY);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_PROPERTY);

		addCategoryArrows(model);
		graph.endUpdate();
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

//	public void buildMongoOrder_3_Contact(Graph graph) {
//		Model model = graph.getModel();
//
//		graph.beginUpdate();
//
//		model.addCell("100", "Customer", 100, 300, CellType.MAPPING_AVAILABLE);
//		model.addCell("101", "Id", 100, 400, CellType.MAPPING_AVAILABLE_PROPERTY);
//		model.addCell("110", "Orders", 100, 200, CellType.MAPPING_AVAILABLE);
//		model.addCell("111", "Order", 200, 200, CellType.MAPPING_AVAILABLE_KIND);
//		model.addCell("112", "Number", 200, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
//		model.addCell("113", "Contact", 300, 200, CellType.MAPPING_SELECTED_PROPERTY);
//		model.addCell("114", "Type", 400, 200, CellType.MAPPING_AVAILABLE);
//		model.addCell("115", "Name", 400, 100, CellType.MAPPING_AVAILABLE);
//		model.addCell("116", "Value", 300, 100, CellType.MAPPING_AVAILABLE);
//		model.addCell("117", "Items", 200, 300, CellType.MAPPING_AVAILABLE);
//		model.addCell("118", "Quantity", 300, 300, CellType.MAPPING_AVAILABLE);
//		model.addCell("121", "Product", 200, 400, CellType.MAPPING_AVAILABLE);
//		model.addCell("122", "Id", 200, 500, CellType.MAPPING_AVAILABLE);
//		model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
//		model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);
//
//		addCategoryArrows(model);
//		graph.endUpdate();
//	}
//
//	public void buildMongoOrder_4_ContactTypeName(Graph graph) {
//		Model model = graph.getModel();
//
//		graph.beginUpdate();
//
//		model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("101", "Id", 100, 400, CellType.MAPPING_AVAILABLE_PROPERTY);
//		model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
//		model.addCell("112", "Number", 200, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
//		model.addCell("113", "Contact", 300, 200, CellType.MAPPING_PROPERTY);
//		model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("115", "Name", 400, 100, CellType.MAPPING_AVAILABLE);
//		model.addCell("116", "Value", 300, 100, CellType.MAPPING_SELECTED_PROPERTY);
//		model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
//		model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
//		model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
//		model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);
//
//		addCategoryArrows(model);
//		graph.endUpdate();
//	}
//
//	public void buildMongoOrder_5_ContactTypeSelectedName(Graph graph) {
//		Model model = graph.getModel();
//
//		graph.beginUpdate();
//
//		model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("101", "Id", 100, 400, CellType.MAPPING_AVAILABLE_PROPERTY);
//		model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
//		model.addCell("112", "Number", 200, 100, CellType.MAPPING_AVAILABLE_PROPERTY);
//		model.addCell("113", "Contact", 300, 200, CellType.MAPPING_PROPERTY);
//		model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("115", "Name", 400, 100, CellType.MAPPING_SELECTED_NAME);
//		model.addCell("116", "Value", 300, 100, CellType.MAPPING_PROPERTY);
//		model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("121", "Product", 200, 400, CellType.CATEGORICAL_OBJECT);
//		model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
//		model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
//		model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);
//
//		addCategoryArrows(model);
//		graph.endUpdate();
//	}
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
