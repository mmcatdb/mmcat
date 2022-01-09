package cat.transformations.algorithms2.schema;

import cat.transformations.algorithms2.model.Cardinality;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavel.contos
 */
public class RelationalSchema implements AbstractSchema {

	private static final Logger LOGGER = Logger.getLogger(RelationalSchema.class.getName());

	private final Map<String, AbstractKindSchema> kinds = new TreeMap<>();

	@Override
	public void createKind(String name) {
		LOGGER.log(Level.INFO, String.format("creating new KIND %s", name));
		kinds.put(name, new RelationalKindSchema(name));
	}

	@Override
	public void createAttribute(String domain, String codomain, String dataType, Cardinality cardinality) {
		LOGGER.log(Level.INFO, String.format("creating new ATTRIBUTE %s:%s in TABLE %s with cardinality %s", codomain, dataType, domain, cardinality));
		switch (cardinality) {
			case ONE_TO_ONE -> {
				LOGGER.log(Level.INFO, String.format("\tcreated single ATTRIBUTE %s", codomain));
				AbstractKindSchema kind = kinds.get(domain);
				kind.createProperty(codomain, dataType, cardinality);
			}
			case ONE_TO_MANY -> {
				LOGGER.log(Level.INFO, String.format("\tcreated ATTRIBUTES (%s,%s) in TABLE %s", domain, codomain, codomain));
				AbstractKindSchema kind = new RelationalKindSchema(codomain);
				System.out.println("TODO: pridej jeste identifikator tehle tabulce... a to identifikator domain tabulky, ale ne jako PK codomain tabulky");
				kind.createProperty("DOMAIN_FK", "TODO-DATATYPE", cardinality);
				kind.createProperty(codomain, dataType, cardinality);
				System.out.println("pridej jeste FK teto tabulce, a to jako FK na domain");
				System.out.println("codomain tabulka muze obsahovat duplicitni hodnoty...");
				kinds.put(codomain, kind);
			}
			case MANY_TO_ONE -> {
				System.out.println("NEDAVA SMYSL... DODELEJ LOGOVANI");
			}
		}

	}

	@Override
	public void createStructuredAttribute(String domain, String codomain) {
		AbstractKindSchema codomainKind = new RelationalKindSchema(codomain);
		AbstractKindSchema domainKind = kinds.get(domain);
		domainKind.createProperty(codomain, "TODO-CODOMAIN-TYPE-IDENTIFIER", Cardinality.ONE_TO_MANY);
		codomainKind.createProperty(domain, "TODO-DOMAIN-TYPE-IDENTIFIER", Cardinality.ONE_TO_MANY);
		kinds.put(codomain, codomainKind);

// TODO: domain nebo codomain bude obsahovat FK?
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void createInlinedStructuredAttribute(String domain, String codomain) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void createAttribute(String parent, String current, String name, String datatype, Cardinality cardinality) {
		// tohle je ten vztahovy pripad!
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void createStructuredAttribute(String parent, String current, String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void createInlinedStructuredAttribute(String parent, String current, String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<String> getKindNames() {
		return kinds.keySet();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String kindName : kinds.keySet()) {
			builder.append(kindName);
			builder.append(" -> ");
			builder.append(kinds.get(kindName));
			builder.append("\n");
		}

		return builder.toString();
	}

	@Override
	public void createArray(String name) {
		LOGGER.log(Level.INFO, String.format("creating new ARRAY %s", name));
		kinds.put(name, new RelationalKindSchema(name));
	}
}
