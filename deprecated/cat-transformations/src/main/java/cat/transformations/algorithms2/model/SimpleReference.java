package cat.transformations.algorithms2.model;

/**
 *
 * @author pavel.contos
 */
public class SimpleReference implements AbstractReference {

	@Override
	public String getReferenced() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int compareTo(AbstractValue o) {
		return -1;
	}

}
