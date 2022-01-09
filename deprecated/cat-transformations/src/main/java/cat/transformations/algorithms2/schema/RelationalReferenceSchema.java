package cat.transformations.algorithms2.schema;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.contos
 */
public class RelationalReferenceSchema implements AbstractReferenceSchema {

	private AbstractKindSchema referenced;

	private final List<AbstractPropertySchema> compounds = new ArrayList<>();

	public RelationalReferenceSchema() {
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		return builder.toString();
	}
}
