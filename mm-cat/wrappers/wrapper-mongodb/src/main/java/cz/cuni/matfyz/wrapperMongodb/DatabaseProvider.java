package cz.cuni.matfyz.wrapperMongodb;

import com.mongodb.client.MongoDatabase;

/**
 *
 */
public interface DatabaseProvider
{
    public MongoDatabase getDatabase();
}