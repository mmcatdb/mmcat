package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;

import java.util.*;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class Mapping
{
	private final SchemaObject rootObject;
	private final SchemaMorphism rootMorphism;
	private final ComplexProperty accessPath;

    private String kindName;
    private Collection<Signature> pkey;
    
    
    public Mapping(SchemaObject rootObject, ComplexProperty accessPath)
    {
        this(rootObject, null, accessPath);
    }
    
    public Mapping(SchemaObject rootObject, SchemaMorphism rootMorphism, ComplexProperty accessPath)
    {
        this.rootObject = rootObject;
        this.rootMorphism = rootMorphism;
        this.accessPath = accessPath;
    }
    
    public boolean hasRootMorphism()
    {
        return rootMorphism != null;
    }
    
    public SchemaObject rootObject()
    {
        return rootObject;
    }
    
    public SchemaMorphism rootMorphism()
    {
        return rootMorphism;
    }
    
    public ComplexProperty accessPath()
    {
        return accessPath;
    }

    public String kindName()
    {
        return kindName;
    }

    public Collection<Signature> pkey()
    {
        return pkey;
    }

    private final List<Reference> references = new ArrayList<Reference>();

    public List<Reference> references()
    {
        return references;
    }

    public void setReferences(Iterable<Reference> references)
    {
        this.references.clear();
        references.forEach(reference -> this.references.add(reference));
    }
}
