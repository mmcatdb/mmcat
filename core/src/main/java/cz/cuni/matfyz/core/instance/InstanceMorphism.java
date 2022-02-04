package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaMorphism;

import java.util.*;
import java.util.stream.Collectors;

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

    private final Map<ActiveDomainRow, Set<ActiveMappingRow>> mappings = new TreeMap<>();
    
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
		Set<ActiveMappingRow> set = mappings.get(mapping.domainRow());
		if (set == null)
		{
			set = new TreeSet<>();
			mappings.put(mapping.domainRow(), set);
		}

        set.add(mapping);
    }

    public Set<ActiveMappingRow> mappingsFromRow(ActiveDomainRow row)
    {
        var mappingsFromRow = mappings.get(row);
		return mappingsFromRow == null ? new TreeSet<>() : mappingsFromRow;
    }

	public Set<ActiveMappingRow> allMappings()
	{
		return mappings.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
	}

	public SchemaMorphism schemaMorphism()
	{
		return schemaMorphism;
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
		//for (Set<ActiveMappingRow> set : mappings.values())
		//	for (ActiveMappingRow row : set)
		for (ActiveMappingRow row : allMappings())
            	builder.append("\t\t").append(row).append("\n");
        
        return builder.toString();
	}
    
    @Override
    public boolean equals(Object object)
    {
        return object instanceof InstanceMorphism instanceMorphism && mappings.equals(instanceMorphism.mappings);
    }
}
