package cz.matfyz.server.example.adminer;

import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.server.example.common.SchemaBase;
import cz.matfyz.tests.example.adminer.Schema;

class SchemaSetup extends SchemaBase {

    private SchemaSetup(SchemaCategoryEntity entity) {
        super(entity, Schema.newSchema());
    }

    static SchemaEvolutionInit createNewUpdate(SchemaCategoryEntity entity) {
        return new SchemaSetup(entity).innerCreateNewUpdate();
    }

    @Override protected void createOperations() {
        // User
        addObjex(Schema.user, 3.5, 4);
        addProperty(Schema.userId, Schema.user_userId, 5, 3);
        addProperty(Schema.name, Schema.user_name, 4, 3);
        addProperty(Schema.createdAt, Schema.user_createdAt, 3, 3);
        addProperty(Schema.fans, Schema.user_fans, 2, 3);
        addIds(Schema.user);

        // Comment
        addObjex(Schema.comment, 0.5, 5);
        addMorphism(Schema.comment_user);
        addProperty(Schema.commentId, Schema.comment_commentId, 1, 6);
        addProperty(Schema.date, Schema.comment_date, 0, 6);
        addProperty(Schema.text, Schema.comment_text, -1, 6);
        addProperty(Schema.stars, Schema.comment_stars, 2, 6);
        addIds(Schema.comment);

        // Business
        addObjex(Schema.business, -3.5, 3);
        addMorphism(Schema.comment_business);
        addProperty(Schema.businessId, Schema.business_businessId, -2, 1);
        addProperty(Schema.city, Schema.business_city, -3, 1);
        addProperty(Schema.state, Schema.business_state, -4, 1);
        addProperty(Schema.dates, Schema.business_dates, -5, 1);
        addIds(Schema.business);

        // Attributes
        addProperty(Schema.attributes, Schema.business_attributes, -6, 1);
        addProperty(Schema.wifi, Schema.attributes_wifi, -5.5, 0);
        addProperty(Schema.outdoorSeating, Schema.attributes_outdoorSeating, -6.5, 0);
        addIds(Schema.attributes);

        // Business Hours
        addObjex(Schema.businessHours, 2, 1);
        addMorphism(Schema.businessHours_business);
        addProperty(Schema.businessHoursId, Schema.businessHours_businessHoursId, 1.5, 0);
        addProperty(Schema.hours, Schema.businessHours_hours, 2.5, 0);
        addIds(Schema.businessHours);

        // Friend
        addObjex(Schema.friend, 5, 6);
        addMorphism(Schema.friend_fromUser);
        addMorphism(Schema.friend__user);
        addProperty(Schema.since, Schema.friend_since, 7, 3);
        addIds(Schema.friend);
    }

}
