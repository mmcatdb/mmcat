package cz.cuni.matfyz.wrappermongodb;

import com.mongodb.client.MongoDatabase;

/**
 * @author jachymb.bartik
 */
public interface DatabaseProvider {
    
    public MongoDatabase getDatabase();
    
}