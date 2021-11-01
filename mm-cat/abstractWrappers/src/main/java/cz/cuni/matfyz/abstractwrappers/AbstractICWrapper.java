/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.core.mapping.IdentifierStructure;
import cz.cuni.matfyz.core.utils.Pair;
import cz.cuni.matfyz.statements.ICStatement;
import java.util.Set;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractICWrapper {

	public abstract void appendIdentifier(String kindName, IdentifierStructure identifier);

	public abstract void appendReference(String kindName, String kindName2, Set<Pair<String, String>> attributePairs);

	public abstract ICStatement createICStatement();

	public abstract ICStatement createICRemoveStatement();

}
