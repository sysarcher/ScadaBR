/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.web.jsonrpc;

/**
 *
 * @author aploese
 * @param <R>
 * @param <E>
 */
public class JsonRpcResponse<R, E> {
    
    private R result;
    private E error;
    private int id;
    
    public static <R> JsonRpcResponse<R, ?> createSuccessResponse(int id, R result) {
        JsonRpcResponse<R, ?> r = new JsonRpcResponse<>();
        r.id = id;
        r.result = result;
        return r;
    }

    public static <E> JsonRpcResponse<?, E> createErrorResponse(int id, E error) {
        JsonRpcResponse<?, E> r = new JsonRpcResponse<>();
        r.id = id;
        r.error = error;
        return r;
    }

    /**
     * @return the result
     */
    public R getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(R result) {
        this.result = result;
    }

    /**
     * @return the error
     */
    public E getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(E error) {
        this.error = error;
    }

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
    
}
