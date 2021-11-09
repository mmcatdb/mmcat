package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.Superid;
import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.category.Functor;
import cz.cuni.matfyz.core.category.*;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.Context;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.mapping.Name;
import cz.cuni.matfyz.core.mapping.Value;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
import java.util.*;

import org.javatuples.Pair;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class ModelToCategory
{
	public void algorithm(SchemaCategory schema, InstanceCategory instance, ForestOfRecords forest, Mapping mapping) {

		Stack<StackTriple> M = new Stack<>();

		forest.forEach(r -> {
			// preparation phase
			if (mapping.rootMorphism == null)
            {
				// K with root object
				CategoricalObject qI = Functor.object(schema, instance, mapping.rootObject);
				Set<Superid> sids = this.fetchSids(mapping.rootObject.superid(), r, null);

				for (Superid sid : sids) {
					sid = this.modify(qI, sid);
					for (var contextValue : children(mapping.accessPath)) {
						M.push(new StackTriple(sid, contextValue.context, contextValue.value));
					}
				}

			}
            else
            {
				// K with root morphism
				SchemaObject qI_dom = (SchemaObject) Functor.object(schema, instance, mapping.rootObject);
				Set<Superid> sidsDom = this.fetchSids(mapping.rootObject.superid(), r, null);
				Superid sid_dom = this.modify(qI_dom, sidsDom.iterator().next());

				SchemaObject qS_cod = (SchemaObject) mapping.rootMorphism.cod();
				Set<Superid> sids_cod = this.fetchSids(qS_cod.superid(), r, null);
				SchemaObject qI_cod = (SchemaObject) Functor.object(schema, instance, qS_cod);
				Superid sid_cod = this.modify(qI_cod, sids_cod.iterator().next());

				Morphism mI = Functor.morphism(schema, instance, mapping.rootMorphism);

				this.addRelation(mI, sid_dom, sid_cod, r);
				this.addRelation(mI.dual(), sid_cod, sid_dom, r);

				AccessPath t_dom = mapping.accessPath.getSubpathBySignature(null);
				AccessPath t_cod = mapping.accessPath.getSubpathBySignature(mapping.rootMorphism.signature());

				AccessPath ap = mapping.accessPath.minus(t_dom, t_cod);
                
				//for (var contextValue : children(ap))
				//	M.push(new StackTriple(sid_dom, contextValue.context, contextValue.value));
                
                if (ap instanceof ComplexProperty apComplex)
                    for (Pair<Signature, ComplexProperty> child : apComplex.children())
                        //M.push(new StackTriple(sid, contextValue.context, contextValue.value));
                        M.push(new StackTriple(sid_dom, child.getValue0(), child.getValue1()));

				//for (var contextValue : children(t_cod))
				//	M.push(new StackTriple(sid_cod, contextValue.context, contextValue.value));
                
                if (t_cod instanceof ComplexProperty t_codComplex)
                    for (Pair<Signature, ComplexProperty> child : t_codComplex.children())
                        M.push(new StackTriple(sid_cod, child.getValue0(), child.getValue1()));
			}

			// processing of the tree
			while (!M.empty()) {
				var triple = M.pop();
				Morphism mI = Functor.morphism(schema, instance, triple.mS);
				SchemaObject oS = (SchemaObject) triple.mS.cod();
				SchemaObject qI = (SchemaObject) Functor.object(schema, instance, oS);
				Set<Superid> sids = this.fetchSids(oS.superId(), r, triple.pid);

				for (Superid sid : sids) {
					sid = this.modify(qI, sid);

					this.addRelation(mI, triple.pid, sid, r);
					this.addRelation(mI.dual(), sid, triple.pid, r);

					//for (var contextValue : children(triple.t)) {
                    if (triple.t instanceof ComplexProperty complexProperty)
                        for (Pair<Signature, ComplexProperty> child : complexProperty.children())
                            //M.push(new StackTriple(sid, contextValue.context, contextValue.value));
                            M.push(new StackTriple(sid, child.getValue0(), child.getValue1()));
				}

			}
		});

	}

	private Set<Superid> fetchSids(Id superId, Object record, Object todo) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private Superid modify(CategoricalObject qI, Superid sid) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

    /*
	private Set<ContextValue> children(AccessPath accessPath) {
		Set<ContextValue> result = new TreeSet<>();
		for (AccessPathProperty property : accessPath.properties()) {
			result.addAll(process(property.name, null, null));
			result.addAll(process(null, property.context, property.value));
		}
		return result;
	}
    */

	private void addRelation(Morphism mI, Superid sid_dom, Superid sid_cod, Object r) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

    /*
	private Collection<? extends ContextValue> process(Name name, Context context, Value value) {
		List<ContextValue> result = new ArrayList<>();
		switch (name.type) {
			case STATIC_NAME -> {
				return result;
			}
			case SIGNATURE -> {
				result.add(new ContextValue(name.context, null));
				return result;
			}

		}

		switch (value.type) {
			case SIGNATURE, EPSILON -> {
				result.add(new ContextValue(new Context(context, value), null));
				return result;
			}

		}

		if (context.type == Context.Type.SIGNATURE) {
			result.add(new ContextValue(context, value));
			return result;
		}

		return children(value);

	}
    */
}
