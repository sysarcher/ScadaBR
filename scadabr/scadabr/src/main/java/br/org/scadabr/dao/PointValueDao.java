/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao;

import br.org.scadabr.DataType;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataImage.types.DoubleValue;
import java.util.List;
import javax.inject.Named;

/**
 *
 * @author aploese
 */
@Named
public interface PointValueDao {

    long deletePointValuesWithMismatchedType(int id, DataType dataType);

    void savePointValueAsync(PointValueTime<DoubleValue> newValue, SetPointSource source);

    PointValueTime savePointValueSync(PointValueTime newValue, SetPointSource source);

    PointValueTime getLatestPointValue(int id);

    long getInceptionDate(int id);

    Iterable<PointValueTime> getPointValues(int id, long from);

    Iterable<PointValueTime> getPointValuesBetween(int id, long from, long to);

    List<Long> getFiledataIds();

    PointValueTime getPointValueBefore(int id, long reportStartTime);
    
}
