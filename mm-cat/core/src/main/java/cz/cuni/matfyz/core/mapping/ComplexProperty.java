package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

import java.util.*;
import org.javatuples.Pair;

/**
 * A complex value in the access path tree. Its context is a signature of a morphism (or undefined in case of an auxiliary property)
 * It has subpaths and it provides many methods needed in the algorithms described in the paper.
 * @author jachymb.bartik
 */
public class ComplexProperty extends AccessPath implements IValue
{
    private final Context context;
    
    @Override
    public Context getContext()
    {
        return context;
    }
    
    @Override
    public ComplexProperty getValue()
    {
        return this;
    }
    
    private final List<AccessPath> subpaths;
    
    public Iterable<AccessPath> getSubpaths()
    {
        return subpaths;
    }
    
    public ComplexProperty(Name name, Context context, List<AccessPath> subpaths)
    {
        super(name);
        
        this.context = context;
        this.subpaths = new ArrayList<>(subpaths);
    }
    
    public ComplexProperty(Name name, Context context, AccessPath... subpaths)
    {
        this(name, context, Arrays.asList(subpaths));
    }
    
    /**
     * Given a signature M, this function finds such a direct subpath S of this path that for each of the leaves L of S holds:
     *      * L.context == M, or
     *      * L.value == M, or
     *      * exists an ancestor A of L in S where A.context == M.
     * If M == null, a leaf L with L.value == epsion is returned.
     * If none of above exists, a null is returned;
     * @param signature
     * @return the closest subpath with given signature (or null if none such exists).
     */
	public AccessPath getSubpathBySignature(Signature signature)
    {
        for (AccessPath subpath : subpaths)
            if (subpath.hasSignature(signature))
                return subpath;
        
        return null;
	}
    
    @Override
    protected boolean hasSignature(Signature signature)
    {
        if (signature == null || context == null)
            return false;
        
        return context.getSignature().equals(signature);
    }
    
    /**
     * Creates a copy of this access path and links it to all its subpaths except the one given subpath.
     * @param subpath
     * @return a copy of this without subpath.
     */
	public AccessPath minusSubtree(AccessPath subpath)
    {
        assert subpaths.stream().anyMatch(path -> path.equals(subpath)) : "Subpath not found in accessPath in minusSubtree";
        
        final List<AccessPath> newSubpaths = subpaths.stream().filter(path -> path.equals(subpath)).toList();
        
        return new ComplexProperty(name, context, newSubpaths);
	}
    
    /**
     * Determine possible sub-paths to be traversed from this complex property (inner node of an access path).
     * According to the paper, this function should return pairs of (context, value). But value of such sub-path can only be an {@link ComplexProperty}.
     * Similarly, context must be a signature of a morphism.
     * @return set of pairs (morphism signature, complex property) of all possible sub-paths.
     */
    public Collection<Pair<Signature, ComplexProperty>> children()
    {
        final List<Pair<Signature, ComplexProperty>> output = new ArrayList<>();
        
        for (AccessPath subpath : subpaths)
        {
            output.addAll(process(subpath.getName(), null, null));
            output.addAll(process(null, subpath.getContext(), subpath.getValue()));
        }
        
        return output;
    }
    
    /**
     * Process (name, context and value) according to the "process" function from the paper.
     * Part of the logic is divided to the IValue.process() functions because it's dependent on the value type (simple/complex).
     * @param name
     * @param context
     * @param value
     * @return see {@link #children()}
     */
    private static Collection<Pair<Signature, ComplexProperty>> process(Name name, Context context, IValue value)
    {
        if (name == null) // Empty name
        {
            return value.process(context);
        }
        else
        {
            if (name.getType() == Name.Type.DYNAMIC_NAME)
                return List.of(new Pair(name.getSignature(), null));
            else
                return Collections.EMPTY_LIST;
        }
    }
    
    @Override
    public Collection<Pair<Signature, ComplexProperty>> process(Context context)
    {
        if (context.getSignature() != null)
            return List.of(new Pair(context, this));
        else
            return children();
    }
}
