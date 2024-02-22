package cz.matfyz.wrappermongodb.inference.helpers;

//import cz.cuni.matfyz.mminfer.persister.model.RecordSchemaDescription;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.core.rsd.*;

import java.util.*;

/**
 *
 * @author sebastian.hricko
 */
public enum MapMongoDocument {
	INSTANCE;

	private static final Logger LOGGER = LoggerFactory.getLogger(MapMongoDocument.class);

	public RecordSchemaDescription process(Document t) {
		RecordSchemaDescription result = new RecordSchemaDescription();

		result.setName("_");
		result.setUnique(Char.FALSE);
		result.setShare(new Share());
		result.setId(Char.FALSE);
		result.setTypes(Type.MAP);
		result.setModels(Model.DOC);

		List<RecordSchemaDescription> children = new ArrayList<>();

		t.forEach((key, value) -> children.add(MapMongoRecord.INSTANCE.process(key, value, true, true)));

		Collections.sort(children);

		result.setChildren(children);

		return result;
	}

}
