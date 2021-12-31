/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.cuni.matfyz.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pavel.koupil
 */
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String... args) {
		LOGGER.debug("TEST - DEBUG");
		LOGGER.info("TEST - INFO");
		LOGGER.warn("TEST - WARN");
		LOGGER.error("TEST - ERROR");
	}

}
