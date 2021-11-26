/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.cuni.matfyz.wrapper.dummy;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class DataProvider {
	
	/**
	 *
	 * @return
	 */
	public static List<String> getData() {
		List<String> data = new ArrayList<>();
		
		// nacitat ze souboru po radcich
		// kazdy radek = 1 json dokument
		
		data.add("""
           {
			....
           }
           """);
		data.add("""
           {
			....
           }
           """);
		data.add("""
           {
			....
           }
           """);
		
		return data;
	}
	
}
