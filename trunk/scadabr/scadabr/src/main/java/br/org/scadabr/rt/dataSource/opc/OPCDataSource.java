package br.org.scadabr.rt.dataSource.opc;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jinterop.dcom.common.JISystem;

import br.org.scadabr.OPCMaster;
import br.org.scadabr.RealOPCMaster;
import br.org.scadabr.vo.dataSource.opc.OPCDataSourceVO;
import br.org.scadabr.vo.dataSource.opc.OPCPointLocatorVO;

import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataSource.PollingDataSource;
import com.serotonin.web.i18n.LocalizableMessage;

public class OPCDataSource extends PollingDataSource {

    private final static Logger LOG = LoggerFactory.getLogger(OPCDataSource.class);
	public static final int POINT_READ_EXCEPTION_EVENT = 1;
	public static final int DATA_SOURCE_EXCEPTION_EVENT = 2;
	public static final int POINT_WRITE_EXCEPTION_EVENT = 3;
	private OPCMaster opcMaster;
	private final OPCDataSourceVO<?> vo;

	public OPCDataSource(OPCDataSourceVO<?> vo) {
		super(vo, true);
		this.vo = vo;
		setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(),
				vo.isQuantize());

		JISystem.getLogger().setLevel(java.util.logging.Level.OFF);

	}

	@Override
	protected void doPoll(long time) {
		ArrayList<String> enabledTags = new ArrayList<String>();
		for (DataPointRT dataPoint : enabledDataPoints) {
			OPCPointLocatorVO dataPointVO = dataPoint.getVO().getPointLocator();
			enabledTags.add(dataPointVO.getTag());
		}

		try {
			opcMaster.configureGroup(enabledTags);
		} catch (Exception e) {
			raiseEvent(
					DATA_SOURCE_EXCEPTION_EVENT,
					time,
					true,
					new LocalizableMessage("event.exception2", vo.getName(), e
							.getMessage()));
		}

		try {
			opcMaster.doPoll();
			returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, time);

		} catch (Exception e) {
			raiseEvent(
					DATA_SOURCE_EXCEPTION_EVENT,
					time,
					true,
					new LocalizableMessage("event.exception2", vo.getName(), e
							.getMessage()));
		}

		for (DataPointRT dataPoint : enabledDataPoints) {
			OPCPointLocatorVO dataPointVO = dataPoint.getVO().getPointLocator();
			MangoValue mangoValue = null;
			String value = "0";
			try {
				value = opcMaster.getValue(dataPointVO.getTag());

				mangoValue = MangoValue.stringToValue(value,
						dataPointVO.getMangoDataType());
				dataPoint
						.updatePointValue(new PointValueTime(mangoValue, time));
			} catch (Exception e) {
				raiseEvent(POINT_READ_EXCEPTION_EVENT, time, true,
						new LocalizableMessage("event.exception2",
								vo.getName(), e.getMessage()));
			}
		}
	}

	@Override
	public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime,
			SetPointSource source) {
		String tag = ((OPCPointLocatorVO) dataPoint.getVO().getPointLocator())
				.getTag();
		Object value = null;
		if (dataPoint.getMangoDataType() == MangoDataType.NUMERIC)
			value = valueTime.getDoubleValue();
		else if (dataPoint.getMangoDataType() == MangoDataType.BINARY)
			value = valueTime.getBooleanValue();
		else if (dataPoint.getMangoDataType() == MangoDataType.MULTISTATE)
			value = valueTime.getIntegerValue();
		else
			value = valueTime.getStringValue();

		try {
			opcMaster.write(tag, value);
		} catch (Exception e) {
			raiseEvent(
					POINT_WRITE_EXCEPTION_EVENT,
					System.currentTimeMillis(),
					true,
					new LocalizableMessage("event.exception2", vo.getName(), e
							.getMessage()));
			e.printStackTrace();
		}
	}

	public void initialize() {
		this.opcMaster = new RealOPCMaster();
		opcMaster.setHost(vo.getHost());
		opcMaster.setDomain(vo.getDomain());
		opcMaster.setUser(vo.getUser());
		opcMaster.setPassword(vo.getPassword());
		opcMaster.setServer(vo.getServer());
		opcMaster.setDataSourceXid(vo.getXid());

		try {
			opcMaster.init();
			returnToNormal(DATA_SOURCE_EXCEPTION_EVENT,
					System.currentTimeMillis());
		} catch (Exception e) {
			raiseEvent(
					DATA_SOURCE_EXCEPTION_EVENT,
					System.currentTimeMillis(),
					true,
					new LocalizableMessage("event.exception2", vo.getName(), e
							.getMessage()));
			LOG.debug("Error while initializing data source", e);
			return;
		}
		super.initialize();
	}

	@Override
	public void terminate() {
		super.terminate();
		try {
			opcMaster.terminate();
		} catch (Exception e) {
			raiseEvent(
					DATA_SOURCE_EXCEPTION_EVENT,
					System.currentTimeMillis(),
					true,
					new LocalizableMessage("event.exception2", vo.getName(), e
							.getMessage()));
		}
	}

}
