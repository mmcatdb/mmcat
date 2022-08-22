package cz.cuni.matfyz.server;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.ActiveDomainRow;
import cz.cuni.matfyz.core.instance.ActiveMappingRow;
import cz.cuni.matfyz.core.instance.IdWithValues;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.Id;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaMorphism.Max;
import cz.cuni.matfyz.core.schema.SchemaMorphism.Min;
import cz.cuni.matfyz.core.utils.Statistics;
import cz.cuni.matfyz.core.utils.Statistics.Counter;
import cz.cuni.matfyz.core.utils.Statistics.Interval;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.service.DatabaseService;
import cz.cuni.matfyz.server.service.WrapperService;
import cz.cuni.matfyz.transformations.algorithms.UniqueIdProvider;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;

/**
 *
 * @author jachymb.bartik
 */
@SpringBootTest
public class EvolutionManagementTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionManagementTests.class);

    private TestData data;


    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private WrapperService wrapperService;

    @BeforeEach
    public void setUp() {
        UniqueIdProvider.reset();
        data = new TestData();
        Statistics.reset();
        //schema = data.createDefaultSchemaCategory();
        //order = schema.getObject(data.getOrderKey());
    }

    @Test
	public void joinMoveTest() throws Exception {
        final int repetitions = 5;
        final int[] batches = new int[] { 1, 2 };
		//final int[] batches = new int[] { 1, 2, 4, 8, 16, 32, 64, 128, 256 };
        //final int[] batches = new int[] { 1, 2, 4, 8, 16, 32, 64 };
        //final int[] batches = new int[] { 48, 80, 96 };
        //final int[] batches = new int[] { 128 };
        final int mongodbDatabaseId = 4;

        Database database = databaseService.find(mongodbDatabaseId);
        AbstractPullWrapper pullWrapper = wrapperService.getPullWraper(database);

        for (int i = 0; i < repetitions; i++) {
			LOGGER.info("Repetition: " + (i + 1));

			for (var batch : batches) {
                
                // Create mapping and schema
                Assertions.assertDoesNotThrow(() -> setMapping());
                var schema = mapping.category();

                // Load data to instance
                Statistics.start(Interval.IMPORT_JOIN_MOVE);

                var process = new DatabaseToInstance();
                process.setLimit(batch * 1000);
                process.input(mapping, null, pullWrapper);

                var instance = process.run().data;
                //LOGGER.info(instance.toString());

                // JOIN
                Statistics.start(Interval.JOIN);
                join(schema, instance);
                Statistics.end(Interval.JOIN);
                
                //LOGGER.info(instance.toString());

                Statistics.start(Interval.MOVE);
                move(schema, instance);
                Statistics.end(Interval.MOVE);
                Statistics.end(Interval.IMPORT_JOIN_MOVE);

                var message = "#" + Statistics.getInfo(Counter.JOIN_ROWS)
                    + ", " + Statistics.getInfo(Counter.MOVE_ROWS)
                    + ", " + Statistics.getInfo(Interval.JOIN)
                    + ", " + Statistics.getInfo(Interval.MOVE)
                    + ", " + Statistics.getInfo(Interval.IMPORT_JOIN_MOVE);

                LOGGER.info(message);
                Statistics.logInfo(Counter.PULLED_RECORDS);

                /*
                Statistics.logInfo(Interval.JOIN);
                Statistics.logInfo(Interval.MOVE);
                Statistics.logInfo(Interval.IMPORT_JOIN_MOVE);
                Statistics.logInfo(Counter.JOIN_ROWS);
                */
            }
        }

        //LOGGER.info(instance.toString());

        /*
        var finalMapping = data.createFinalMapping(schema);
        var instanceToDatabase = new InstanceToDatabase();
        instanceToDatabase.input(finalMapping, instance, ddlWrapper, pushWrapper);
        var finalResult = instanceToDatabase.run();
        */
        //LOGGER.info(finalResult.data);
	}

    private Set<ActiveDomainRow> getRows(InstanceCategory instance, ActiveDomainRow source, Signature... signatures) {
        var signature = signatures[0];
        var restOfSignatures = Arrays.copyOfRange(signatures, 1, signatures.length);
        var instanceMorphism = instance.getMorphism(signature);
        var mappings = instanceMorphism.mappingsFromRow(source);

        if (restOfSignatures.length == 0)
            return new TreeSet<>(mappings.stream().map(mapping -> mapping.codomainRow()).toList());
            
        var output = new TreeSet<ActiveDomainRow>();
        mappings.forEach(mapping -> {
            var target = mapping.codomainRow();
            output.addAll(getRows(instance, target, restOfSignatures));
        });

        return output;
    }

    private ActiveDomainRow getRowEffective(ActiveDomainRow source, List<InstanceMorphism> morphismPath) {
        for (var morphism : morphismPath) {
            source = morphism.mappingsFromRow(source).stream().findFirst().get().codomainRow();
        }

        return source;
    }

    private void join(SchemaCategory schema, InstanceCategory instance) {
        var fullAddress = TestData.addSchemaObject(schema, data.fullAddressKey, "fullAddress", Id.Empty());
        var address = schema.getObject(data.addressKey);
        var addressToFullAddressMorphism = TestData.addMorphismWithDual(schema, data.addressToFullAddress, address, fullAddress, Min.ONE, Max.ONE, Min.ONE, Max.ONE);
        
        var fullAddressInstance = new InstanceObject(fullAddress);
        instance.objects().put(fullAddressInstance.key(), fullAddressInstance);

        var addressInstance = instance.getObject(data.addressKey);
        var addressToFullAddressMorphismInstance = createInstanceMorphismWithDual(instance, addressToFullAddressMorphism);

        var fullAddressInnerMap = new TreeMap<IdWithValues, ActiveDomainRow>();
        var fullAddressId = new Id(Signature.Empty());

        Statistics.set(Counter.JOIN_ROWS, addressInstance.rows().size());
        for (var addressRow : addressInstance.rows()) {
            // Get new value
            // TODO - tímto se vyhledává pouze v id daných řádků ... chtělo by to udělat funkci, která se automaticky podívá i do morfizmů
            // ideálně bychom měli tyto morfizmy uchovávat na řádcích
            var street = addressRow.getValue(data.addressToStreet);
            var city = addressRow.getValue(data.addressToCity);
            var fullAddressValue = joinFunction(street, city);

            // Create idWithValues and row
            var builder = new IdWithValues.Builder();
            builder.add(Signature.Empty(), fullAddressValue);
            var fullAddressRow = new ActiveDomainRow(builder.build());

            // Add it to the inner map which will be put to the instance object
            fullAddressInnerMap.put(fullAddressRow.idWithValues(), fullAddressRow);

            // Add morphisms
            addressToFullAddressMorphismInstance.addMapping(new ActiveMappingRow(addressRow, fullAddressRow));
            addressToFullAddressMorphismInstance.dual().addMapping(new ActiveMappingRow(fullAddressRow, addressRow));
        }
        // Add the inner map to the instance object
        fullAddressInstance.activeDomain().put(fullAddressId, fullAddressInnerMap);
    }

    private void move(SchemaCategory schema, InstanceCategory instance) {
        // Link order with full address
        var order = schema.getObject(data.orderKey);
        var fullAddress = schema.getObject(data.fullAddressKey);
        var orderToFullAddressMorphism = TestData.addMorphismWithDual(schema, data.orderToFullAddress, order, fullAddress, Min.ONE, Max.ONE, Min.ONE, Max.ONE);

        var orderToFullAddressMorphismInstance = createInstanceMorphismWithDual(instance, orderToFullAddressMorphism);

        Statistics.set(Counter.MOVE_ROWS, instance.getObject(data.orderKey).rows().size());

        var originalSignatures = List.of(data.userToOrder.dual(), data.userToAddress, data.addressToFullAddress);
        List<InstanceMorphism> morphismPath = originalSignatures.stream()
            .map(signature -> instance.getMorphism(signature)).toList();
        
        for (var orderRow : instance.getObject(data.orderKey).rows()) {
            var fullAddressRow = getRowEffective(orderRow, morphismPath);
            orderToFullAddressMorphismInstance.addMapping(new ActiveMappingRow(orderRow, fullAddressRow));
            orderToFullAddressMorphismInstance.dual().addMapping(new ActiveMappingRow(fullAddressRow, orderRow));
        }

        // Delete address to full address
        var addressToFullAddress = schema.getMorphism(data.addressToFullAddress);
        schema.deleteMorphism(addressToFullAddress);
        schema.deleteMorphism(addressToFullAddress.dual());

        var addressToFullAddressInstance = instance.getMorphism(addressToFullAddress);
        instance.deleteMorphism(addressToFullAddressInstance);
        instance.deleteMorphism(addressToFullAddressInstance.dual());
    }

    private static InstanceMorphism createInstanceMorphismWithDual(InstanceCategory instance, SchemaMorphism morphism) {
        var dom = instance.getObject(morphism.dom());
        var cod = instance.getObject(morphism.cod());

        var morphismInstance = new InstanceMorphism(morphism, dom, cod, instance);
        var dualInstance = new InstanceMorphism(morphism.dual(), cod, dom, instance);
        instance.morphisms().put(morphismInstance.signature(), morphismInstance);
        instance.morphisms().put(dualInstance.signature(), dualInstance);

        return morphismInstance;
    }

    private void createInstanceAsACopy(InstanceCategory instance, Key masterKey, Key sourceKey, Signature masterToSource, Key copyKey, Signature masterToCopy) {
        // Create instance object
        var copy = instance.schema.getObject(copyKey);
        var copyInstance = new InstanceObject(copy);
        instance.objects().put(copyKey, copyInstance);

        // Create instance morphisms
        var masterInstance = instance.getObject(masterKey);
        var masterToCopyMorphism = instance.schema.getMorphism(masterToCopy);
        var masterToCopyMorphismInstance = new InstanceMorphism(masterToCopyMorphism, masterInstance, copyInstance, instance);
        instance.morphisms().put(masterToCopyMorphismInstance.signature(), masterToCopyMorphismInstance);
        var masterToCopyDualInstance = new InstanceMorphism(masterToCopyMorphism.dual(), copyInstance, masterInstance, instance);
        instance.morphisms().put(masterToCopyDualInstance.signature(), masterToCopyDualInstance);
        
        // Populate instance object
        var sourceToCopyMappings = new TreeMap<ActiveDomainRow, ActiveDomainRow>();
        var sourceInstance = instance.getObject(sourceKey);
        sourceInstance.activeDomain().forEach((id, innerMap) -> {
            var copiedInnerMap = new TreeMap<IdWithValues, ActiveDomainRow>();

            innerMap.forEach((idWithValues, row) -> {
                var copiedIdWithValues = idWithValues.copy();
                var copiedRow = new ActiveDomainRow(copiedIdWithValues);
                copiedInnerMap.put(copiedIdWithValues, copiedRow);
                sourceToCopyMappings.put(row, copiedRow);
            });

            copyInstance.activeDomain().put(id, copiedInnerMap);
        });

        // Populate instance morphism
        var masterToSourceMorphismInstance = instance.getMorphism(masterToSource);
        var masterToSourceDualInstance = masterToSourceMorphismInstance.dual();
        masterToSourceMorphismInstance.allMappings().forEach(mappingRow -> {
            var masterRow = mappingRow.domainRow();
            var copyRow = sourceToCopyMappings.get(mappingRow.codomainRow());

            masterToSourceMorphismInstance.addMapping(new ActiveMappingRow(masterRow, copyRow));
            masterToSourceDualInstance.addMapping(new ActiveMappingRow(copyRow, masterRow));
        });
    }

    private static String joinFunction(String street, String city) {
        return street + " --- " + city;
    }

    private Mapping mapping;

    private void setMapping() throws URISyntaxException {
        mapping = data.createInitialMapping();
    }

}
