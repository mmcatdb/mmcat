package cz.matfyz.server.example.adminer;

import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.example.common.SchemaBase;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.tests.example.adminer.Schema;

class SchemaSetup extends SchemaBase {

    private SchemaSetup(SchemaCategoryWrapper wrapper) {
        super(wrapper, Schema.newSchema());
    }

    static SchemaEvolutionInit createNewUpdate(SchemaCategoryWrapper wrapper) {
        return new SchemaSetup(wrapper).innerCreateNewUpdate();
    }

    @Override protected void createOperations() {
        // User
        addObject(Schema.user, 0, 0);
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.userId, 0, -1);
            addMorphism(Schema.userToUserId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.name, -1, -1);
            addMorphism(Schema.userToName);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.createdAt, -2, -1);
            addMorphism(Schema.userToCreatedAt);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.fans, -3, -1);
            addMorphism(Schema.userToFans);
        });
        addIds(Schema.user);

        // Comment
        addObject(Schema.comment, 1, 0);
        addMorphism(Schema.commentToUser);
        addMorphism(Schema.commentToBusiness);
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.commentId, 1, -1);
            addMorphism(Schema.commentToCommentId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.date, 0, -1);
            addMorphism(Schema.commentToDate);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.text, -1, -1);
            addMorphism(Schema.commentToText);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.stars, -2, -1);
            addMorphism(Schema.commentToStars);
        });
        addIds(Schema.comment);

        // Business
        addObject(Schema.business, -1, 1);
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.businessId, -2, 1);
            addMorphism(Schema.businessToBusinessId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.city, -3, 1);
            addMorphism(Schema.businessToCity);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.state, -4, 1);
            addMorphism(Schema.businessToState);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.dates, -5, 1);
            addMorphism(Schema.businessToDates);
        });
        addIds(Schema.business);

        // Attributes
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.attributes, -6, 1);
            addMorphism(Schema.businessToAttributes);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.wifi, -7, 1);
            addMorphism(Schema.attributesToWifi);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.outdoorSeating, -8, 1);
            addMorphism(Schema.attributesToOutdoorSeating);
        });
        addIds(Schema.attributes);

        // Business Hours
        addComposite(ADD_SET, () -> {
            addObject(Schema.businessHours, 2, 1);
            addMorphism(Schema.businessHoursToBusiness);
            addMorphism(Schema.businessHoursToBusinessId);
            addIds(Schema.businessHours);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.businessHoursId, 3, 1);
            addMorphism(Schema.businessHoursToBusinessHoursId);
        });
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.hours, 4, 1);
            addMorphism(Schema.businessHoursToHours);
        });

        // Friend
        addObject(Schema.friend, -1, -2);
        addMorphism(Schema.friendToUser);
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.since, -2, -2);
            addMorphism(Schema.friendToSince);
        });
        addIds(Schema.friend);

    }

}
