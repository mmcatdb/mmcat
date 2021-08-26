package de.hda.fbi.modules.schemaextraction.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.BooleanSchema;
import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import de.hda.fbi.modules.schemaextraction.AnalyseManager;
import de.hda.fbi.modules.schemaextraction.common.AnalyseResult;
import de.hda.fbi.modules.schemaextraction.common.DataType;
import de.hda.fbi.modules.schemaextraction.common.PropertyType;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionException;
import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfiguration;
import de.hda.fbi.modules.schemaextraction.tree.Tree;
import de.hda.fbi.modules.schemaextraction.tree.TreeHolder;
import de.hda.fbi.modules.schemaextraction.tree.TreeNode;
import de.hda.fbi.modules.schemaextraction.tree.TreeNodeInfoService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyseManagerImpl implements AnalyseManager {

//    private static Logger LOGGER = LoggerFactory.getLogger(AnalyseManagerImpl.class);
	private TreeHolder treeHolder;

	private TreeNodeInfoService treeNodeInfoService;

	public AnalyseManagerImpl() {
	}

	@Autowired
	public AnalyseManagerImpl(TreeNodeInfoService treeNodeInfoService) {
		this.treeHolder = TreeHolder.getInstance();
		this.treeNodeInfoService = treeNodeInfoService;
	}

	public AnalyseResult analyse(SchemaExtractionConfiguration extractionConfiguration) throws SchemaExtractionException {
		AnalyseResult result = null;
//		for (int iterations = 0; iterations < 50; ++iterations) {
		//        LOGGER.info("Starting analyse!");

		List<String> entityTypes = extractionConfiguration.getEntityTypes();

		result = new AnalyseResult();

		List<String> schemas = new ArrayList<String>();

		// Generiere ein Gesamt-Schema pro Entitytyp
		for (String entityType : entityTypes) {

//            LOGGER.info("Analyse entity type {}", entityType);
			Tree tree = this.treeHolder.getTree(entityType);

			if (tree == null) {
				continue;
			}

			TreeNode root = tree.getRoot();

			ObjectSchema schema = new ObjectSchema();

			schema.setTitle(root.getName());

			this.assignProperties(tree, root, schema);
			ObjectMapper mapper = new ObjectMapper();
			String schemaAsString = "";
			try {
				schemaAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
			} catch (JsonProcessingException e) {
				throw new SchemaExtractionException(e);
			}

			schemas.add(schemaAsString);
		}

		result.setSchemas(schemas);

		Map<String, Integer> numberOfDifferentSchemasForEntityType = new HashMap<String, Integer>();

		Vector<Integer> allTimestampsSorted = this.treeHolder.getAllUniqueTimestampsSorted();

		for (int i = 0; i < allTimestampsSorted.size(); i++) {
//            LOGGER.debug("Analyse timestamp {}", i);
			int currentTimestamp = allTimestampsSorted.get(i);
			Integer lastTimestamp = 0;

			Tree tree = this.treeHolder.getTreeForTimestamp(currentTimestamp);

			lastTimestamp = tree.getTimestampBefore(currentTimestamp);

			Vector<TreeNode> deletedNodes = new Vector<TreeNode>();
			Vector<TreeNode> addedNodes = new Vector<TreeNode>();

			deletedNodes = this.treeNodeInfoService.getDeletedNodes(tree.getRoot(), lastTimestamp, currentTimestamp);

			addedNodes = this.treeNodeInfoService.getAddedNodes(tree.getRoot(), lastTimestamp, currentTimestamp);

			if (deletedNodes.size() > 0 || addedNodes.size() > 0) {
				// das Schema hat sich geändert
//                LOGGER.info("Schema {} changed!", tree.getName());
				if (!numberOfDifferentSchemasForEntityType.containsKey(tree.getName())) {
					numberOfDifferentSchemasForEntityType.put(tree.getName(), 1);
				} else {
//                    LOGGER.info("Schema {} not changed!", tree.getName());
					Integer counter = numberOfDifferentSchemasForEntityType.get(tree.getName());
					counter = counter + 1;
					numberOfDifferentSchemasForEntityType.remove(tree.getName());
					numberOfDifferentSchemasForEntityType.put(tree.getName(), counter);
				}
			}
		}

//        LOGGER.info("numberOfDifferentSchemasForEntityType {}", numberOfDifferentSchemasForEntityType);
		result.setNumberOfDifferentSchemasForEntityType(numberOfDifferentSchemasForEntityType);

//		}
		return result;

	}

	private DataType convert(PropertyType propertyType) {
		switch (propertyType) {
			case Boolean:
				return DataType.BOOLEAN;
			case Number:
				return DataType.INTEGER;
			case String:
				return DataType.STRING;
			default:
				return DataType.STRING;
		}
	}

	private void assignProperties(Tree tree, TreeNode parent, ObjectSchema schema) {

		Vector<JsonSchema> properties = new Vector<JsonSchema>();

		if (parent.getChildren(false).size() > 0) {
			for (TreeNode node : parent.getChildren(false)) {

				JsonSchema property = null;

				if (node.getPropertyType() == PropertyType.String) {
					property = new StringSchema();

				}
				if (node.getPropertyType() == PropertyType.Number) {
					property = new IntegerSchema();

				}
				if (node.getPropertyType() == PropertyType.Boolean) {
					property = new BooleanSchema();
				}

				double occurence = (double) node.getNumberOfDocuments() / (double) tree.getNumberOfDocuments() * 100;

				// property = builder.build();
				String occurenceText = "Occurrence: " + (int) occurence + "%";

				/*
                 * if (occurence < 50) { occurenceText += " ???? OPTIONAL ???";
                 * }
				 */
				property.setDescription(occurenceText);
				schema.putOptionalProperty(node.getName(), property);

			}
		}
	}
}
