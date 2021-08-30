/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.editor;

/**
 *
 * @author pavel.contos
 */
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

// https://mvnrepository.com/artifact/org.graphstream/gs-core
public class Tutorial {

	public static void main(String args[]) {
//		System.setProperty("org.graphstream.ui", "swing");
		System.setProperty("org.graphstream.ui", "javafx");

//		Graph graph = new SingleGraph("Tutorial 1");
		MultiGraph graph = new MultiGraph("category");

		graph.addNode("Friends");
		var x = graph.getNode("Friends");
		x.setAttribute("name", "Friends");
		graph.addNode("Customer");
		graph.addNode("Id");
		graph.addNode("Name");
		graph.addNode("Surname");
		graph.addNode("Address");
		graph.addNode("Street");
		graph.addNode("City");
		graph.addNode("PostalCode");
		graph.addNode("Orders");
		graph.addNode("Order");
		graph.addNode("Number");
		graph.addNode("Contact");
		graph.addNode("Value");
		graph.addNode("Type");
		graph.addNode("NameOfType");
		graph.addNode("Items");
		graph.addNode("ItemsQuantity");
		graph.addNode("Product");
		graph.addNode("Cart");
		graph.addNode("Quantity");
		graph.addNode("ProductId");
		graph.addNode("ProductName");
		graph.addNode("Price");
		graph.addNode("Audiobook");
		graph.addNode("Time");
		graph.addNode("Book");
		graph.addNode("Pages");
		graph.addEdge("1", "Friends", "Customer");
		graph.addEdge("2", "Customer", "Friends");
		graph.addEdge("3", "Friends", "Customer");
		graph.addEdge("4", "Customer", "Friends");
		graph.addEdge("5", "Customer", "Id");
		graph.addEdge("6", "Id", "Customer");
		graph.addEdge("7", "Customer", "Name");
		graph.addEdge("8", "Name", "Customer");
		graph.addEdge("9", "Customer", "Surname");
		graph.addEdge("10", "Surname", "Customer");
		graph.addEdge("11", "Customer", "Address");
		graph.addEdge("12", "Address", "Customer");
		graph.addEdge("13", "Address", "Street");
		graph.addEdge("14", "Street", "Address");
		graph.addEdge("15", "Address", "City");
		graph.addEdge("16", "City", "Address");
		graph.addEdge("17", "Address", "PostalCode");
		graph.addEdge("18", "PostalCode", "Address");
		graph.addEdge("19", "Customer", "Orders");
		graph.addEdge("20", "Orders", "Customer");
		graph.addEdge("21", "Orders", "Order");
		graph.addEdge("22", "Order", "Orders");
		graph.addEdge("23", "Order", "Number");
		graph.addEdge("24", "Number", "Order");
		graph.addEdge("25", "Order", "Contact");
		graph.addEdge("26", "Contact", "Order");
		graph.addEdge("27", "Contact", "Value");
		graph.addEdge("28", "Value", "Contact");
		graph.addEdge("29", "Contact", "Type");
		graph.addEdge("30", "Type", "Contact");
		graph.addEdge("31", "Type", "NameOfType");
		graph.addEdge("32", "NameOfType", "Type");
		graph.addEdge("33", "Order", "Items");
		graph.addEdge("34", "Items", "Order");
		graph.addEdge("35", "Items", "ItemsQuantity");
		graph.addEdge("36", "ItemsQuantity", "Items");
		graph.addEdge("37", "Items", "Product");
		graph.addEdge("38", "Product", "Items");
		graph.addEdge("39", "Customer", "Cart");
		graph.addEdge("40", "Cart", "Customer");
		graph.addEdge("41", "Cart", "Quantity");
		graph.addEdge("42", "Quantity", "Cart");
		graph.addEdge("43", "Cart", "Product");
		graph.addEdge("44", "Product", "Cart");
		graph.addEdge("45", "Product", "ProductId");
		graph.addEdge("46", "ProductId", "Product");
		graph.addEdge("47", "Product", "ProductName");
		graph.addEdge("48", "ProductName", "Product");
		graph.addEdge("49", "Product", "Price");
		graph.addEdge("50", "Price", "Product");
		graph.addEdge("51", "Product", "Audiobook");
		graph.addEdge("52", "Audiobook", "Product");
		graph.addEdge("53", "Product", "Book");
		graph.addEdge("54", "Book", "Product");
		graph.addEdge("55", "Audiobook", "Time");
		graph.addEdge("56", "Time", "Audiobook");
		graph.addEdge("57", "Book", "Pages");
		graph.addEdge("58", "Pages", "Book");

		for (Node node : graph) {
			node.setAttribute("ui.label", node.getId());
			node.setAttribute("ui.style", "fill-color: rgb(" + (int) (Math.random() * 255) + "," + (int) (Math.random() * 255) + "," + (int) (Math.random() * 255) + ");");
		}

		for (Edge edge : graph.getEachEdge()) {
			edge.setAttribute("ui.label", edge.getId());
		}
		graph.display();
	}
}
