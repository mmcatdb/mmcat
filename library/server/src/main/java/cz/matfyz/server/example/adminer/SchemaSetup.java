package cz.matfyz.server.example.adminer;

import cz.matfyz.server.entity.SchemaCategoryEntity;
import cz.matfyz.server.example.common.SchemaBase;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;
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
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.userId, 5, 3);
            addMorphism(Schema.user_userId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.name, 4, 3);
            addMorphism(Schema.user_name);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.createdAt, 3, 3);
            addMorphism(Schema.user_createdAt);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.fans, 2, 3);
            addMorphism(Schema.user_fans);
        });
        addIds(Schema.user);

        // Comment
        addObjex(Schema.comment, 0.5, 5);
        addMorphism(Schema.comment_user);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.commentId, 1, 6);
            addMorphism(Schema.comment_commentId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.date, 0, 6);
            addMorphism(Schema.comment_date);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.text, -1, 6);
            addMorphism(Schema.comment_text);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.stars, 2, 6);
            addMorphism(Schema.comment_stars);
        });
        addIds(Schema.comment);

        // Business
        addObjex(Schema.business, -3.5, 3);
        addMorphism(Schema.comment_business);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.businessId, -2, 1);
            addMorphism(Schema.business_businessId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.city, -3, 1);
            addMorphism(Schema.business_city);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.state, -4, 1);
            addMorphism(Schema.business_state);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.dates, -5, 1);
            addMorphism(Schema.business_dates);
        });
        addIds(Schema.business);

        // Attributes
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.attributes, -6, 1);
            addMorphism(Schema.business_attributes);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.wifi, -5.5, 0);
            addMorphism(Schema.attributes_wifi);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.outdoorSeating, -6.5, 0);
            addMorphism(Schema.attributes_outdoorSeating);
        });
        addIds(Schema.attributes);

        // Business Hours
        addObjex(Schema.businessHours, 2, 1);
        addMorphism(Schema.businessHours_business);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.businessHoursId, 1.5, 0);
            addMorphism(Schema.businessHours_businessHoursId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.hours, 2.5, 0);
            addMorphism(Schema.businessHours_hours);
        });
        addIds(Schema.businessHours);

        // Friend
        addObjex(Schema.friend, 5, 6);
        addMorphism(Schema.friend_fromUser);
        addMorphism(Schema.friend__user);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.since, 7, 3);
            addMorphism(Schema.friend_since);
        });
        addIds(Schema.friend);
    }

}
