package cz.matfyz.inference.schemaconversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.inference.schemaconversion.utils.UniqueNumberGenerator;

import java.util.List;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.rsd.RecordSchemaDescription;

/**
 * Class for converting a {@link RecordSchemaDescription} (RSD) to a Schema Category and its associated mappings.
 * This conversion process includes creating an access tree from the RSD, converting the access tree into a schema category,
 * and generating the corresponding mapping.
 */
public class SchemaConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaConverter.class);

    private RecordSchemaDescription rsd;

    /** The kind name used to label the root of the Schema Category and the mapping. */
    public String kindName; // TODO: This needs to match the collection name in the case of MongoDB for pulling data.

    private final UniqueNumberGenerator keyGenerator;
    private final UniqueNumberGenerator signatureGenerator;

    /**
     * Constructs a new {@code SchemaConverter} with initialized key and signature generators.
     */
    public SchemaConverter() {
        this.keyGenerator = new UniqueNumberGenerator(0);
        this.signatureGenerator = new UniqueNumberGenerator(0);
    }

    /**
     * Sets a new {@link RecordSchemaDescription} (RSD) and kind name for the converter.
     *
     * @param rsd The new {@link RecordSchemaDescription} to set.
     * @param kindName The kind name to use for the root of the Schema Category and mapping.
     */
    public void setNewRSD(RecordSchemaDescription rsd, String kindName) {
        this.rsd = rsd;
        this.kindName = kindName;
    }

    /**
     * Converts the current {@link RecordSchemaDescription} to a schema category and mapping.
     * This involves creating an access tree from the RSD, converting the tree to a schema category,
     * and generating the mapping for the schema category.
     *
     * @return A {@link CategoryMappingPair} containing the schema and its associated metadata and mappings.
     */
    public CategoryMappingPair convertToSchemaCategoryAndMapping() {
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
        final MappingCreator mappingCreator = new MappingCreator(root.getKey(), root);
        final Mapping mapping = mappingCreator.createMapping(schema, this.kindName);

        return new CategoryMappingPair(schema, metadata, List.of(mapping));
    }

    /**
     * Enum representing the possible labels for schema objects.
     */
    public enum Label {
        IDENTIFIER, RELATIONAL;
    }
}
