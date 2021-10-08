/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;

/**
 *
 * @author jachym.bartik
 */
public class PostgreSQLPathWrapper implements AbstractPathWrapper
{
	public void addProperty(String hierarchy)
    {
        // Maybe this could return some more structured object so we doesn't have to parse it's information from string?
    }

	public boolean check()
    {
        return true; // This should be ok
    }

	public boolean isRootObjectAllowed() { return true; }
	public boolean isPropertyToOneAllowed() { return true; }
	public boolean isPropertyToManyAllowed() { return false; }
	public boolean isInliningToOneAllowed() { return true; }
	public boolean isInliningToManyAllowed() { return false; }
	public boolean isGrouppingAllowed() { return false; }
	public boolean isDynamicNamingAllowed() { return false; }
	public boolean isAnonymousNamingAllowed() { return false; }
	public boolean isReferenceAllowed() { return true; }
}
