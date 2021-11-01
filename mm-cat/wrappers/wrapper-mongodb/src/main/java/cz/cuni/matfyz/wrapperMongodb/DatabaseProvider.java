package cz.cuni.matfyz.wrapperMongodb;

import com.mongodb.client.MongoDatabase;

/**
 *
 * @author jachymb.bartik
 */
public interface DatabaseProvider
{
    public MongoDatabase getDatabase();
}