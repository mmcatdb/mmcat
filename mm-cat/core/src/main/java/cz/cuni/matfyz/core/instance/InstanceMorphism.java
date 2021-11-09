package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class InstanceMorphism implements Morphism
{
    private final SchemaMorphism schemaMorphism;
	private final InstanceObject dom;
	private final InstanceObject cod;
	private final InstanceCategory category;
    
	private final List<ActiveMappingRow> activeDomain = new ArrayList<>();

	public void addMapping(ActiveMappingRow mapping)
    {
		activeDomain.add(mapping);
	}
    
	InstanceMorphism(SchemaMorphism schemaMorphism, InstanceObject dom, InstanceObject cod, InstanceCategory category) {
		this.schemaMorphism = schemaMorphism;
		this.dom = dom;
		this.cod = cod;
        this.category = category;
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
	public Morphism dual()
    {
		return category.dual(signature());
	}

	@Override
	public Signature signature()
    {
		return schemaMorphism.signature();
	}

}
