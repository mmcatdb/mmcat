/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.abstractwrappers;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractPathWrapper {

	public abstract void addProperty(String hierarchy);

	public abstract boolean check();

	public abstract boolean isRootObjectAllowed();

	public abstract boolean isPropertyToOneAllowed();

	public abstract boolean isPropertyToManyAllowed();

	public abstract boolean isInliningToOneAllowed();

	public abstract boolean isInliningToManyAllowed();

	public abstract boolean isGrouppingAllowed();

	public abstract boolean isDynamicNamingAllowed();

	public abstract boolean isAnonymousNamingAllowed();

	public abstract boolean isReferenceAllowed();
}
