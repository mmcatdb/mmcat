/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.utils;

/**
 *
 * @author pavel.koupil
 */
public class BasicHashFunction implements HashFunction {

    @Override
    public Integer apply(Object value) {
        return Math.abs(value.toString().hashCode() % 10);
    }

}
