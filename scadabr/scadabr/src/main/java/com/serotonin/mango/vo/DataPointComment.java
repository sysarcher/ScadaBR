/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.vo;


/**
 * Thios class encapsulates the comments on datapoints.
 * 
 * @author aploese
 */
public class DataPointComment extends UserComment {

    private int dataPointId;

    public DataPointComment() {
        super();
    }
    
    public DataPointComment(User user, String comment, int dataPointId) {
        super(user, comment);
        this.dataPointId = dataPointId;
    }

    /**
     * @return the dataPointId
     */
    public int getDataPointId() {
        return dataPointId;
    }

    /**
     * @param dataPointId the dataPointId to set
     */
    public void setDataPointId(int dataPointId) {
        this.dataPointId = dataPointId;
    }
    
}
