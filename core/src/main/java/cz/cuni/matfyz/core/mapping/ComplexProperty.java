package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.ToJSONSwitchConverterBase;
import cz.cuni.matfyz.core.utils.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public Signature signature()
    {
        return signature;
    }
    
    @Override
    public IContext context()
    {
        return signature;
    }
    
    public boolean isAuxiliary()
    {
        return context().equals(Signature.Null());
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
        this(new StaticName(name), signature, Arrays.asList(subpaths));
    }
    
    public ComplexProperty(Signature name, Signature signature, AccessPath... subpaths)
    {
        this(new DynamicName(name), signature, Arrays.asList(subpaths));
    }
    
    public static ComplexProperty Empty()
    {
        return new ComplexProperty(null, Signature.Null(), Collections.<AccessPath>emptyList());
    }
    
    /**
     * Given a signature M, this function finds such a direct subpath S of this path that for each of the leaves L of S holds:
     *      - L.context == M, or
     *      - L.value == M, or
     *      - exists an ancestor A of L in S where A.context == M.
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
        
        // If M = null, a leaf L with L.value = epsion is returned.
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
        var subpathBuilder = new IntendedStringBuilder(1);
        
        if (subpaths.size() > 0)
            subpathBuilder.append(subpaths.get(0));
        for (int i = 1; i < subpaths.size(); i++)
            subpathBuilder.append(",\n").append(subpaths.get(i));
        subpathBuilder.append("\n");
        
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": ");
        if (!isAuxiliary())
            builder.append(context()).append(" ");
        
        builder.append("{\n").append(subpathBuilder).append("}");
        
        return builder.toString();
    }
    
    /**
     * Properties from given synthetic nodes are moved to their parent paths
     * @return 
     */
    public ComplexProperty copyWithoutAuxiliaryNodes()
    {
        List<AccessPath> newSubpaths = this.getContentWithoutAuxiliaryNodes();
        return new ComplexProperty(name, signature, newSubpaths);
    }
    
    private List<AccessPath> getContentWithoutAuxiliaryNodes()
    {
        List<AccessPath> newSubpaths = new ArrayList<>();
        for (AccessPath path : subpaths)
        {
            if (path instanceof SimpleProperty) // Not making a copy because the path is expected to be immutable.
            {
                newSubpaths.add(path);
            }
            else if (path instanceof ComplexProperty complexProperty)
            {
                if (complexProperty.isAuxiliary())
                    newSubpaths.addAll(complexProperty.getContentWithoutAuxiliaryNodes());
                else
                    newSubpaths.add(complexProperty.copyWithoutAuxiliaryNodes());
            }
        }
        
        return newSubpaths;
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONSwitchConverterBase<ComplexProperty> {

        @Override
        protected JSONObject _toJSON(ComplexProperty object) throws JSONException {
            var output = new JSONObject();
    
            output.put("name", object.name.toJSON());
            output.put("signature", object.signature.toJSON());

            var subpaths = new JSONArray(object.subpaths.stream().map(subpath -> subpath.toJSON()).toList());
            output.put("subpaths", subpaths);
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<ComplexProperty> {
    
        @Override
        protected ComplexProperty _fromJSON(JSONObject jsonObject) throws JSONException {
            var name = new Name.Builder().fromJSON(jsonObject.getJSONObject("name"));
            var signature = new Signature.Builder().fromJSON(jsonObject.getJSONObject("signature"));

            var subpathsArray = jsonObject.getJSONArray("subpaths");
            var subpaths = new ArrayList<AccessPath>();
            var builder = new AccessPath.Builder();
            for (int i = 0; i < subpathsArray.length(); i++)
                subpaths.add(builder.fromJSON(subpathsArray.getJSONObject(i)));

            return new ComplexProperty(name, signature, subpaths);
        }
    
    }
}
