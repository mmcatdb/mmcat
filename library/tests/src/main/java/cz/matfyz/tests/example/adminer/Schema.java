package cz.matfyz.tests.example.adminer;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.Tag;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObject;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;

public class Schema {

    public static final String schemaLabel = "Adminer Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    // Keys

    public static final BuilderObject user =            builder.object("user", 1);
    public static final BuilderObject userId =          builder.object("user_id", 2);
    public static final BuilderObject name =            builder.object("name", 3);
    public static final BuilderObject createdAt =       builder.object("created_at", 4);
    public static final BuilderObject fans =            builder.object("fans", 5);

    public static final BuilderObject comment =         builder.object("comment", 6);
    public static final BuilderObject commentId =       builder.object("comment_id", 7);
    public static final BuilderObject businessId =      builder.object("business_id", 8);
    public static final BuilderObject date =            builder.object("date", 9);
    public static final BuilderObject text =            builder.object("text", 10);
    public static final BuilderObject stars =           builder.object("stars", 11);

    public static final BuilderObject businessHours =   builder.object("business_hours", 12);
    public static final BuilderObject businessHoursId = builder.object("business_hours_id", 13);
    public static final BuilderObject hours =           builder.object("hours", 14);

    public static final BuilderObject business =        builder.object("business", 15);
    public static final BuilderObject city =            builder.object("city", 16);
    public static final BuilderObject state =           builder.object("state", 17);
    public static final BuilderObject dates =           builder.object("dates", 18);
    public static final BuilderObject attributes =      builder.generatedIds().object("attributes", 19);
    public static final BuilderObject wifi =            builder.object("wifi", 20);
    public static final BuilderObject outdoorSeating =  builder.object("outdoor_seating", 21);

    public static final BuilderObject friend =          builder.object("FRIEND", 22);
    public static final BuilderObject since =           builder.object("since", 23);

    // Signatures

    public static final BuilderMorphism userToUserId =                      builder.morphism(user, userId, 1);
    public static final BuilderMorphism userToName =                        builder.morphism(user, name, 2);
    public static final BuilderMorphism userToCreatedAt =                   builder.morphism(user, createdAt, 3);
    public static final BuilderMorphism userToFans =                        builder.morphism(user, fans, 4);

    public static final BuilderMorphism businessToBusinessId =              builder.morphism(business, businessId, 5);
    public static final BuilderMorphism businessToName =                    builder.morphism(business, name, 6);
    public static final BuilderMorphism businessToCity =                    builder.morphism(business, city, 7);
    public static final BuilderMorphism businessToState =                   builder.morphism(business, state, 8);
    public static final BuilderMorphism businessToDates =                   builder.morphism(business, dates, 9);
    public static final BuilderMorphism businessToAttributes =              builder.morphism(business, attributes, 10);
    public static final BuilderMorphism attributesToWifi =                  builder.morphism(attributes, wifi, 11);
    public static final BuilderMorphism attributesToOutdoorSeating =        builder.morphism(attributes, outdoorSeating, 12);

    public static final BuilderMorphism commentToCommentId =                builder.morphism(comment, commentId, 13);
    public static final BuilderMorphism commentToUser =                     builder.morphism(comment, user, 14);
    public static final BuilderMorphism commentToBusiness =                 builder.morphism(comment, business, 15);
    public static final BuilderMorphism commentToDate =                     builder.morphism(comment, date, 16);
    public static final BuilderMorphism commentToText =                     builder.morphism(comment, text, 17);
    public static final BuilderMorphism commentToStars =                    builder.morphism(comment, stars, 18);

    public static final BuilderMorphism commentToUserId =                   builder.composite(commentToUser, userToUserId);
    public static final BuilderMorphism commentToBusinessId =               builder.composite(commentToBusiness, businessToBusinessId);

    public static final BuilderMorphism businessHoursToBusinessHoursId =    builder.morphism(businessHours, businessHoursId, 19);
    public static final BuilderMorphism businessHoursToBusiness =           builder.morphism(businessHours, business, 20);
    public static final BuilderMorphism businessHoursToHours =              builder.morphism(businessHours, hours, 21);

    public static final BuilderMorphism businessHoursToBusinessId =         builder.composite(businessHoursToBusiness, businessToBusinessId);

    public static final BuilderMorphism friendToSince =                     builder.morphism(friend, since, 22);
    public static final BuilderMorphism friendToFromUser =                  builder.tags(Tag.role).morphism(friend, user, 23);
    public static final BuilderMorphism friendToToUser =                    builder.tags(Tag.role).morphism(friend, user, 24);
    public static final BuilderMorphism frientToFromUserId =                builder.composite(friendToFromUser, userToUserId);
    public static final BuilderMorphism frientToToUserId =                  builder.composite(friendToToUser, userToUserId);

    private static final SchemaBuilder ids = builder
        .ids(user, userToUserId)
        .ids(comment, commentToCommentId)
        .ids(business, businessToBusinessId)
        .ids(businessHours, businessHoursToBusinessHoursId)
        .ids(friend, frientToFromUserId, frientToToUserId);

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchema() {
        return builder.build();
    }

    public static MetadataCategory newMetadata(SchemaCategory schema) {
        return builder.buildMetadata(schema);
    }

}
