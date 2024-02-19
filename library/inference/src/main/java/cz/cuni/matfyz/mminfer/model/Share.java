/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.cuni.matfyz.mminfer.model;

import java.io.Serializable;

/**
 *
 * @author pavel.koupil
 */
public class Share implements Serializable {

    private Integer total;
    private Integer first;

    public Share() {
        this.total = 1;
        this.first = 1;
    }

    public Share(Integer total, Integer first) {
        this.total = total;
        this.first = first;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getFirst() {
        return first;
    }

    public void setFirst(Integer first) {
        this.first = first;
    }

    @Override
    public String toString() {
        return "Share{" + total + "/" + first + '}';
    }

    public Share add(Share other) {
        Share result = new Share();
        result.total = this.total + other.total;
        result.first = this.first + other.first;
        return result;
    }

}
