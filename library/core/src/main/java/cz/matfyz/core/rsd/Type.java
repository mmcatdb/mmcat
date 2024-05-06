/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.core.rsd;

/**
 *
 * @author pavel.koupil
 */
public abstract class Type {

	public static final int UNKNOWN = 0;
	public static final int OBJECT = (int) Math.pow(2, 0);
	public static final int STRING = (int) Math.pow(2, 1);
	public static final int BOOLEAN = (int) Math.pow(2, 2);
	public static final int NUMBER = (int) Math.pow(2, 3);
	public static final int ARRAY = (int) Math.pow(2, 4);
	public static final int MAP = (int) Math.pow(2, 5);
	public static final int DATE = (int) Math.pow(2, 6);

}
