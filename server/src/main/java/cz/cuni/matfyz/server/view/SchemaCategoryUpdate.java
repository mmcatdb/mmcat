package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.server.utils.Position;

/**
 * 
 * @author jachym.bartik
 */
public class SchemaCategoryUpdate {

    public ObjectUpdate[] objects;
    public MorphismUpdate[] morphisms;

    // TODO make normal class?
    public static class ObjectUpdate {

        public int temporaryId;
        public Position position;
        public String jsonValue;

    }

    public static class MorphismUpdate {

        public Integer domId;
        public Integer codId;
        public Integer temporaryDomId;
        public Integer temporaryCodId;
        public String jsonValue;

    }

}
