/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.mapping;

/**
 *
 * @author pavel.koupil
 */
public class Name {
	
	public Context context;

	public Type type;

	public static enum Type {
		STATIC_NAME,
		SIGNATURE,
		DYNAMIC_NAME,
		TODO;

	}

}
