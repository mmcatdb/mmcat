/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.category;

/**
 *
 * @author pavel.koupil
 */
public class Signature {

	private int signature;

	public Signature(Signature signature) {
		this.signature = signature.signature;
	}

	public Signature(int signature) {
		this.signature = signature;
	}

	public Signature dual() {
		return new Signature(signature * -1);
	}

}
