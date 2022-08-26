package cz.cuni.matfyz.server.entity.schema;

import java.util.Arrays;

/**
 * @author jachym.bartik
 */
public record SchemaCategoryUpdate(
    SchemaObjectUpdate[] objects,
    SchemaMorphismUpdate[] morphisms
) {

    @Override
    public String toString() {
        return "SchemaCategoryUpdate []";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(morphisms);
        result = prime * result + Arrays.hashCode(objects);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SchemaCategoryUpdate other = (SchemaCategoryUpdate) obj;
        if (!Arrays.equals(morphisms, other.morphisms))
            return false;
        if (!Arrays.equals(objects, other.objects))
            return false;
        return true;
    }
}
