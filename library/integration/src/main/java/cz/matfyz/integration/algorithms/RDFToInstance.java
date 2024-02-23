package cz.matfyz.integration.algorithms;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.core.instance.SuperIdWithValues;
import cz.matfyz.core.utils.Config;
import cz.matfyz.integration.exception.ObjectException;
import cz.matfyz.integration.propertyprocessor.ArrayProcessor;
import cz.matfyz.integration.propertyprocessor.PropertyProcessor;
import cz.matfyz.integration.propertyprocessor.SimpleAttributeProcessor;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class RDFToInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDFToInstance.class);

    static final String RDF_TYPE_IRI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    static final String CLASS_TO_ISA_IRI = Config.GLOBAL.get("integration.iriPrefix") + "class-to-isa";

    private Dataset dataset;
    private InstanceCategory category;

    public void input(Dataset dataset, InstanceCategory category) {
        this.dataset = dataset;
        this.category = category;
        this.propertyProcessors = definePropertyProcessors(category);
    }

    public void algorithm() {
        processModel(dataset.getDefaultModel());
        dataset.listModelNames().forEachRemaining(resource -> {
            final Model model = dataset.getNamedModel(resource.getURI());
            processModel(model);
        });
    }

    private void processModel(Model model) {
        model.listStatements().forEach(statement -> {
            LOGGER.debug("[Statement]: {}", statement);
        });

        model.listSubjects().forEach(this::processResource);
    }

    private void processResource(Resource resource) {
        LOGGER.debug("[Resource]: {}", resource.getURI());

        final String typeIri = getTypeIri(resource);
        final InstanceObject object = findObject(typeIri);
        final DomainRow row = getOrCreateInitialDomainRow(resource, object);

        resource.listProperties().forEach(statement -> {
            if (statement.getPredicate().equals(resource.getModel().getProperty(RDF_TYPE_IRI)))
                return;

            processProperty(statement, object, row);
        });
    }

    private String getTypeIri(Resource resource) {
        final var rdfTypeProperty = resource.getModel().getProperty(RDF_TYPE_IRI);
        final var typeStatement = resource.getProperty(rdfTypeProperty);
        final var type = typeStatement.getObject().toString();
        LOGGER.debug("[Type]: {}", type);

        return type;
    }

    private static List<? extends PropertyProcessor> definePropertyProcessors(InstanceCategory category) {
        final List<PropertyProcessor> output = new ArrayList<>();

        output.add(new SimpleAttributeProcessor(category));
        output.add(new ArrayProcessor(category));
        // TODO map processor

        return output;
    }

    private List<? extends PropertyProcessor> propertyProcessors;

    private void processProperty(Statement statement, InstanceObject resourceObject, DomainRow resourceRow) {
        LOGGER.debug("{}", statement);

        for (final var processor : propertyProcessors)
            if (processor.tryProcessProperty(statement, resourceObject, resourceRow))
                return;

        LOGGER.error("No processor found for statement: {}.", statement);
    }

    private InstanceObject findObject(String pimIri) {
        final var objects = this.category.objects().values().stream().filter(object -> object.schemaObject.pimIri.equals(pimIri)).toList();
        if (objects.size() != 1)
            throw ObjectException.notFound(pimIri);

        return objects.get(0);
    }

    private DomainRow getOrCreateInitialDomainRow(Resource resource, InstanceObject resourceObject) {
        if (!resourceObject.schemaObject.ids().isValue())
            throw ObjectException.idsIsNotValue(resourceObject.schemaObject.pimIri, resource.asNode());

        final var resourceSuperId = SuperIdWithValues.fromEmptySignature(resource.getURI());

        return resourceObject.getOrCreateRow(resourceSuperId);
    }
}
