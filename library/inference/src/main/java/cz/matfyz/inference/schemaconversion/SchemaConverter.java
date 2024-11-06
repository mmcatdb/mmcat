package cz.matfyz.inference.schemaconversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingsPair;
import cz.matfyz.inference.schemaconversion.utils.UniqueNumberGenerator;

import java.util.List;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.rsd.RecordSchemaDescription;

/**
 * Class for converting a {@link RecordSchemaDescription} (RSD) to a Schema Category and its associated mappings.
 * This conversion process includes creating an access tree from the RSD, converting the access tree into a schema category,
 * and generating the corresponding mapping.
 */
public class SchemaConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaConverter.class);

    private final UniqueNumberGenerator keyGenerator;
    private final UniqueNumberGenerator signatureGenerator;

    public SchemaConverter() {
        this.keyGenerator = new UniqueNumberGenerator(0);
        this.signatureGenerator = new UniqueNumberGenerator(0);
    }

    /**
     * Converts the current {@link RecordSchemaDescription} to a schema category and mapping.
     * This involves creating an access tree from the RSD, converting the tree to a schema category, and generating the mapping for the schema category.
     */
    public CategoryMappingsPair convert(RecordSchemaDescription rsd, Datasource datasource, String kindName) {
        LOGGER.info("Converting RSD to SchemaCategory...");

        LOGGER.info("Creating the access tree from RSD...");
        final RSDToAccessTreeConverter rsdToAccessTreeConverter = new RSDToAccessTreeConverter(kindName, keyGenerator, signatureGenerator);
        final AccessTreeNode root = rsdToAccessTreeConverter.convert(rsd);
        System.out.println("Access tree with unprocessed arrays: ");
        root.printTree(" ");

        LOGGER.info("Creating the schema category from the access tree...");
        final AccessTreeToSchemaCategoryConverter accessTreeToSchemaCategoryConverter = new AccessTreeToSchemaCategoryConverter(kindName);
        final var schemaWithMetadata = accessTreeToSchemaCategoryConverter.convert(root);
        final var schema = schemaWithMetadata.schema();
        final var metadata = schemaWithMetadata.metadata();

        LOGGER.info("Creating the mapping for the schema category...");
        final MappingCreator mappingCreator = new MappingCreator(root.key, root);
        final Mapping mapping = mappingCreator.createMapping(datasource, schema, kindName);

        return new CategoryMappingsPair(schema, metadata, List.of(mapping));
    }

    /**
     * Enum representing the possible labels for schema objects.
     */
    public enum Label {
        IDENTIFIER,
        RELATIONAL,
    }
}
