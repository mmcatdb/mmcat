package cz.matfyz.tests.example.adminer;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.Tag;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;

public class Schema {

    public static final String schemaLabel = "Adminer Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    // Keys

    public static final BuilderObjex user =            builder.objex("user", 1);
    public static final BuilderObjex userId =          builder.objex("user_id", 2);
    public static final BuilderObjex name =            builder.objex("name", 3);
    public static final BuilderObjex createdAt =       builder.objex("created_at", 4);
    public static final BuilderObjex fans =            builder.objex("fans", 5);

    public static final BuilderObjex comment =         builder.objex("comment", 6);
    public static final BuilderObjex commentId =       builder.objex("comment_id", 7);
    public static final BuilderObjex businessId =      builder.objex("business_id", 8);
    public static final BuilderObjex date =            builder.objex("date", 9);
    public static final BuilderObjex text =            builder.objex("text", 10);
    public static final BuilderObjex stars =           builder.objex("stars", 11);

    public static final BuilderObjex businessHours =   builder.objex("business_hours", 12);
    public static final BuilderObjex businessHoursId = builder.objex("business_hours_id", 13);
    public static final BuilderObjex hours =           builder.objex("hours", 14);

    public static final BuilderObjex business =        builder.objex("business", 15);
    public static final BuilderObjex city =            builder.objex("city", 16);
    public static final BuilderObjex state =           builder.objex("state", 17);
    public static final BuilderObjex dates =           builder.objex("dates", 18);
    public static final BuilderObjex attributes =      builder.generatedIds().objex("attributes", 19);
    public static final BuilderObjex wifi =            builder.objex("wifi", 20);
    public static final BuilderObjex outdoorSeating =  builder.objex("outdoor_seating", 21);

    public static final BuilderObjex friend =          builder.objex("FRIEND", 22);
    public static final BuilderObjex since =           builder.objex("since", 23);

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

    // Ids

    static {

        builder
            .ids(user, userToUserId)
            .ids(comment, commentToCommentId)
            .ids(business, businessToBusinessId)
            .ids(businessHours, businessHoursToBusinessHoursId)
            .ids(friend, frientToFromUserId, frientToToUserId);

    }

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
