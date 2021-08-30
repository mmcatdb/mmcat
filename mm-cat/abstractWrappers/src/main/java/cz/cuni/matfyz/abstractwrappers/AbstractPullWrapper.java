/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.Forest;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractPullWrapper {

	public abstract Forest pullForest(String selectAll, AccessPath path);

	public abstract Forest pullForest(String selectAll, AccessPath path, int limit, int offset);

}
