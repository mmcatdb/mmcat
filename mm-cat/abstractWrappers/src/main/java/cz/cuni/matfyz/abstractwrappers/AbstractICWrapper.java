/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.core.mapping.IdentifierStructure;
import cz.cuni.matfyz.core.util.Pair;
import cz.cuni.matfyz.statements.ICStatement;
import java.util.Set;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractICWrapper {

	public abstract void appendIdentifier(String name, IdentifierStructure identifier);

	public abstract void appendReference(String name, Set<Pair<String, String>> atts);

	public abstract ICStatement createICStatement();

	public abstract ICStatement createICRemoveStatement();

}
