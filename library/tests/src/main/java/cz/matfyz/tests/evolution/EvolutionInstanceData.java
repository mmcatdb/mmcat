package cz.matfyz.tests.evolution;

import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Helper class for adding data to instances in evolution tests.
 * 
 * Extends the basic PostgreSQL/MongoDB helpers with additional scenarios
 * specifically useful for testing schema evolution.
 */
public class EvolutionInstanceData {

    private EvolutionInstanceData() {}

    // === Order with all relationships ===

    /**
     * Adds a complete order with customer, address, and items.
     * This creates a rich data structure useful for testing complex evolutions.
     */
    public static void addCompleteOrder(
            InstanceBuilder builder,
            String orderNumber,
            String customerName,
            String street,
            String city,
            String zip) {
        
        // Create the order
        final var order = builder
            .value(Schema.order_number, orderNumber)
            .objex(Schema.order);
        
        // Create customer and link to order
        final var customer = builder
            .value(Schema.customer_name, customerName)
            .objex(Schema.customer);
        builder.morphism(Schema.order_customer, order, customer);
        
        // Create address and link to order
        final var address = builder
            .value(Schema.address_street, street)
            .value(Schema.address_city, city)
            .value(Schema.address_zip, zip)
            .objex(Schema.address);
        builder.morphism(Schema.order_address, order, address);
    }

    /**
     * Adds an item to an existing order.
     */
    public static void addItemToOrder(
            InstanceBuilder builder,
            int orderIndex,
            String productId,
            String productLabel,
            String productPrice,
            String quantity) {
        
        final var order = builder.getRow(Schema.order, orderIndex);
        final var orderNumber = order.tryGetScalarValue(Schema.order_number.signature());
        
        // Create product
        final var product = builder
            .value(Schema.product_id, productId)
            .value(Schema.product_label, productLabel)
            .value(Schema.product_price, productPrice)
            .objex(Schema.product);
        
        // Create item linking order and product
        final var item = builder
            .value(Schema.item_number, orderNumber)
            .value(Schema.item_id, productId)
            .value(Schema.item_quantity, quantity)
            .objex(Schema.item);
        
        builder.morphism(Schema.item_order, item, order);
        builder.morphism(Schema.item_product, item, product);
    }

    /**
     * Adds a contact to an existing order.
     */
    public static void addContactToOrder(
            InstanceBuilder builder,
            int orderIndex,
            String contactType,
            String contactValue) {
        
        final var order = builder.getRow(Schema.order, orderIndex);
        final var orderNumber = order.tryGetScalarValue(Schema.order_number.signature());
        
        final var contact = builder
            .value(Schema.contact_number, orderNumber)
            .value(Schema.contact_type, contactType)
            .value(Schema.contact_value, contactValue)
            .objex(Schema.contact);
        
        builder.morphism(Schema.contact_order, contact, order);
    }

    // === Simple data for basic tests ===

    /**
     * Adds a simple order with just a number (no relationships).
     */
    public static void addSimpleOrder(InstanceBuilder builder, String orderNumber) {
        builder.value(Schema.order_number, orderNumber).objex(Schema.order);
    }

    /**
     * Adds a simple product.
     */
    public static void addSimpleProduct(InstanceBuilder builder, String id, String label, String price) {
        builder
            .value(Schema.product_id, id)
            .value(Schema.product_label, label)
            .value(Schema.product_price, price)
            .objex(Schema.product);
    }

    /**
     * Adds a customer with name.
     */
    public static void addSimpleCustomer(InstanceBuilder builder, String name) {
        builder.value(Schema.customer_name, name).objex(Schema.customer);
    }

    // === Data for new dynamically created objexes ===

    /**
     * Adds data to a dynamically created objex (for testing CreateObjex with data).
     * 
     * @param builder The instance builder
     * @param objex The BuilderObjex reference (from schema evolution)
     * @param morphism The morphism to the value objex
     * @param value The value to store
     */
    public static void addValueToObjex(
            InstanceBuilder builder,
            BuilderObjex objex,
            BuilderMorphism morphism,
            String value) {
        
        builder.value(morphism, value).objex(objex);
    }
}
