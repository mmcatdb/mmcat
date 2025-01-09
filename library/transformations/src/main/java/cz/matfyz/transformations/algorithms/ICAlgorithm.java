package cz.matfyz.transformations.algorithms;

import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractICWrapper.AttributePair;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.IdentifierStructure;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.transformations.exception.InvalidStateException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This algorithm creates integrity constraint statements.
 * It creates multiple statements because some of them need to be executed before others.
 * So, all statements for all kinds in a given datasource should be created first and then sorted.
 */
public class ICAlgorithm {

    public static Collection<AbstractStatement> run(Mapping mapping, Iterable<Mapping> allMappings, AbstractICWrapper wrapper) {
        return new ICAlgorithm(mapping, allMappings, wrapper).run();
    }

    private final Mapping mapping;
    private final Map<SchemaObject, Mapping> mappingsByObjects;
    private final AbstractICWrapper wrapper;

    private ICAlgorithm(Mapping mapping, Iterable<Mapping> allMappings, AbstractICWrapper wrapper) {
        this.mapping = mapping;
        this.wrapper = wrapper;
        this.mappingsByObjects = new TreeMap<>();
        allMappings.forEach(m -> mappingsByObjects.put(m.rootObject(), m));
    }

    private final Map<String, Set<AttributePair>> referencesForAllKinds = new TreeMap<>();

    private Collection<AbstractStatement> run() {
        wrapper.clear();

        // Primary key constraint.
        final IdentifierStructure identifierStructure = collectNames(mapping.accessPath(), mapping.primaryKey());
        // If there are no signatures, we can't create the primary key.
        if (!identifierStructure.isEmpty())
            wrapper.appendIdentifier(mapping.kindName(), identifierStructure);

        // Reference keys constraints.
        processPath(mapping.accessPath(), mapping, Signature.createEmpty());
        referencesForAllKinds.forEach((referencedKindName, references) -> wrapper.appendReference(mapping.kindName(), referencedKindName, references));

        return wrapper.createICStatements();
    }

    /**
     * For each signature from primaryKey, we look to the access path to find a subpath with given signature.
     * The set of these names is then returned.
     * @param path Access path corresponding to the kind of the mapping.
     * @param primaryKey An eventually ordered collection of signatures of morphisms whose codomains correspond to properties forming the primary identifier of given kind.
     * @return The set of names corresponding to signatures from primaryKey.
     */
    private IdentifierStructure collectNames(ComplexProperty path, Collection<Signature> primaryKey) {
        final var output = new TreeSet<String>();

        for (Signature signature : primaryKey) {
            // The empty signature does mean the object is identified by its value, i.e., there is no scecial primary key
            if (signature.isEmpty())
                continue;

            final var subpath = path.getSubpathBySignature(signature);
            if (subpath == null)
                continue;

            if (subpath.name() instanceof StaticName staticName)
                output.add(staticName.getStringName());
            else
                // These names are identifiers of given kind so they must be unique among all names.
                // This quality can't be achieved by dynamic names so they aren't supported here.
                throw InvalidStateException.nameIsNotStatic(subpath.name());
        }

        return new IdentifierStructure(output);
    }

    private void processPath(AccessPath path, Mapping lastMapping, Signature signatureFromLastMapping) {
        //final var newSignatureFromLastMapping = signatureFromLastMapping.concatenate(path.signature());

        for (final var baseSignature : path.signature().toBases()) {
            final var object = mapping.category().getEdge(baseSignature).to();
            final var objectMapping = mappingsByObjects.get(object);

            if (objectMapping == null) {
                signatureFromLastMapping = signatureFromLastMapping.concatenate(baseSignature);
            }
            else {
                lastMapping = objectMapping;
                signatureFromLastMapping = Signature.createEmpty();
            }
        }

        if (path instanceof SimpleProperty simpleProperty)
            processSimpleSubpath(simpleProperty, lastMapping, signatureFromLastMapping);
        else if (path instanceof ComplexProperty complexProperty)
            processAllSubpaths(complexProperty, lastMapping, signatureFromLastMapping);
    }

    private void processSimpleSubpath(SimpleProperty property, Mapping lastMapping, Signature signatureFromLastMapping) {
        if (lastMapping.equals(mapping))
            return;

        if (!lastMapping.primaryKey().contains(signatureFromLastMapping))
            return;

        // The property is contained in the primary key of the mapping meaning we have to include it in the reference keys.
        final var subpathInLastMapping = lastMapping.accessPath().getSubpathBySignature(signatureFromLastMapping);
        if (!(subpathInLastMapping instanceof SimpleProperty referencedProperty))
            return;

        if (!(property.name() instanceof StaticName referencingName))
            return;

        if (!(referencedProperty.name() instanceof StaticName referencedName))
            return;

        final var referencesForKind = referencesForAllKinds.computeIfAbsent(lastMapping.kindName(), x -> new TreeSet<>());
        referencesForKind.add(new AttributePair(referencingName.toString(), referencedName.toString()));
    }

    private void processAllSubpaths(ComplexProperty complexProperty, Mapping lastMapping, Signature signatureFromLastMapping) {
        complexProperty.subpaths().forEach(subpath -> processPath(subpath, lastMapping, signatureFromLastMapping));
    }

}
