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
            addMorphism(Schema.userToUserId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.name, 4, 3);
            addMorphism(Schema.userToName);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.createdAt, 3, 3);
            addMorphism(Schema.userToCreatedAt);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.fans, 2, 3);
            addMorphism(Schema.userToFans);
        });
        addIds(Schema.user);

        // Comment
        addObjex(Schema.comment, 0.5, 5);
        addMorphism(Schema.commentToUser);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.commentId, 1, 6);
            addMorphism(Schema.commentToCommentId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.date, 0, 6);
            addMorphism(Schema.commentToDate);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.text, -1, 6);
            addMorphism(Schema.commentToText);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.stars, 2, 6);
            addMorphism(Schema.commentToStars);
        });
        addIds(Schema.comment);

        // Business
        addObjex(Schema.business, -3.5, 3);
        addMorphism(Schema.commentToBusiness);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.businessId, -2, 1);
            addMorphism(Schema.businessToBusinessId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.city, -3, 1);
            addMorphism(Schema.businessToCity);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.state, -4, 1);
            addMorphism(Schema.businessToState);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.dates, -5, 1);
            addMorphism(Schema.businessToDates);
        });
        addIds(Schema.business);

        // Attributes
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.attributes, -6, 1);
            addMorphism(Schema.businessToAttributes);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.wifi, -5.5, 0);
            addMorphism(Schema.attributesToWifi);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.outdoorSeating, -6.5, 0);
            addMorphism(Schema.attributesToOutdoorSeating);
        });
        addIds(Schema.attributes);

        // Business Hours
        addObjex(Schema.businessHours, 2, 1);
        addMorphism(Schema.businessHoursToBusiness);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.businessHoursId, 1.5, 0);
            addMorphism(Schema.businessHoursToBusinessHoursId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.hours, 2.5, 0);
            addMorphism(Schema.businessHoursToHours);
        });
        addIds(Schema.businessHours);

        // Friend
        addObjex(Schema.friend, 5, 6);
        addMorphism(Schema.friendToFromUser);
        addMorphism(Schema.friendToToUser);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.since, 7, 3);
            addMorphism(Schema.friendToSince);
        });
        addIds(Schema.friend);
    }

}
