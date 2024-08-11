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
 * Class for conversion from RSD to Schema Category
 */
public class SchemaConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaConverter.class);

    private RecordSchemaDescription rsd;
    public String kindName; // TODO: I need this to name the root of my SK and my mapping (probably the same as kind name). Getting it as user inpu rn, but in case of MongoDB, it has to match a collection name! (otherwise cant pull from it)
    private final UniqueNumberGenerator keyGenerator;
    private final UniqueNumberGenerator signatureGenerator;

    public SchemaConverter() {
        this.keyGenerator = new UniqueNumberGenerator(0);
        this.signatureGenerator = new UniqueNumberGenerator(0);
    }

    public void setNewRSD(RecordSchemaDescription rsd, String kindName) {
        this.rsd = rsd;
        this.kindName = kindName;
    }

    public CategoryMappingPair convertToSchemaCategoryAndMapping() {
        System.out.println(rsd);
        LOGGER.info("Converting RSD to SchemaCategory...");

        LOGGER.info("Creating the access tree from RSD...");
        final RSDToAccessTreeConverter rsdToAccessTreeConverter = new RSDToAccessTreeConverter(kindName, keyGenerator, signatureGenerator);
        final AccessTreeNode root = rsdToAccessTreeConverter.convert(rsd);
        // System.out.println("Access tree with unprocessed arrays: ");
        // root.printTree(" ");

        LOGGER.info("Creating the schema category from the access tree...");
        final AccessTreeToSchemaCategoryConverter accessTreeToSchemaCategoryConverter = new AccessTreeToSchemaCategoryConverter(kindName);
        final var schemaWithMetadata = accessTreeToSchemaCategoryConverter.convert(root);
        final var schema = schemaWithMetadata.schema();
        final var metadata = schemaWithMetadata.metadata();


        // System.out.println("Morphisms in the final SK: ");
        // for (SchemaMorphism m : schemaCategory.allMorphisms()) {
        //     System.out.println(m.dom() == null ? "Domain is null" : "Domain: " + m.dom().label());
        //     System.out.println(m.cod() == null ? "Codomain is null" : "Codomain: " + m.cod().label());
        //     System.out.println();
        // }

        LOGGER.info("Creating the mapping for the schema category...");
        final MappingCreator mappingCreator = new MappingCreator(root.getKey(), root);
        final Mapping mapping = mappingCreator.createMapping(schema, this.kindName); //What will this label be?

        return new CategoryMappingPair(schema, metadata, List.of(mapping));
    }

    public enum Label {
        IDENTIFIER, RELATIONAL;
    }
}
