package cz.matfyz.server.utils;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.builder.MetadataContext;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.Position;

import java.util.Map;

public class SchemaCategoryUtil {

    public static SchemaCategoryWrapper createWrapperFromCategory(SchemaCategory category) {
        MetadataContext context = new MetadataContext();

        context.setId(new Id(null)); // is null ok?
        context.setVersion(Version.generateInitial());

        Map<Key, Position> positions = LayoutUtil.layoutObjects(category);
        positions.forEach(context::setPosition);

        return SchemaCategoryWrapper.fromSchemaCategory(category, context);
    }

}
