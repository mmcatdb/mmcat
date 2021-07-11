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

    public void buildProductKind(TextArea textArea) {
        textArea.setText("""
                         {
                         }""");
    }

    public void buildProductKind2(TextArea textArea) {
        textArea.setText("""
                         {
                           id : 1
                         }""");
    }

    public void buildProductKind3(TextArea textArea) {
        textArea.setText("""
                         {
                           id : 1,
                           name : 2,
                           price : 3
                         }""");
    }

    public void buildProductCustomer(TextArea textArea) {
        textArea.setText("""
                         {
                           id : 1
                         }""");
    }

    public void buildProductOrders(TextArea textArea) {
        textArea.setText("""
                         {
                           id : 1.1,
                           number : 2.2
                         }""");
    }

    public void buildProductOrder(TextArea textArea) {
        textArea.setText("""
                         {
                           id : 1.1.2,
                           number : 2
                         }""");
    }

    public void buildProductItems(TextArea textArea) {
        textArea.setText("""
                         {
                           id : 1.1.1.1,
                           number : 2.2,
                           productId : 3.3,
                           quantity: 4
                         }""");
    }

    public void buildProductContact(TextArea textArea) {
        textArea.setText("""
                         {
                           id : 1.1.1.1,
                           number : 2.2,
                           value : 3,
                           name : 4.4
                         }""");
    }

    public void buildProductType(TextArea textArea) {
        textArea.setText("""
                         {
                           name : 4
                         }""");
    }

    public void buildMongoOrder_0(TextArea textArea) {
        textArea.setText("""
                         {
                         }""");
    }

    public void buildMongoOrder_1_GroupingId(TextArea textArea) {
        textArea.setText("""
                         {
                           _id :
                         }""");
    }

    public void buildMongoOrder_2_CompleteId(TextArea textArea) {
        textArea.setText("""
                         {
                           _id : {
                             id : 1.1.1,
                             number : 2
                           }
                         }""");
    }

    public void buildMongoOrder_3_Contact(TextArea textArea) {
        textArea.setText("""
                         {
                           _id : {
                             id : 1.1.1,
                             number : 2
                           },
                           contact : 6
                         }""");
    }

    public void buildMongoOrder_4_ContactTypeName(TextArea textArea) {
        textArea.setText("""
                         {
                           _id : {
                             id : 1.1.1,
                             number : 2
                           },
                           contact : 6 {
                             : 7
                           }
                         }""");
    }

    public void buildMongoOrder_5_ContactTypeSelectedName(TextArea textArea) {
        textArea.setText("""
                         {
                           _id : {
                             id : 1.1.1,
                             number : 2
                           },
                           contact : 6 {
                             8.9 : 7
                           }
                         }""");
    }

    public void buildMongoOrder_60_Items(TextArea textArea) {
        textArea.setText("""
                         {
                           _id : {
                             id : 1.1.1,
                             number : 2
                           },
                           contact : 6 {
                             8.9 : 7
                           }
                         }""");
    }

    public void buildMongoOrder_6_Items(TextArea textArea) {
        textArea.setText("""
                         {
                           _id : {
                             id : 1.1.1,
                             number : 2
                           },
                           contact : 6 {
                             8.9 : 7
                           },
                           items : 11
                         }""");
    }

    public void buildMongoOrder_7_InliningProduct(TextArea textArea) {
        textArea.setText("""
                         {
                           _id : {
                             id : 1.1.1,
                             number : 2
                           },
                           contact : 6 {
                             8.9 : 7
                           },
                           items : 11 {
                             id : 12
                           }
                         }""");
    }

    public void buildMongoOrder_8_Complete(TextArea textArea) {
        textArea.setText("""
                         {
                           _id : {
                             id : 1.1.1,
                             number : 2
                           },
                           contact : 6 {
                             8.9 : 7
                           },
                           items : 11 {
                             id : 12,
                             name : 13,
                             price : 14
                           }
                         }""");
    }

}
