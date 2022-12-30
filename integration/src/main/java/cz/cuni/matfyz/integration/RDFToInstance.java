package cz.cuni.matfyz.integration;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.Merger;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;
import cz.cuni.matfyz.integration.propertyprocessor.ArrayProcessor;
import cz.cuni.matfyz.integration.propertyprocessor.NonTypePropertyProcessor;
import cz.cuni.matfyz.integration.propertyprocessor.PropertyProcessor;
import cz.cuni.matfyz.integration.propertyprocessor.TextProcessor;

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

    static final String IDENTIFIER_IRI = Common.CUSTOM_IRI_PREFIX + "identifier";
    static final String CLASS_TO_IDENTIFIER_IRI = Common.CUSTOM_IRI_PREFIX + "class-to-identifier";
    static final String CLASS_TO_ISA_IRI = Common.CUSTOM_IRI_PREFIX + "class-to-isa";

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
        LOGGER.info("[Resource]: {}", resource.getURI());

        final String typeIri = getTypeIri(resource);
        final InstanceObject object = findObject(typeIri);
        final DomainRow row = getOrCreateInitialDomainRow(resource, object);

        // TODO add relation from row to the id object?
        // Ne, spíš vytvořit testovací framework a pak postupně sledovat jak se to objevuje v IC
        
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
        LOGGER.info("[Type]: {}", type);

        return type;
    }

    private static List<? extends PropertyProcessor> definePropertyProcessors(InstanceCategory category) {
        final List<PropertyProcessor> output = new ArrayList<>();

        output.add(new NonTypePropertyProcessor(category));
        output.add(new TextProcessor(category));
        output.add(new ArrayProcessor(category));

        return output;
    }

    private List<? extends PropertyProcessor> propertyProcessors;


    private void processProperty(Statement statement, InstanceObject resourceObject, DomainRow resourceRow) {
        LOGGER.info("{}", statement);

        for (final var processor : propertyProcessors)
            if (processor.tryProcessProperty(statement, resourceObject, resourceRow))
                return;

        throw new UnsupportedOperationException("No processor found for statement: " + statement + ".");
    }

    private InstanceObject findObject(String pimIri) {
        final var objects = this.category.objects().values().stream().filter(object -> object.schemaObject.pimIri.equals(pimIri)).toList();
        if (objects.size() != 1)
            throw new UnsupportedOperationException("No instance object found for pimIri: " + pimIri + ".");

        return objects.get(0);
    }

    private DomainRow getOrCreateInitialDomainRow(Resource resource, InstanceObject resourceObject) {
        if (!resourceObject.schemaObject.ids().isValue())
            throw new UnsupportedOperationException("Identifier has wrong id for resource:" + resource.getURI() + ".");

        final var resourceSuperId = new SuperIdWithValues.Builder()
            .add(Signature.createEmpty(), resource.getURI())
            .build();

        return resourceObject.getOrCreateRow(resourceSuperId);
    }
}
