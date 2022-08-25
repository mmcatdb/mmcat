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
public class InstanceMorphism implements Comparable<InstanceMorphism>, Morphism
{
    private final SchemaMorphism schemaMorphism;
	private final InstanceObject dom;
	private final InstanceObject cod;
	private final InstanceCategory category;

    //private final Map<DomainRow, Set<MappingRow>> mappings = new TreeMap<>();
	SortedSet<MappingRow> mappings = new TreeSet<>();
	private boolean isActive = false;

	public boolean isActive() {
		return this.isActive;
	}
    
	public InstanceMorphism(SchemaMorphism schemaMorphism, InstanceObject dom, InstanceObject cod, InstanceCategory category)
    {
		this.schemaMorphism = schemaMorphism;
		this.dom = dom;
		this.cod = cod;
        this.category = category;
	}

	/**
	 * Returns base morphisms in the order they need to be traversed (i.e., the first one has the same domainObject as this).
	 * @return
	 */
	public List<InstanceMorphism> toBases() {
		return signature().toBasesReverse().stream().map(signature -> category.getMorphism(signature)).toList();
	}
    
    public void addMapping(MappingRow mapping)
    {
		/*
		Set<MappingRow> set = mappings.get(mapping.domainRow());
		if (set == null)
		{
			set = new TreeSet<>();
			mappings.put(mapping.domainRow(), set);
		}
		*/

        mappings.add(mapping);

		mapping.domainRow().addMappingFrom(this, mapping);
		//mapping.codomainRow().addMappingTo(this, mapping);

		this.isActive = true;
    }

	public void removeMapping(MappingRow mapping) {
		mappings.remove(mapping);
		mapping.domainRow().removeMappingFrom(this, mapping);
	}

	public SortedSet<MappingRow> allMappings()
	{
		//return new TreeSet<>(mappings.values().stream().flatMap(Set::stream).collect(Collectors.toSet()));
		return mappings;
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
	public int compareTo(InstanceMorphism instanceMorphism)
    {
		var domainComparison = dom.compareTo(instanceMorphism.dom);
		return domainComparison != 0 ? domainComparison : cod.compareTo(instanceMorphism.cod);
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
		for (MappingRow row : allMappings())
            	builder.append("\t\t").append(row).append("\n");
        
        return builder.toString();
	}
    
    @Override
    public boolean equals(Object object)
    {
        return object instanceof InstanceMorphism instanceMorphism && mappings.equals(instanceMorphism.mappings);
    }
}
