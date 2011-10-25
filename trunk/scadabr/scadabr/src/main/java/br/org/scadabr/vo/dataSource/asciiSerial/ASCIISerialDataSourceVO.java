package br.org.scadabr.vo.dataSource.asciiSerial;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import br.org.scadabr.rt.dataSource.asciiSerial.ASCIISerialDataSource;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.vo.dataSource.DataSourceRegistry;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.dataSource.PointLocatorVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

@JsonRemoteEntity
public class ASCIISerialDataSourceVO<T extends ASCIISerialDataSourceVO<?>>
		extends DataSourceVO<T> {

	@Override
	protected void addEventTypes(List<EventTypeVO> eventTypes) {
		eventTypes.add(createEventType(
				ASCIISerialDataSource.POINT_READ_EXCEPTION_EVENT,
				new LocalizableMessage("event.ds.pointRead")));
		eventTypes.add(createEventType(
				ASCIISerialDataSource.DATA_SOURCE_EXCEPTION_EVENT,
				new LocalizableMessage("event.ds.dataSource")));

	}

	private static final ExportCodes EVENT_CODES = new ExportCodes();
	static {
		EVENT_CODES.addElement(
				ASCIISerialDataSource.DATA_SOURCE_EXCEPTION_EVENT,
				"DATA_SOURCE_EXCEPTION");
		EVENT_CODES.addElement(
				ASCIISerialDataSource.POINT_READ_EXCEPTION_EVENT,
				"POINT_READ_EXCEPTION");
	}

	@Override
	protected void addPropertiesImpl(List<LocalizableMessage> list) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addPropertyChangesImpl(List<LocalizableMessage> list, T from) {
		// TODO Auto-generated method stub

	}

	@Override
	public DataSourceRT createDataSourceRT() {
		return new ASCIISerialDataSource(this);
	}

	@Override
	public PointLocatorVO createPointLocator() {
		return new ASCIISerialPointLocatorVO();
	}

	@Override
	public LocalizableMessage getConnectionDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExportCodes getEventCodes() {
		return EVENT_CODES;
	}

	@Override
	public DataSourceRegistry getType() {
		return DataSourceRegistry.ASCII_SERIAL;
	}

	private int updatePeriodType = Common.TimePeriods.SECONDS;
	@JsonRemoteProperty
	private int updatePeriods = 1;
	@JsonRemoteProperty
	private String commPortId;
	@JsonRemoteProperty
	private int baudRate = 9600;
	@JsonRemoteProperty
	private int dataBits = 8;
	@JsonRemoteProperty
	private int stopBits = 1;
	@JsonRemoteProperty
	private int parity = 0;
	@JsonRemoteProperty
	private int timeout = 300;
	@JsonRemoteProperty
	private int retries = 2;
	@JsonRemoteProperty
	private int stopMode = 0;
	@JsonRemoteProperty
	private int nChar = 1;
	@JsonRemoteProperty
	private int charStopMode = 0;
	@JsonRemoteProperty
	private String charX = "";
	@JsonRemoteProperty
	private String hexValue = "";
	@JsonRemoteProperty
	private int stopTimeout = 1000;
	@JsonRemoteProperty
	private String initString = "";
	@JsonRemoteProperty
	private int bufferSize = 2;
	@JsonRemoteProperty
	private boolean quantize;

	@Override
	public void validate(DwrResponseI18n response) {
		super.validate(response);
	}

	public int getUpdatePeriodType() {
		return updatePeriodType;
	}

	public void setUpdatePeriodType(int updatePeriodType) {
		this.updatePeriodType = updatePeriodType;
	}

	public int getUpdatePeriods() {
		return updatePeriods;
	}

	public void setUpdatePeriods(int updatePeriods) {
		this.updatePeriods = updatePeriods;
	}

	public String getCommPortId() {
		return commPortId;
	}

	public void setCommPortId(String commPortId) {
		this.commPortId = commPortId;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public int getDataBits() {
		return dataBits;
	}

	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}

	public int getStopBits() {
		return stopBits;
	}

	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}

	public int getParity() {
		return parity;
	}

	public void setParity(int parity) {
		this.parity = parity;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public int getStopMode() {
		return stopMode;
	}

	public void setStopMode(int stopMode) {
		this.stopMode = stopMode;
	}

	public int getnChar() {
		return nChar;
	}

	public void setnChar(int nChar) {
		this.nChar = nChar;
	}

	public void setCharStopMode(int charStopMode) {
		this.charStopMode = charStopMode;
	}

	public int getCharStopMode() {
		return charStopMode;
	}

	public String getCharX() {
		return charX;
	}

	public void setCharX(String charX) {
		this.charX = charX;
	}

	public void setHexValue(String hexValue) {
		this.hexValue = hexValue;
	}

	public String getHexValue() {
		return hexValue;
	}

	public void setStopTimeout(int stopTimeout) {
		this.stopTimeout = stopTimeout;
	}

	public int getStopTimeout() {
		return stopTimeout;
	}

	public String getInitString() {
		return initString;
	}

	public void setInitString(String initString) {
		this.initString = initString;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public boolean isQuantize() {
		return quantize;
	}

	public void setQuantize(boolean quantize) {
		this.quantize = quantize;
	}

	private static final long serialVersionUID = -1;
	private static final int version = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(version);
		out.writeInt(updatePeriodType);
		out.writeInt(updatePeriods);
		SerializationHelper.writeSafeUTF(out, commPortId);
		out.writeInt(baudRate);
		out.writeInt(stopBits);
		out.writeInt(dataBits);
		out.writeInt(parity);
		out.writeInt(timeout);
		out.writeInt(retries);
		out.writeInt(stopMode);
		out.writeInt(nChar);
		out.writeInt(charStopMode);
		SerializationHelper.writeSafeUTF(out, charX);
		SerializationHelper.writeSafeUTF(out, hexValue);
		out.writeInt(stopTimeout);
		out.writeInt(bufferSize);
		out.writeBoolean(quantize);

	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int ver = in.readInt();
		if (ver == 1) {
			updatePeriodType = in.readInt();
			updatePeriods = in.readInt();
			commPortId = SerializationHelper.readSafeUTF(in);
			baudRate = in.readInt();
			stopBits = in.readInt();
			dataBits = in.readInt();
			parity = in.readInt();
			timeout = in.readInt();
			retries = in.readInt();
			stopMode = in.readInt();
			nChar = in.readInt();
			charStopMode = in.readInt();
			charX = SerializationHelper.readSafeUTF(in);
			hexValue = SerializationHelper.readSafeUTF(in);
			stopTimeout = in.readInt();
			bufferSize = in.readInt();
			quantize = in.readBoolean();
		}
	}

	@Override
	public void jsonDeserialize(JsonReader reader, JsonObject json)
			throws JsonException {
		super.jsonDeserialize(reader, json);
		Integer value = deserializeUpdatePeriodType(json);
		if (value != null)
			updatePeriodType = value;
	}

	@Override
	public void jsonSerialize(Map<String, Object> map) {
		super.jsonSerialize(map);
		serializeUpdatePeriodType(map, updatePeriodType);
	}

}
