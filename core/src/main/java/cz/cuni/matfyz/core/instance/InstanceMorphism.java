package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaMorphism;

import java.util.*;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class InstanceMorphism implements Morphism
{
    private final SchemaMorphism schemaMorphism;
	private final InstanceObject dom;
	private final InstanceObject cod;
	private final InstanceCategory category;
    
    private final Set<ActiveMappingRow> mappings = new TreeSet<>();
    
    /*
	private final List<ActiveMappingRow> activeDomain = new ArrayList<>();

	public void addMapping(ActiveMappingRow mapping)
    {
		activeDomain.add(mapping);
	}
    */
    
	InstanceMorphism(SchemaMorphism schemaMorphism, InstanceObject dom, InstanceObject cod, InstanceCategory category)
    {
		this.schemaMorphism = schemaMorphism;
		this.dom = dom;
		this.cod = cod;
        this.category = category;
	}
    
    public void addMapping(ActiveMappingRow mapping)
    {
        mappings.add(mapping);
    }

    public Set<ActiveMappingRow> mappings()
    {
        return mappings();
    }
    
	@Override
	public InstanceObject dom()
    {
		return dom;
	}

	@Override
	public InstanceObject cod()
    {
		return cod;
	}

	@Override
	public InstanceMorphism dual()
    {
		return category.dual(signature());
	}

	@Override
	public Signature signature()
    {
		return schemaMorphism.signature();
	}
	
    @Override
	public String toString()
    {
		var builder = new StringBuilder();

		builder.append("\tSignature: ").append(signature())
                .append("\tDom: ").append(dom.key())
                .append("\tCod: ").append(cod.key()).append("\n");
        
        builder.append("\tValues:\n");
		for (ActiveMappingRow row : mappings)
            builder.append("\t\t").append(row).append("\n");
        
        return builder.toString();
	}
    
    @Override
    public boolean equals(Object object)
    {
        return object instanceof InstanceMorphism instanceMorphism ? equals(instanceMorphism) : false;
    }
    
    public boolean equals(InstanceMorphism morphism)
    {
        if (morphism == null)
            return false;
        
        return mappings.equals(morphism.mappings);
    }
}
