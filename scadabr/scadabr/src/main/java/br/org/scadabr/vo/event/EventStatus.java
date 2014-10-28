/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.vo.event;

/**
 *
 * @author aploese
 */
public enum EventStatus {
    ACTIVE,
    RTN,
    NORTN;
    
    public String getName() {
        return name();
    }
    
    public final static String ANY = "ANY";
}
