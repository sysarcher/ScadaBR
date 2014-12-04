/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc.controller.rest;

import br.org.scadabr.dao.PointValueDao;
import br.org.scadabr.logger.LogUtils;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.DoubleValue;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author aploese
 */
@RestController
@Scope("request")
@RequestMapping(value = "/rest/pointValues/")
public class RestPointValuesController {

    private static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private transient PointValueDao pointValueDao;

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public Collection<JsonPointValue> getPointValues(@PathVariable int id, @RequestParam(value = "from", required = false) Long from, @RequestParam(value = "to", required = false) Long to) {
        if (from == null) {
            from = pointValueDao.getInceptionDate(id);
        }
        Iterable<PointValueTime> pvt;
        if (to == null) {
            pvt = pointValueDao.getPointValues(id, from);
        } else {
            pvt = pointValueDao.getPointValuesBetween(id, from, to);
        }
        Collection<JsonPointValue> result = new LinkedList<>();
        for (PointValueTime p : pvt) {
            result.add(new JsonPointValue(p.getTimestamp(), ((PointValueTime<DoubleValue>)p).getMangoValue().getDoubleValue()));
        }
        return result;
    }

}
