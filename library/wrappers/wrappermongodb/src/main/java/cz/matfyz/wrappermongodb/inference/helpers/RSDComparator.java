package cz.matfyz.wrappermongodb.inference.helpers;

import java.io.Serializable;
import java.util.Comparator;

import cz.matfyz.core.rsd.RecordSchemaDescription;

/**
 *
 * @author sebastian.hricko
 */
public enum RSDComparator implements Comparator<RecordSchemaDescription>, Serializable {
	INSTANCE;

	@Override
	public int compare(RecordSchemaDescription o1, RecordSchemaDescription o2) {
		// TOHLE JE OPET PRASARNA! MELA BY SE POUZIT METODA COMPARE Z RSD!
		int nameCompare = o1.getName().compareTo(o2.getName());
		boolean typesEqual = o1.getTypes() == o2.getTypes();
		return nameCompare == 0
				? typesEqual
						? 0 : -1
				: nameCompare;     //Ak sa nazov a typy zhoduju, jedna sa o rovnaky element
	}
}
