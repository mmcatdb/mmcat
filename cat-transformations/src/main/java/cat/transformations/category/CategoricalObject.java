/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.category;

import java.util.List;

/**
 *
 * @author pavel.contos
 */
public interface CategoricalObject {

	public abstract void add(Object object);
	
	public abstract String getName();

    public int size();
	
}
