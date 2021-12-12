package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

import java.util.*;

/**
 * A complex value in the access path tree. Its context is a signature of a morphism (or undefined in case of an auxiliary property)
 * It has subpaths and it provides many methods needed in the algorithms described in the paper.
 * @author jachymb.bartik
 */
public class ComplexProperty extends AccessPath implements IValue
{
    private final Signature signature;
    
    @Override
    public IContext context()
    {
        return signature;
    }
    
    public boolean isAuxiliary()
    {
        return context() == null;
    }
    
    public Signature signature()
    {
        return signature;
    }
    
    @Override
    public ComplexProperty value()
    {
        return this;
    }
    
    private final List<AccessPath> subpaths;
    
    public Iterable<AccessPath> subpaths()
    {
        return subpaths;
    }
    
    public ComplexProperty(Name name, Signature signature, List<AccessPath> subpaths)
    {
        super(name);
        
        this.signature = signature;
        this.subpaths = new ArrayList<>(subpaths);
    }
    
    public ComplexProperty(Name name, Signature signature, AccessPath... subpaths)
    {
        this(name, signature, Arrays.asList(subpaths));
    }
    
    public ComplexProperty(String name, Signature signature, AccessPath... subpaths)
    {
        this(new Name(name), signature, Arrays.asList(subpaths));
    }
    
    public ComplexProperty(Signature name, Signature signature, AccessPath... subpaths)
    {
        this(new Name(name), signature, Arrays.asList(subpaths));
    }
    
    public static ComplexProperty Empty()
    {
        return new ComplexProperty(null, null, Collections.EMPTY_LIST);
    }
    
    /**
     * Given a signature M, this function finds such a direct subpath S of this path that for each of the leaves L of S holds:
     *      * L.context == M, or
     *      * L.value == M, or
     *      * exists an ancestor A of L in S where A.context == M.
     * If there are more such subpaths (i.e. when some of them are auxiliary), the closest one is returned.
     * If M == null, a leaf L with L.value == epsion is returned.
     * If none of above exists, a null is returned;
     * @param signature
     * @return the closest subpath with given signature (or null if none such exists).
     */
	public AccessPath getSubpathBySignature(Signature signature)
    {
        if (context().equals(signature))
            return this;
        
        // If M == null, a leaf L with L.value == epsion is returned.
        if (signature == null)
        {
            for (AccessPath subpath : subpaths)
                if (subpath instanceof SimpleProperty simpleProperty && simpleProperty.value() == SimpleValue.Empty())
                    return simpleProperty;
            
            for (AccessPath subpath : subpaths)
                if (subpath instanceof ComplexProperty complexProperty)
                {
                    AccessPath result = complexProperty.getSubpathBySignature(null);
                    if (result != null)
                        return result;
                }
        }
        
        // If this is an auxiliary property, we must find if all of the descendats of this property have M in their contexts or values.
        // If so, this is returned even if this context is null.
        if (isAuxiliary())
        {
            boolean returnThis = true;
            for (AccessPath subpath : subpaths)
            {
                if (!subpath.hasSignature(signature) && subpath instanceof ComplexProperty complexProperty)
                    if (complexProperty.getSubpathBySignature(signature) != complexProperty)
                    {
                        returnThis = false;
                        break;
                    }
            }
            
            if (returnThis)
                return this;
        }
        
        for (AccessPath subpath : subpaths)
            if (subpath.hasSignature(signature))
                return subpath;
        
        for (AccessPath subpath : subpaths)
            if (subpath instanceof ComplexProperty complexProperty)
            {
                AccessPath result = complexProperty.getSubpathBySignature(signature);
                if (result != null)
                    return result;
            }
        
        return null;
	}
    
    @Override
    protected boolean hasSignature(Signature signature)
    {
        if (signature == null)
            return false;
        
        return signature.equals(this.signature);
    }
    
    /**
     * Creates a copy of this access path and links it to all its subpaths except the one given subpath.
     * @param subpath
     * @return a copy of this without subpath.
     */
	public ComplexProperty minusSubpath(AccessPath subpath)
    {
        assert subpaths.stream().anyMatch(path -> path.equals(subpath)) : "Subpath not found in accessPath in minusSubtree";
        
        final List<AccessPath> newSubpaths = subpaths.stream().filter(path -> path.equals(subpath)).toList();
        
        return new ComplexProperty(name, signature, newSubpaths);
	}
    
    @Override
    public String toString()
    {
        StringBuilder subpathBuilder = new StringBuilder();
        
        if (subpaths.size() > 0)
            subpathBuilder.append(subpaths.get(0));
            
        for (int i = 1; i < subpaths.size(); i++)
            subpathBuilder.append(",\n").append(subpaths.get(i));
        subpathBuilder.append("\n");
        String subpathResult = subpathBuilder.toString();
        
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": ");
        if (!isAuxiliary())
            builder.append(context()).append(" ");
        builder.append("{\n");
        
        for (String line : subpathResult.lines().toList())
            builder.append("\t").append(line).append("\n");
        
        builder.append("}");
        
        return builder.toString();
    }
}
