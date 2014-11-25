/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc.controller.datasources;

import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.utils.TimePeriods;
import br.org.scadabr.vo.LoggingTypes;
import br.org.scadabr.vo.dataSource.PointLocatorVO;
import br.org.scadabr.vo.datasource.DataSourcesRegistry;
import br.org.scadabr.web.l10n.RequestContextAwareLocalizer;
import com.googlecode.jsonrpc4j.JsonRpcService;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.view.chart.ImageChartRenderer;
import com.serotonin.mango.view.text.AnalogRenderer;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 *
 * @author aploese
 */
@Named
@JsonRpcService("/dataSources/rpc/")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RpcController {
    
    private final static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private DataSourceDao dataSourceDao;
    @Inject
    private RequestContextAwareLocalizer localizer;
    @Inject
    private DataPointDao dataPointDao;
    @Inject
    private RuntimeManager runtimeManager;
    @Inject
    private DataSourcesRegistry dataSourcesRegistry;
    
    public JsonDataSource addDataSource(String type) {
        DataSourceVO result = dataSourcesRegistry.createDataSourceVO(type);
        dataSourceDao.saveDataSource(result);
        return new JsonDataSource(result, localizer);
    }

    public boolean deleteDataSource(int id) {
        try {
            runtimeManager.deleteDataSource(id);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error saving DataSource with id: " + id, e);
            return false;
        }
    }

    public PointLocatorVO addPointLocator(int dataSourceId, int pointlOcatorId) {
        PointLocatorVO result = dataSourceDao.getDataSource(dataSourceId).createPointLocator();
        DataPointVO dp = new DataPointVO();
        dp.setName(result.getClass().getSimpleName());
        dp.setDataSourceId(dataSourceId);
        dp.setPointLocator(result);
        dp.setEnabled(false);
        dp.setLoggingType(LoggingTypes.ALL);
        dp.setEventDetectors(new ArrayList<PointEventDetectorVO>());
        dp.setTextRenderer(new AnalogRenderer("#,##0.0", ""));
        dp.setChartRenderer(new ImageChartRenderer(TimePeriods.DAYS, 1));
        dataPointDao.saveDataPoint(dp);
        return result;
    }

    public JsonDataSource startDataSource(int id) {
        DataSourceVO dsVo = dataSourceDao.getDataSource(id);
        dsVo.setEnabled(true);
        runtimeManager.saveDataSource(dsVo);
        return new JsonDataSource(dsVo, localizer);
    }
    
    public JsonDataSource stopDataSource(int id) {
        DataSourceVO dsVo = dataSourceDao.getDataSource(id);
        dsVo.setEnabled(false);
        runtimeManager.saveDataSource(dsVo);
        return new JsonDataSource(dsVo, localizer);
    }

    public JsonPointLocator startPointLocator(int id) {
        DataPointVO dpVo = dataPointDao.getDataPoint(id);
        dpVo.setEnabled(true);
        runtimeManager.saveDataPoint(dpVo);
        return new JsonPointLocator(dpVo.getPointLocator(), localizer);
    }
    
    public JsonPointLocator stopPointLocator(int id) {
        DataPointVO dpVo = dataPointDao.getDataPoint(id);
        dpVo.setEnabled(false);
        runtimeManager.saveDataPoint(dpVo);
        return new JsonPointLocator(dpVo.getPointLocator(), localizer);
    }

}
