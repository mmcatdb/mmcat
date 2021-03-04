/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.commons;

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
