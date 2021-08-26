package de.hda.fbi.modules.schemaextraction.impl;

import de.hda.fbi.modules.schemaextraction.EntityReader;
import de.hda.fbi.modules.schemaextraction.TreeIntegrationManager;
import de.hda.fbi.modules.schemaextraction.common.DataType;
import de.hda.fbi.modules.schemaextraction.common.EntityInfo;
import de.hda.fbi.modules.schemaextraction.common.LastExtractedSchema;
import de.hda.fbi.modules.schemaextraction.common.LastExtractedSchemaProperty;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;
import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfiguration;
import de.hda.fbi.modules.schemaextraction.tree.TreeBuilder;
import de.hda.fbi.modules.schemaextraction.tree.TreeNodeManager;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TreeIntegrationManagerImpl implements TreeIntegrationManager {

//    private static Logger LOGGER = LoggerFactory.getLogger(TreeIntegrationManagerImpl.class);

    private EntityReader entityReader;

    private TreeBuilder treeBuilder;

    @Autowired
    public TreeIntegrationManagerImpl(EntityReader entityReader, TreeNodeManager treeNodeManager) {
        this.entityReader = entityReader;
        this.treeBuilder = new TreeBuilder(treeNodeManager);
    }

    public void integrate(SchemaExtractionConfiguration extractionConfiguration) {

        List<EntityInfo> documentInfos = this.getEntityInfos(extractionConfiguration);

        // Für alle Dokumente die Bäume aufspannen
        this.treeBuilder.build(documentInfos);
    }

    private List<EntityInfo> getEntityInfos(SchemaExtractionConfiguration extractionConfiguration) {

        List<String> entityTypes = extractionConfiguration.getEntityTypes();

        List<EntityInfo> entityInfos = new ArrayList<EntityInfo>();

        // Nur für die Entity-Typen die auch analysiert werden sollen
        if (extractionConfiguration.getUseCase().equals(SchemaExtractionUseCase.Incremental)) {
            // Hierbei nicht für jeden eingelesenen EntityType ein Baum
            // erstellen sondern für jedes vorhandene Schema

            int timestamp = extractionConfiguration.getLastExtractedSchemas().size() * -1;

            for (LastExtractedSchema lastExtractedSchema : extractionConfiguration.getLastExtractedSchemas()) {

                com.google.gson.JsonObject node = new com.google.gson.JsonObject();

                for (LastExtractedSchemaProperty property : lastExtractedSchema.getProperties()) {

                    if (property.getDataType().equals(DataType.STRING)) {
                        node.addProperty(property.getPropertyName(), "");
                    } else if (property.getDataType().equals(DataType.INTEGER)
                            || property.getDataType().equals(DataType.LONG)) {
                        node.addProperty(property.getPropertyName(), 0);
                    } else {
                        // throw new Exception("DataType not supported for
                        // schema extration.");
                        // TODO Exception
                    }
                }

                EntityInfo documentInfo = new EntityInfo(node, -1, lastExtractedSchema.getEntityTypeName(), null);

                entityInfos.add(documentInfo);
            }
        }

        entityInfos.addAll(this.entityReader.readEntitiesSorted(extractionConfiguration, entityTypes));
//        LOGGER.debug("Adding entity infos done! Added {} infos!", entityInfos.size());
        return entityInfos;
    }

    public void integrate(List<EntityInfo> entityinfos) {
        // Für alle Dokumente die Bäume aufspannen
        this.treeBuilder.build(entityinfos);
    }

}
