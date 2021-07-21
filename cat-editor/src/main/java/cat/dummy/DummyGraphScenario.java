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

		model.addCell("100", "Customer", 10, 220, CellType.ER_ENTITY);
		model.addCell("101", "Id", 50, 330, CellType.ER_IDENTIFIER);
		model.addCell("110", "Orders", 10, 95, CellType.ER_RELATIONSHIP);
		model.addCell("111", "Order", 160, 100, CellType.ER_ENTITY);
		model.addCell("112", "Number", 200, 10, CellType.ER_ATTRIBUTE);
		model.addCell("113", "Contact", 310, 95, CellType.ER_RELATIONSHIP);
		model.addCell("114", "Type", 460, 100, CellType.ER_ENTITY);
		model.addCell("115", "Name", 500, 10, CellType.ER_IDENTIFIER);
		model.addCell("116", "Value", 350, 10, CellType.ER_ATTRIBUTE);
		model.addCell("117", "Items", 160, 215, CellType.ER_RELATIONSHIP);
		model.addCell("118", "Quantity", 310, 230, CellType.ER_ATTRIBUTE);
		model.addCell("121", "Product", 160, 340, CellType.ER_ENTITY);
		model.addCell("122", "Id", 160, 450, CellType.ER_IDENTIFIER);
		model.addCell("123", "Name", 240, 450, CellType.ER_ATTRIBUTE);
		model.addCell("124", "Price", 310, 350, CellType.ER_ATTRIBUTE);
		model.addCell("X", "", 130, 110, CellType.ER_WEAK_IDENTIFIER);

		model.addEdge("", "100", "101", EdgeType.ER);
		model.addEdge("(0,*)", "110", "100", EdgeType.ER);
		model.addEdge("(1,1)", "110", "111", EdgeType.ER);
		model.addEdge("(0,*)", "113", "111", EdgeType.ER);
		model.addEdge("", "111", "112", EdgeType.ER);
		model.addEdge("(0,*)", "113", "114", EdgeType.ER);
		model.addEdge("", "113", "116", EdgeType.ER);
		model.addEdge("", "114", "115", EdgeType.ER);
		model.addEdge("(0,*)", "117", "111", EdgeType.ER);
		model.addEdge("", "117", "118", EdgeType.ER);
		model.addEdge("(0,*)", "117", "121", EdgeType.ER);
		model.addEdge("", "121", "122", EdgeType.ER);
		model.addEdge("", "121", "123", EdgeType.ER);
		model.addEdge("", "121", "124", EdgeType.ER);
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
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
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
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryOrder2Arrows(model);
		graph.endUpdate();
	}

	private void addCategoryOrder2Arrows(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.SELECTED_CATEGORICAL);

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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryOrder60Arrows(model);
		graph.endUpdate();
	}

	private void addCategoryOrder60Arrows(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryOrder6Arrows(model);
		graph.endUpdate();
	}

	private void addCategoryOrder6Arrows(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.CATEGORICAL);
		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);

		model.addEdge("8", "114", "115", EdgeType.CATEGORICAL);
		model.addEdge("-8", "115", "114", EdgeType.CATEGORICAL);

		model.addEdge("9", "117", "111", EdgeType.CATEGORICAL);
		model.addEdge("-9", "111", "117", EdgeType.SELECTED_CATEGORICAL);
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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_SELECTED_PROPERTY);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryOrder7Arrows(model);
		graph.endUpdate();
	}

	private void addCategoryOrder7Arrows(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.CATEGORICAL);
		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);

		model.addEdge("8", "114", "115", EdgeType.CATEGORICAL);
		model.addEdge("-8", "115", "114", EdgeType.CATEGORICAL);

		model.addEdge("9", "117", "111", EdgeType.CATEGORICAL);
		model.addEdge("-9", "111", "117", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("-10", "118", "117", EdgeType.CATEGORICAL);
		model.addEdge("10", "117", "118", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("11", "117", "121", EdgeType.CATEGORICAL);
		model.addEdge("-11", "121", "117", EdgeType.CATEGORICAL);

		model.addEdge("12", "121", "122", EdgeType.CATEGORICAL);
		model.addEdge("-12", "122", "121", EdgeType.CATEGORICAL);
		model.addEdge("13", "121", "123", EdgeType.CATEGORICAL);
		model.addEdge("-13", "123", "121", EdgeType.CATEGORICAL);
		model.addEdge("14", "121", "124", EdgeType.CATEGORICAL);
		model.addEdge("-14", "124", "121", EdgeType.CATEGORICAL);

		model.addEdge("12.11", "117", "122", EdgeType.SELECTED_CATEGORICAL);
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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_PROPERTY);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_PROPERTY);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_PROPERTY);

		addCategoryOrder8Arrows(model);
		graph.endUpdate();
	}

	private void addCategoryOrder8Arrows(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.CATEGORICAL);
		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);

		model.addEdge("8", "114", "115", EdgeType.CATEGORICAL);
		model.addEdge("-8", "115", "114", EdgeType.CATEGORICAL);

		model.addEdge("9", "117", "111", EdgeType.CATEGORICAL);
		model.addEdge("-9", "111", "117", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("-10", "118", "117", EdgeType.CATEGORICAL);
		model.addEdge("10", "117", "118", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("11", "117", "121", EdgeType.CATEGORICAL);
		model.addEdge("-11", "121", "117", EdgeType.CATEGORICAL);

		model.addEdge("12", "121", "122", EdgeType.CATEGORICAL);
		model.addEdge("-12", "122", "121", EdgeType.CATEGORICAL);
		model.addEdge("13", "121", "123", EdgeType.CATEGORICAL);
		model.addEdge("-13", "123", "121", EdgeType.CATEGORICAL);
		model.addEdge("14", "121", "124", EdgeType.CATEGORICAL);
		model.addEdge("-14", "124", "121", EdgeType.CATEGORICAL);

		model.addEdge("12.11", "117", "122", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("13.11", "117", "123", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("14.11", "117", "124", EdgeType.PROPERTY_CATEGORICAL);
	}

	public void buildNeo4j(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_KIND);
		model.addCell("101", "Id", 50, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_KIND);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_KIND);
		model.addCell("112", "Number", 170, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("113", "Contact", 290, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("115", "Name", 410, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("116", "Value", 290, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("117", "Items", 170, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("118", "Quantity", 290, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryArrows(model);
		graph.endUpdate();
	}

	public void buildCustomerNode(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_KIND);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
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
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryCustomerNodeArrows(model);
		graph.endUpdate();
	}

	private void addCategoryCustomerNodeArrows(Model model) {
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);
		model.addEdge("1", "100", "101", EdgeType.PROPERTY_CATEGORICAL);

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

	public void buildOrderNode(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_NAME);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_PROPERTY);
		model.addCell("117", "Items", 170, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("118", "Quantity", 290, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryOrderNodeArrows(model);
		graph.endUpdate();
	}

	private void addCategoryOrderNodeArrows(Model model) {
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("7.-4", "111", "116", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("8.6.-4", "111", "115", EdgeType.NAME_CATEGORICAL);

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

	public void buildOrdersEdge(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_KIND);
		model.addCell("111", "Order", 170, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("115", "Name", 410, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("116", "Value", 290, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("117", "Items", 170, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("118", "Quantity", 290, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryOrderEdgeArrows(model);
		graph.endUpdate();
	}

	private void addCategoryOrderEdgeArrows(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("5.-3", "110", "112", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("1.-2", "110", "101", EdgeType.PROPERTY_CATEGORICAL);

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

	public void buildPostgreSQL(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_KIND);
		model.addCell("101", "Id", 50, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("110", "Orders", 50, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_KIND);
		model.addCell("112", "Number", 170, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_KIND);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_KIND);
		model.addCell("115", "Name", 410, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("116", "Value", 290, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("117", "Items", 170, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("118", "Quantity", 290, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryArrows(model);
		graph.endUpdate();
	}

	public void buildContact(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("111", "Order", 170, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_KIND);
		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_PROPERTY);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_PROPERTY);
		model.addCell("117", "Items", 170, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("118", "Quantity", 290, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryContactArrows(model);
		graph.endUpdate();
	}

	private void addCategoryContactArrows(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);

		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.CATEGORICAL);
		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);

		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("-8", "115", "114", EdgeType.CATEGORICAL);
		model.addEdge("8", "114", "115", EdgeType.CATEGORICAL);
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

		model.addEdge("5.4", "113", "112", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("8.6", "113", "115", EdgeType.PROPERTY_CATEGORICAL);
	}

	public void buildCustomer(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_KIND);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
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
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryCustomerArrows(model);
		graph.endUpdate();
	}

	private void addCategoryCustomerArrows(Model model) {

		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);
		model.addEdge("1", "100", "101", EdgeType.PROPERTY_CATEGORICAL);
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

	public void buildItems(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("111", "Order", 170, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("115", "Name", 410, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("116", "Value", 290, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_KIND);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_PROPERTY);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_PROPERTY);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryItemsArrows(model);
		graph.endUpdate();
	}

	private void addCategoryItemsArrows(Model model) {
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

		model.addEdge("-10", "118", "117", EdgeType.CATEGORICAL);
		model.addEdge("10", "117", "118", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("11", "117", "121", EdgeType.CATEGORICAL);
		model.addEdge("-11", "121", "117", EdgeType.CATEGORICAL);

		model.addEdge("12", "121", "122", EdgeType.CATEGORICAL);
		model.addEdge("-12", "122", "121", EdgeType.CATEGORICAL);
		model.addEdge("13", "121", "123", EdgeType.CATEGORICAL);
		model.addEdge("-13", "123", "121", EdgeType.CATEGORICAL);
		model.addEdge("14", "121", "124", EdgeType.CATEGORICAL);
		model.addEdge("-14", "124", "121", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3.9", "117", "101", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("5.9", "117", "112", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("12.11", "117", "122", EdgeType.PROPERTY_CATEGORICAL);
	}

	public void buildOrders(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_KIND);
		model.addCell("111", "Order", 170, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("115", "Name", 410, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("116", "Value", 290, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("117", "Items", 170, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("118", "Quantity", 290, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryOrdersArrows(model);
		graph.endUpdate();
	}

	private void addCategoryOrdersArrows(Model model) {
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

		model.addEdge("1.-2", "110", "101", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("5.-3", "110", "112", EdgeType.PROPERTY_CATEGORICAL);
	}

	public void buildProduct(Graph graph) {
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
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_KIND);
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_PROPERTY);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_PROPERTY);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_PROPERTY);

		addCategoryProductArrows(model);
		graph.endUpdate();
	}

	private void addCategoryProductArrows(Model model) {
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

		model.addEdge("-12", "122", "121", EdgeType.CATEGORICAL);

		model.addEdge("-13", "123", "121", EdgeType.CATEGORICAL);

		model.addEdge("-14", "124", "121", EdgeType.CATEGORICAL);
		model.addEdge("13", "121", "123", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("14", "121", "124", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("12", "121", "122", EdgeType.PROPERTY_CATEGORICAL);
	}

	public void buildType(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("101", "Id", 50, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("110", "Orders", 50, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("111", "Order", 170, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("112", "Number", 170, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("113", "Contact", 290, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_KIND);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_PROPERTY);
		model.addCell("116", "Value", 290, 50, CellType.CATEGORICAL_OBJECT);
		model.addCell("117", "Items", 170, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("118", "Quantity", 290, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryTypeArrows(model);
		graph.endUpdate();
	}

	private void addCategoryTypeArrows(Model model) {
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

		model.addEdge("-8", "115", "114", EdgeType.CATEGORICAL);
		model.addEdge("8", "114", "115", EdgeType.PROPERTY_CATEGORICAL);

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

//	public void buildProductKind3(Graph graph) {
//		Model model = graph.getModel();
//
//		graph.beginUpdate();
//
//		model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
//		model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
//		model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
//		model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
//		model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("121", "Product", 200, 400, CellType.MAPPING_SELECTED_KIND);
//		model.addCell("122", "Id", 200, 500, CellType.MAPPING_AVAILABLE);
//		model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
//		model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);
//
//		addCategoryArrows(model);
//		graph.endUpdate();
//	}
//
//	public void buildProductKind2(Graph graph) {
//		Model model = graph.getModel();
//
//		graph.beginUpdate();
//
//		model.addCell("100", "Customer", 100, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
//		model.addCell("110", "Orders", 100, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("111", "Order", 200, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
//		model.addCell("113", "Contact", 300, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("114", "Type", 400, 200, CellType.CATEGORICAL_OBJECT);
//		model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
//		model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
//		model.addCell("117", "Items", 200, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("121", "Product", 200, 400, CellType.MAPPING_SELECTED_KIND);
//		model.addCell("122", "Id", 200, 500, CellType.MAPPING_PROPERTY);
//		model.addCell("123", "Name", 300, 500, CellType.MAPPING_AVAILABLE);
//		model.addCell("124", "Price", 300, 400, CellType.MAPPING_AVAILABLE);
//
//		addCategoryArrows(model);
//		graph.endUpdate();
//	}
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
//	public void buildPostgreSQLInstance(Graph graph) {
//		Model model = graph.getModel();
//
//		graph.beginUpdate();
//
//		model.addCell("100", "Customer", 100, 300, CellType.MAPPING_KIND);
//		model.addCell("101", "Id", 100, 400, CellType.CATEGORICAL_OBJECT);
//		model.addCell("110", "Orders", 100, 200, CellType.MAPPING_KIND);
//		model.addCell("111", "Order", 200, 200, CellType.MAPPING_KIND);
//		model.addCell("112", "Number", 200, 100, CellType.CATEGORICAL_OBJECT);
//		model.addCell("113", "Contact", 300, 200, CellType.MAPPING_KIND);
//		model.addCell("114", "Type", 400, 200, CellType.MAPPING_KIND);
//		model.addCell("115", "Name", 400, 100, CellType.CATEGORICAL_OBJECT);
//		model.addCell("116", "Value", 300, 100, CellType.CATEGORICAL_OBJECT);
//		model.addCell("117", "Items", 200, 300, CellType.MAPPING_KIND);
//		model.addCell("118", "Quantity", 300, 300, CellType.CATEGORICAL_OBJECT);
//		model.addCell("121", "Product", 200, 400, CellType.MAPPING_KIND);
//		model.addCell("122", "Id", 200, 500, CellType.CATEGORICAL_OBJECT);
//		model.addCell("123", "Name", 300, 500, CellType.CATEGORICAL_OBJECT);
//		model.addCell("124", "Price", 300, 400, CellType.CATEGORICAL_OBJECT);
//
//		addCategoryArrows(model);
//		graph.endUpdate();
//	}
//
//	public void buildNeo4jInstance(Graph graph) {
//		System.out.println("TODO");
//	}
//
//	public void buildMongoDBInstance(Graph graph) {
//		System.out.println("TODO");
//	}
//
//	public void buildRiakKVInstance(Graph graph) {
//		System.out.println("TODO");
//	}
//
//	public void buildCassandraInstance(Graph graph) {
//		System.out.println("TODO");
//	}
	public void buildPostgreSQLOrder_0(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_SELECTED_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryPostgreSQLOrderArrows_0(model);
		graph.endUpdate();
	}

	private void addCategoryPostgreSQLOrderArrows_0(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);

		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.CATEGORICAL);

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

//		model.addEdge("1.-2.3", "111", "101", EdgeType.CATEGORICAL);
	}

	public void buildPostgreSQLOrder_1(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_SELECTED_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryPostgreSQLOrderArrows_1(model);
		graph.endUpdate();
	}

	private void addCategoryPostgreSQLOrderArrows_1(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);

		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.CATEGORICAL);

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

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_INCOMPLETE_CATEGORICAL);
	}

	public void buildPostgreSQLOrder_2(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_SELECTED_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryPostgreSQLOrderArrows_2(model);
		graph.endUpdate();
	}

	private void addCategoryPostgreSQLOrderArrows_2(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);

		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

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

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);
	}

	public void buildPostgreSQLOrder_3(Graph graph) {
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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryPostgreSQLOrderArrows_3(model);
		graph.endUpdate();
	}

	private void addCategoryPostgreSQLOrderArrows_3(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);

		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.CATEGORICAL);
		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);

		model.addEdge("8", "114", "115", EdgeType.CATEGORICAL);
		model.addEdge("-8", "115", "114", EdgeType.CATEGORICAL);

		model.addEdge("9", "117", "111", EdgeType.CATEGORICAL);
		model.addEdge("-9", "111", "117", EdgeType.PROPERTY_INCOMPLETE_CATEGORICAL);
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

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);
	}

	public void buildPostgreSQLOrder_4(Graph graph) {
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
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_PROPERTY);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryPostgreSQLOrderArrows_4(model);
		graph.endUpdate();
	}

	private void addCategoryPostgreSQLOrderArrows_4(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);

		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.CATEGORICAL);
		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);

		model.addEdge("8", "114", "115", EdgeType.CATEGORICAL);
		model.addEdge("-8", "115", "114", EdgeType.CATEGORICAL);

		model.addEdge("9", "117", "111", EdgeType.CATEGORICAL);
		model.addEdge("-9", "111", "117", EdgeType.PROPERTY_INCOMPLETE_CATEGORICAL);
		model.addEdge("-10", "118", "117", EdgeType.CATEGORICAL);
		model.addEdge("10", "117", "118", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("11", "117", "121", EdgeType.CATEGORICAL);
		model.addEdge("-11", "121", "117", EdgeType.CATEGORICAL);

		model.addEdge("12", "121", "122", EdgeType.CATEGORICAL);
		model.addEdge("-12", "122", "121", EdgeType.CATEGORICAL);
		model.addEdge("13", "121", "123", EdgeType.CATEGORICAL);
		model.addEdge("-13", "123", "121", EdgeType.CATEGORICAL);
		model.addEdge("14", "121", "124", EdgeType.CATEGORICAL);
		model.addEdge("-14", "124", "121", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);
	}

	public void buildPostgreSQLOrder_5(Graph graph) {
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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_PROPERTY);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_PROPERTY);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_PROPERTY);

		addCategoryPostgreSQLOrderArrows_5(model);
		graph.endUpdate();
	}

	private void addCategoryPostgreSQLOrderArrows_5(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);

		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.CATEGORICAL);
		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);

		model.addEdge("8", "114", "115", EdgeType.CATEGORICAL);
		model.addEdge("-8", "115", "114", EdgeType.CATEGORICAL);

		model.addEdge("9", "117", "111", EdgeType.CATEGORICAL);
		model.addEdge("-9", "111", "117", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("-10", "118", "117", EdgeType.CATEGORICAL);
		model.addEdge("10", "117", "118", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("11", "117", "121", EdgeType.CATEGORICAL);
		model.addEdge("-11", "121", "117", EdgeType.CATEGORICAL);

		model.addEdge("12", "121", "122", EdgeType.CATEGORICAL);
		model.addEdge("-12", "122", "121", EdgeType.CATEGORICAL);
		model.addEdge("13", "121", "123", EdgeType.CATEGORICAL);
		model.addEdge("-13", "123", "121", EdgeType.CATEGORICAL);
		model.addEdge("14", "121", "124", EdgeType.CATEGORICAL);
		model.addEdge("-14", "124", "121", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("12.11", "117", "122", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("13.11", "117", "123", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("14.11", "117", "124", EdgeType.PROPERTY_CATEGORICAL);
	}

//	public void buildOrder(Graph graph) {
//		Model model = graph.getModel();
//
//		graph.beginUpdate();
//
//		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
//		model.addCell("101", "Id", 50, 410, CellType.CATEGORICAL_OBJECT);
//		model.addCell("110", "Orders", 50, 170, CellType.CATEGORICAL_OBJECT);
//		model.addCell("111", "Order", 170, 170, CellType.CATEGORICAL_OBJECT);
//		model.addCell("112", "Number", 170, 50, CellType.CATEGORICAL_OBJECT);
//		model.addCell("113", "Contact", 290, 170, CellType.CATEGORICAL_OBJECT);
//		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
//		model.addCell("115", "Name", 410, 50, CellType.CATEGORICAL_OBJECT);
//		model.addCell("116", "Value", 290, 50, CellType.CATEGORICAL_OBJECT);
//		model.addCell("117", "Items", 170, 290, CellType.CATEGORICAL_OBJECT);
//		model.addCell("118", "Quantity", 290, 290, CellType.CATEGORICAL_OBJECT);
//		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
//		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
//		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
//		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);
//
//		addCategoryOrderArrows(model);
//		graph.endUpdate();
//	}
	
	
	
	
	public void buildMongoDBOrder_0(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_SELECTED_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryMongoDBOrderArrows_0(model);
		graph.endUpdate();
	}

	private void addCategoryMongoDBOrderArrows_0(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

//		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.CATEGORICAL);

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
	
	public void buildMongoDBOrder_1(Graph graph) {
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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryMongoDBOrderArrows_1(model);
		graph.endUpdate();
	}
	
	private void addCategoryMongoDBOrderArrows_1(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

//		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.CATEGORICAL);

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
	
	public void buildMongoDBOrder_2(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryMongoDBOrderArrows_2(model);
		graph.endUpdate();
	}
	
	private void addCategoryMongoDBOrderArrows_2(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_INCOMPLETE_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.CATEGORICAL);

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
	
	public void buildMongoDBOrder_3(Graph graph) {
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
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryMongoDBOrderArrows_3(model);
		graph.endUpdate();
	}
	
	private void addCategoryMongoDBOrderArrows_3(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

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
	
	public void buildMongoDBOrder_4(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_AVAILABLE_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_SELECTED_PROPERTY);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryMongoDBOrderArrows_4(model);
		graph.endUpdate();
	}
	
	private void addCategoryMongoDBOrderArrows_4(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.PROPERTY_INCOMPLETE_CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

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
	
	public void buildMongoDBOrder_5(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_SELECTED_PROPERTY);
		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_PROPERTY);
		model.addCell("117", "Items", 170, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("118", "Quantity", 290, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 50, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("123", "Name", 290, 530, CellType.CATEGORICAL_OBJECT);
		model.addCell("124", "Price", 290, 410, CellType.CATEGORICAL_OBJECT);

		addCategoryMongoDBOrderArrows_5(model);
		graph.endUpdate();
	}
	
	private void addCategoryMongoDBOrderArrows_5(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.PROPERTY_INCOMPLETE_CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);
		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.PROPERTY_INCOMPLETE_CATEGORICAL);

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
	
	public void buildMongoDBOrder_6(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_SELECTED_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE_NAME);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryMongoDBOrderArrows_6(model);
		graph.endUpdate();
	}
	
	private void addCategoryMongoDBOrderArrows_6(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("8.6", "113", "115", EdgeType.NAME_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);
		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.PROPERTY_CATEGORICAL);

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
	
	public void buildMongoDBOrder_7(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_AVAILABLE_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("114", "Type", 410, 170, CellType.MAPPING_AVAILABLE);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_AVAILABLE_NAME);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_AVAILABLE_PROPERTY);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_SELECTED_PROPERTY);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_AVAILABLE);
		model.addCell("121", "Product", 170, 410, CellType.MAPPING_AVAILABLE);
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_AVAILABLE);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_AVAILABLE);

		addCategoryMongoDBOrderArrows_7(model);
		graph.endUpdate();
	}
	
	private void addCategoryMongoDBOrderArrows_7(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("8.6", "113", "115", EdgeType.NAME_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);
		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("8", "114", "115", EdgeType.CATEGORICAL);
		model.addEdge("-8", "115", "114", EdgeType.CATEGORICAL);

		model.addEdge("9", "117", "111", EdgeType.CATEGORICAL);
		model.addEdge("-9", "111", "117", EdgeType.PROPERTY_INCOMPLETE_CATEGORICAL);
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
	
	public void buildMongoDBOrder_8(Graph graph) {
		Model model = graph.getModel();

		graph.beginUpdate();

		model.addCell("100", "Customer", 50, 290, CellType.CATEGORICAL_OBJECT);
		model.addCell("101", "Id", 50, 410, CellType.MAPPING_PROPERTY);
		model.addCell("110", "Orders", 50, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("111", "Order", 170, 170, CellType.MAPPING_KIND);
		model.addCell("112", "Number", 170, 50, CellType.MAPPING_PROPERTY);
		model.addCell("113", "Contact", 290, 170, CellType.MAPPING_PROPERTY);
		model.addCell("114", "Type", 410, 170, CellType.CATEGORICAL_OBJECT);
		model.addCell("115", "Name", 410, 50, CellType.MAPPING_NAME);
		model.addCell("116", "Value", 290, 50, CellType.MAPPING_PROPERTY);
		model.addCell("117", "Items", 170, 290, CellType.MAPPING_PROPERTY);
		model.addCell("118", "Quantity", 290, 290, CellType.MAPPING_PROPERTY);
		model.addCell("121", "Product", 170, 410, CellType.CATEGORICAL_OBJECT);
		model.addCell("122", "Id", 50, 530, CellType.MAPPING_PROPERTY);
		model.addCell("123", "Name", 290, 530, CellType.MAPPING_PROPERTY);
		model.addCell("124", "Price", 290, 410, CellType.MAPPING_PROPERTY);

		addCategoryMongoDBOrderArrows_8(model);
		graph.endUpdate();
	}
	
	private void addCategoryMongoDBOrderArrows_8(Model model) {
		model.addEdge("1", "100", "101", EdgeType.CATEGORICAL);
		model.addEdge("-1", "101", "100", EdgeType.CATEGORICAL);

		model.addEdge("1.-2.3", "111", "101", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("8.6", "113", "115", EdgeType.NAME_CATEGORICAL);
		model.addEdge("12.11", "117", "122", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("13.11", "117", "123", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("14.11", "117", "124", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("2", "100", "110", EdgeType.CATEGORICAL);
		model.addEdge("-2", "110", "100", EdgeType.CATEGORICAL);
		model.addEdge("3", "111", "110", EdgeType.CATEGORICAL);
		model.addEdge("-3", "110", "111", EdgeType.CATEGORICAL);

		model.addEdge("4", "113", "111", EdgeType.CATEGORICAL);
		model.addEdge("-4", "111", "113", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("-5", "112", "111", EdgeType.CATEGORICAL);
		model.addEdge("5", "111", "112", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("6", "113", "114", EdgeType.CATEGORICAL);
		model.addEdge("-6", "114", "113", EdgeType.CATEGORICAL);
		model.addEdge("-7", "116", "113", EdgeType.CATEGORICAL);
		model.addEdge("7", "113", "116", EdgeType.PROPERTY_CATEGORICAL);

		model.addEdge("8", "114", "115", EdgeType.CATEGORICAL);
		model.addEdge("-8", "115", "114", EdgeType.CATEGORICAL);

		model.addEdge("9", "117", "111", EdgeType.CATEGORICAL);
		model.addEdge("-9", "111", "117", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("-10", "118", "117", EdgeType.CATEGORICAL);
		model.addEdge("10", "117", "118", EdgeType.PROPERTY_CATEGORICAL);
		model.addEdge("11", "117", "121", EdgeType.CATEGORICAL);
		model.addEdge("-11", "121", "117", EdgeType.CATEGORICAL);

		model.addEdge("12", "121", "122", EdgeType.CATEGORICAL);
		model.addEdge("-12", "122", "121", EdgeType.CATEGORICAL);
		model.addEdge("13", "121", "123", EdgeType.CATEGORICAL);
		model.addEdge("-13", "123", "121", EdgeType.CATEGORICAL);
		model.addEdge("14", "121", "124", EdgeType.CATEGORICAL);
		model.addEdge("-14", "124", "121", EdgeType.CATEGORICAL);
	}
}
