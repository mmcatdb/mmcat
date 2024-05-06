/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.algorithms.miner.functions;

import java.io.Serializable;

import cz.matfyz.core.rsd.Share;
import org.apache.spark.api.java.function.Function2;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class ReduceRawPropertiesFunction implements Function2<Share, Share, Share>, Serializable {

	@Override
	public Share call(Share a, Share b) throws Exception {
		return a.add(b);
	}

}