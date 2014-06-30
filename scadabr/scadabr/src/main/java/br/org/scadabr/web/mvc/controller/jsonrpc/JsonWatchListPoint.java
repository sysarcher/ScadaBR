package br.org.scadabr.web.mvc.controller.jsonrpc;

import br.org.scadabr.web.l10n.Localizer;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.view.chart.ChartRenderer;
import com.serotonin.mango.view.chart.ChartType;
import com.serotonin.mango.vo.DataPointVO;
import java.io.Serializable;

/**
 *
 * @author aploese
 */
public class JsonWatchListPoint implements Serializable {

    private String canonicalName;
    private int id;
    private boolean settable;
    private String timestamp;
    private String value;
    private ChartType chartType;

    JsonWatchListPoint(DataPointVO dp, DataPointRT dpRt, DataPointDao dataPointDao, Localizer localizer) {
        id = dp.getId();
        settable = dp.isSettable();
        canonicalName = dataPointDao.getCanonicalPointName(dp);
        if (dpRt != null) {
            PointValueTime pvt = dpRt.getPointValue();

            timestamp = localizer.localizeTimeStamp(pvt.getTime(), true);
            value = dp.getTextRenderer().getText(pvt, 0);//TODO hint????
            final ChartRenderer renderer = dp.getChartRenderer();
            if (renderer != null) {
                chartType = renderer.getType();
            } else {
                chartType = ChartType.NONE;
            }
        }
    }

    /**
     * @return the canonicalName
     */
    public String getCanonicalName() {
        return canonicalName;
    }

    /**
     * @param canonicalName the canonicalName to set
     */
    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
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

    /**
     * @return the settable
     */
    public boolean isSettable() {
        return settable;
    }

    /**
     * @param settable the settable to set
     */
    public void setSettable(boolean settable) {
        this.settable = settable;
    }

    /**
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the chartType
     */
    public ChartType getChartType() {
        return chartType;
    }

    /**
     * @param chartType the chartType to set
     */
    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

}
