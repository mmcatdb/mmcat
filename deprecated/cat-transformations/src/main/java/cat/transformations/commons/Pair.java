package cat.transformations.commons;

import cat.transformations.algorithms2.model.AbstractCategoricalObject;

/**
 *
 * @author pavel.contos
 * @param <X>
 * @param <Y>
 */
public class Pair<X extends Comparable<X>, Y extends Comparable<Y>> implements Comparable<Pair<X, Y>> {

	private final X x;
	private final Y y;

	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	public Pair(AbstractCategoricalObject domain, AbstractCategoricalObject codomain) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public X getX() {
		return x;
	}

	public Y getY() {
		return y;
	}

	@Override
	public int compareTo(Pair<X, Y> other) {
		int xComparison = x.compareTo(other.getX());
		return xComparison == 0 ? y.compareTo(other.getY()) : xComparison;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		builder.append(x);
		builder.append(",");
		builder.append(y);
		builder.append(")");
		return builder.toString();
	}

}
