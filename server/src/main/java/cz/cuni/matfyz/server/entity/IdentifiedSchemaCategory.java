package cz.cuni.matfyz.server.entity;

import cz.cuni.matfyz.core.schema.SchemaCategory;

/**
 * 
 * @author jachym.bartik
 */
public class IdentifiedSchemaCategory
{
    public final String id;
    public final SchemaCategory category;

    public IdentifiedSchemaCategory(String id, SchemaCategory category)
    {
        this.id = id;
        this.category = category;
    }
}
