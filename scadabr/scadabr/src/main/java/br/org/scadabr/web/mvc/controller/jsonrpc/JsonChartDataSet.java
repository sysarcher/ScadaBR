/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.web.mvc.controller.jsonrpc;

import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.vo.DataPointVO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sf.openv4j.DataPoint;

/**
 *
 * @author aploese
 */
public class JsonChartDataSet {

    private List<XYDataSet> xyDataSet; 
    private String name;
    private long from;
    private long to;
    
    JsonChartDataSet(long from, long to, DataPointVO dp, Collection<PointValueTime> pvt) {
        this.name = dp.getName();
        this.from = from;
        this.to = to;
        xyDataSet = new ArrayList<>(pvt.size());
        for (PointValueTime p : pvt) {
            xyDataSet.add(new XYDataSet(p.getTime(),p.getDoubleValue()));
        }
    }

    JsonChartDataSet() {
    }

    /**
     * @return the xyDataSet
     */
    public Collection<XYDataSet> getXyDataSet() {
        return xyDataSet;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the from
     */
    public long getFrom() {
        return from;
    }

    /**
     * @return the to
     */
    public long getTo() {
        return to;
    }

    
}