package br.org.scadabr.vo.dataSource.opc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import br.org.scadabr.rt.dataSource.opc.OPCPointLocatorRT;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.json.JsonSerializable;
import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

@JsonRemoteEntity
public class OPCPointLocatorVO extends AbstractPointLocatorVO implements
		JsonSerializable {

	@Override
	public PointLocatorRT createRuntime() {
		return new OPCPointLocatorRT(this);
	}

	@Override
	public LocalizableMessage getConfigurationDescription() {
		return null;
	}

	@Override
	public MangoDataType getMangoDataType() {
		return mangoDataType;
	}

	public void setMangoDataType(MangoDataType mangoDataType) {
		this.mangoDataType = mangoDataType;
	}

	@Override
	public boolean isSettable() {
		return settable;
	}

	public void setSettable(boolean settable) {
		this.settable = settable;
	}

	@JsonRemoteProperty
	private String tag = "";
        @JsonRemoteProperty(alias=MangoDataType.ALIAS_DATA_TYPE)
	private MangoDataType mangoDataType = MangoDataType.BINARY;
	@JsonRemoteProperty
	private boolean settable;

	@Override
	public void validate(DwrResponseI18n response) {

	}

	@Override
	public void addProperties(List<LocalizableMessage> list) {

	}

	@Override
	public void addPropertyChanges(List<LocalizableMessage> list, Object o) {

	}

	private static final long serialVersionUID = -1;
	private static final int version = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(version);
		SerializationHelper.writeSafeUTF(out, tag);
		out.writeInt(mangoDataType.mangoId);
		out.writeBoolean(settable);

	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int ver = in.readInt();
		if (ver == 1) {
			tag = SerializationHelper.readSafeUTF(in);
			mangoDataType = MangoDataType.fromMangoId(in.readInt());
			settable = in.readBoolean();
		}
	}

	@Override
	public void jsonDeserialize(JsonReader arg0, JsonObject arg1)
			throws JsonException {

	}

	@Override
	public void jsonSerialize(Map<String, Object> arg0) {
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}
