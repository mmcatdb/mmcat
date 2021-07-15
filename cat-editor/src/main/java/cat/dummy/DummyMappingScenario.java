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
public enum DummyMappingScenario {
	INSTANCE;

	public void buildOrderCollection(TextArea textArea) {
		textArea.setText("""
                         {
                         }""");
	}

	public void buildOrderCollection_GroupingId(TextArea textArea) {
		textArea.setText("""
                         {
                           _id :
                         }""");
	}

	public void buildOrderCollection_CompleteId(TextArea textArea) {
		textArea.setText("""
                         {
                           _id : {
                             id : 1.-2.3,
                             number : 5
                           }
                         }""");
	}

//    public void buildMongoOrder_3_Contact(TextArea textArea) {
//        textArea.setText("""
//                         {
//                           _id : {
//                             id : 1.1.1,
//                             number : 2
//                           },
//                           contact : 6
//                         }""");
//    }
//
//    public void buildMongoOrder_4_ContactTypeName(TextArea textArea) {
//        textArea.setText("""
//                         {
//                           _id : {
//                             id : 1.1.1,
//                             number : 2
//                           },
//                           contact : 6 {
//                             : 7
//                           }
//                         }""");
//    }
//
//    public void buildMongoOrder_5_ContactTypeSelectedName(TextArea textArea) {
//        textArea.setText("""
//                         {
//                           _id : {
//                             id : 1.1.1,
//                             number : 2
//                           },
//                           contact : 6 {
//                             8.9 : 7
//                           }
//                         }""");
//    }
	public void buildOrderCollection_Items(TextArea textArea) {
		textArea.setText("""
                         {
                           _id : {
                             id : 1.-2.3,
                             number : 5
                           }
                         }""");
	}

	public void buildOrderCollection_Items2(TextArea textArea) {
		textArea.setText("""
                         {
                           _id : {
                             id : 1.-2.3,
                             number : 5
                           },
                           items : -9
                         }""");
	}

	public void buildOrderCollection_InliningProduct(TextArea textArea) {
		textArea.setText("""
                         {
                           _id : {
                             id : 1.-2.3,
                             number : 5
                           },
                           items : -9 {
                             quantity : 10,
                             id : 12.11
                           }
                         }""");
	}

	public void buildOrderCollection_Complete(TextArea textArea) {
		textArea.setText("""
                         {
                           _id : {
                             id : 1.-2.3,
                             number : 5
                           },
                           items : -9 {
                             quantity : 10,
                             id : 12.11,
                             name : 13.11,
                             price : 14.11
                           }
                         }""");
	}

//	public void buildProductKind(TextArea textArea) {
//		textArea.setText("""
//                         {
//                         }""");
//	}
//
//	public void buildProductKind2(TextArea textArea) {
//		textArea.setText("""
//                         {
//                           id : 1
//                         }""");
//	}
	public void buildProduct(TextArea textArea) {
		textArea.setText("""
                         {
                           id : 12,
                           name : 13,
                           price : 14
                         }""");
	}

	public void buildCustomer(TextArea textArea) {
		textArea.setText("""
                         {
                           id : 1
                         }""");
	}

	public void buildOrders(TextArea textArea) {
		textArea.setText("""
                         {
                           id : 1.-2,
                           number : 5.-3
                         }""");
	}

	public void buildOrder(TextArea textArea) {
		textArea.setText("""
                         {
                           id : 1.-2.3,
                           number : 5
                         }""");
	}

	public void buildItems(TextArea textArea) {
		textArea.setText("""
                         {
                           id : 1.-2.3.9,
                           number : 5.9,
                           productId : 12.11,
                           quantity: 10
                         }""");
	}

	public void buildContact(TextArea textArea) {
		textArea.setText("""
                         {
                           id : 1.-2.3.4,
                           number : 5.4,
                           value : 7,
                           name : 8.6
                         }""");
	}

	public void buildType(TextArea textArea) {
		textArea.setText("""
                         {
                           name : 8
                         }""");
	}

	public void buildCustomerNode(TextArea textArea) {
		textArea.setText("""
                         {
                           id : 1
                         }""");
	}

	public void buildOrderNode(TextArea textArea) {
		textArea.setText("""
                         {
                           id : 1.-2.3
                           number : 5,
                           8.6.-4 : 7.-4            
                         }""");
	}

	public void buildOrdersEdge(TextArea textArea) {
		textArea.setText("""
                         {
                           id : 1.-2,
                           number : 5.-3
                         }""");
	}

}
