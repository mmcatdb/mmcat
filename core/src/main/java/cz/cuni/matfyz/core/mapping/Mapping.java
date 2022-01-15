package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class Mapping
{
	private final SchemaObject rootObject;
	private final SchemaMorphism rootMorphism;
	private final ComplexProperty accessPath;
    
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
}
