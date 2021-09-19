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
public class Context {

	public Type type;

	public enum Type {
		SIGNATURE;
	}

	public Context(Context context, Value value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
