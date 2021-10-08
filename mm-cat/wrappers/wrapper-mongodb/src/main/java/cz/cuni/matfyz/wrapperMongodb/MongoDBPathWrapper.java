/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.wrapperMongoDB;

import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;
import java.util.ArrayList;

/**
 *
 * @author jachym.bartik
 */
public class MongoDBPathWrapper implements AbstractPathWrapper
{
    private ArrayList<String> properties = new ArrayList<String>();
    
	public void addProperty(String hierarchy)
    {
        properties.add(hierarchy);
    }

	public boolean check()
    {
        return properties.stream().anyMatch(property -> property.endsWith("/_id"));
    }

	public boolean isRootObjectAllowed() { return true; }
	public boolean isPropertyToOneAllowed() { return true; }
	public boolean isPropertyToManyAllowed() { return true; }
	public boolean isInliningToOneAllowed() { return true; }
	public boolean isInliningToManyAllowed() { return true; }
	public boolean isGrouppingAllowed() { return true; }
	public boolean isDynamicNamingAllowed() { return true; }
	public boolean isAnonymousNamingAllowed() { return true; }
	public boolean isReferenceAllowed() { return true; }
}
