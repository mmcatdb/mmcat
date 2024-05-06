/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.core.rsd;

import java.io.Serializable;

/**
 *
 * @author pavel.koupil
 */
public class Share implements Serializable {

	private int total;
	private int first;

	public Share() {
		this.total = 1;
		this.first = 1;
	}

	public Share(int total, int first) {
		this.total = total;
		this.first = first;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	@Override
	public String toString() {
		return "Share{" + total + "/" + first + '}';
	}
        
        public Share add(Share other) {
            this.total += other.total;
            this.first += other.first;
            return this;
        }

}
