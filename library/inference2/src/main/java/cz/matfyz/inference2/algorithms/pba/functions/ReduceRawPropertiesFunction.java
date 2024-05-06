/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms.pba.functions;

import cz.matfyz.core.rsd2.Share;
import java.io.Serializable;
import org.apache.spark.api.java.function.Function2;

/**
 *
 * @author pavel.koupil
 */
public class ReduceRawPropertiesFunction implements Function2<Share, Share, Share>, Serializable {

	@Override
	public Share call(Share share1, Share share2) throws Exception {
		share1.setFirst(share1.getFirst() + share2.getFirst());
		share1.setTotal(share1.getTotal() + share2.getTotal());
		return share1;
	}

}
