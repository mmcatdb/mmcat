/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.dummy;

import javafx.scene.control.TextArea;

/**
 *
 * @author pavel.koupil
 */
public enum DummyDDLScenario {
	INSTANCE;

	public void createMongoKinds(TextArea textArea) {
		textArea.setText("""
                         db.createCollection(\"orders\")
                         """);
	}

	public void buildType(TextArea textArea) {
		textArea.setText("""
                        CREATE TABLE Type (
                          Name TEXT
                        );
                        """);
	}

//	public void buildProduct(TextArea textArea) {
//		textArea.setText("""
//                              CREATE TABLE Customer (
//                              id TEXT
//                              );
//                        
//                              CREATE TABLE Orders (
//                              id TEXT,
//                              number TEXT,
//                              items JSONB
//                              );
//                                                
//                              CREATE TABLE Contact (
//                              id TEXT,
//                              number TEXT,
//                              name TEXT,
//                              value TEXT
//                              );
//                        
//                              CREATE TABLE Type (
//                              Name TEXT
//                              );
//                        """);
//	}
	public void buildOrders(TextArea textArea) {
		textArea.setText("""
                        CREATE TABLE Orders (
                          id TEXT,
                          number TEXT,
                          items JSONB
                        );
                        """);
	}

//	public void buildOrder(TextArea textArea) {
//		textArea.setText("""
//                              CREATE TABLE Customer (
//                              id TEXT
//                              );
//                        
//                              CREATE TABLE Orders (
//                              id TEXT,
//                              number TEXT,
//                              items JSONB
//                              );
//                                                
//                              CREATE TABLE Contact (
//                              id TEXT,
//                              number TEXT,
//                              name TEXT,
//                              value TEXT
//                              );
//                        
//                              CREATE TABLE Type (
//                              Name TEXT
//                              );
//                        """);
//	}
//	public void buildItems(TextArea textArea) {
//		textArea.setText("""
//                              CREATE TABLE Customer (
//                              id TEXT
//                              );
//                        
//                              CREATE TABLE Orders (
//                              id TEXT,
//                              number TEXT,
//                              items JSONB
//                              );
//                                                
//                              CREATE TABLE Contact (
//                              id TEXT,
//                              number TEXT,
//                              name TEXT,
//                              value TEXT
//                              );
//                        
//                              CREATE TABLE Type (
//                              Name TEXT
//                              );
//                        """);
//	}
	public void buildCustomer(TextArea textArea) {
		textArea.setText("""
                        CREATE TABLE Customer (
                          id TEXT
                        );
                        """);
	}

	public void buildContact(TextArea textArea) {
		textArea.setText("""
                        CREATE TABLE Contact (
                          id TEXT,
                          number TEXT,
                          name TEXT,
                          value TEXT
                        );
                        """);
	}

	public void createPostgreSQLKinds(TextArea textArea) {
		textArea.setText("""
                        CREATE TABLE Customer (
                          id TEXT
                        );
                        
                        CREATE TABLE Contact (
                          id TEXT,
                          number TEXT,
                          name TEXT,
                          value TEXT
                        );
                   
                        CREATE TABLE Orders (
                          id TEXT,
                          number TEXT,
                          items JSONB
                        );
                                                
                        CREATE TABLE Type (
                          Name TEXT
                        );
                        """);
	}

	public void buildPostgreSQLOrder_0(TextArea textArea) {
		textArea.setText("""
                        CREATE TABLE Orders (
                        );
                        """);
	}

	public void buildPostgreSQLOrder_1(TextArea textArea) {
		textArea.setText("""
                        CREATE TABLE Orders (
                          id TEXT
                        );
                        """);
	}

	public void buildPostgreSQLOrder_2(TextArea textArea) {
		textArea.setText("""
                        CREATE TABLE Orders (
                          id TEXT,
                          number TEXT
                        );
                        """);
	}

	public void buildPostgreSQLOrder_3(TextArea textArea) {
		textArea.setText("""
                        CREATE TABLE Orders (
                          id TEXT,
                          number TEXT,
                          items JSONB
                        );
                        """);
	}

	public void buildPostgreSQLOrder_4(TextArea textArea) {
		textArea.setText("""
                        CREATE TABLE Orders (
                          id TEXT,
                          number TEXT,
                          items JSONB
                        );
                        """);
	}

	public void buildPostgreSQLOrder_5(TextArea textArea) {
		textArea.setText("""
                        CREATE TABLE Orders (
                          id TEXT,
                          number TEXT,
                          items JSONB
                        );
                        """);
	}

}
