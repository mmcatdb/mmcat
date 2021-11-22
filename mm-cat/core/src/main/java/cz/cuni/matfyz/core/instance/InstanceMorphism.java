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
    
    private final Map<ActiveDomainRow, Map<ActiveDomainRow, ActiveMappingRow>> mappingsByDomain = new TreeMap<>();
    
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
        Map<ActiveDomainRow, ActiveMappingRow> mappingsByCodomain = mappingsByDomain.get(mapping.domainRow());
        
        if (mappingsByCodomain == null)
        {
            mappingsByCodomain = new TreeMap<>();
            mappingsByDomain.put(mapping.domainRow(), mappingsByCodomain);
        }
        
        if (mappingsByCodomain.get(mapping.codomainRow()) == null)
            mappingsByCodomain.put(mapping.codomainRow(), mapping);
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

}
