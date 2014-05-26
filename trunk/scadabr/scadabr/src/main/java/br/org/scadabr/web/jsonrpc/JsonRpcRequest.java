/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.web.jsonrpc;

import java.util.List;

/**
 *
 * @author aploese
 */
public class JsonRpcRequest {
    
    private int id;
    private String method;
    private List<Object> params;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return the params
     */
    public List<Object> getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(List<Object> params) {
        this.params = params;
    }
}
