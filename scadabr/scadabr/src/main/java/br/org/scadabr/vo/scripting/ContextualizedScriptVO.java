package br.org.scadabr.vo.scripting;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.org.scadabr.rt.scripting.ContextualizedScriptRT;
import br.org.scadabr.rt.scripting.ScriptRT;
import br.org.scadabr.rt.scripting.context.ScriptContextObject;

import com.serotonin.json.JsonArray;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonValue;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.web.i18n.LocalizableMessage;

@JsonRemoteEntity
public class ContextualizedScriptVO extends ScriptVO<ContextualizedScriptVO>
		implements ChangeComparable<ContextualizedScriptVO> {
	public static final Type TYPE = Type.CONTEXTUALIZED_SCRIPT;

	@Override
	public br.org.scadabr.vo.scripting.ScriptVO.Type getType() {
		return TYPE;
	}

	private Map<Integer, String> pointsOnContext = new HashMap<Integer, String>();
	private Map<Integer, String> objectsOnContext = new HashMap<Integer, String>();

	//
	// /
	// / Serialization
	// /
	//
	private static final long serialVersionUID = -1;
	private static final int version = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(version);
		AbstractPointLocatorVO.writeIntValuePairList(pointsOnContext, out);
		AbstractPointLocatorVO.writeIntValuePairList(objectsOnContext, out);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int ver = in.readInt();
		// Switch on the version of the class so that version changes can be
		if (ver == 1) {
			pointsOnContext = AbstractPointLocatorVO.readIntValuePairList(in);
			objectsOnContext = AbstractPointLocatorVO.readIntValuePairList(in);
		}
	}

	@Override
	public ScriptRT createScriptRT() {
		return new ContextualizedScriptRT(this);
	}

	public Map<Integer, String> getPointsOnContext() {
		return pointsOnContext;
	}

	public void setPointsOnContext(Map<Integer, String> pointsOnContext) {
		this.pointsOnContext = pointsOnContext;
	}

	public void setObjectsOnContext(Map<Integer, String> objectsOnContext) {
		this.objectsOnContext = objectsOnContext;
	}

	public Map<Integer, String> getObjectsOnContext() {
		return objectsOnContext;
	}

	@Override
	public String getTypeKey() {
		return "event.audit.scripts";
	}

	@Override
	public void addPropertyChanges(List<LocalizableMessage> list,
			ContextualizedScriptVO from) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addProperties(List<LocalizableMessage> list) {
		// TODO Auto-generated method stub

	}

	@Override
	public void jsonDeserialize(JsonReader reader, JsonObject json)
			throws JsonException {

		super.jsonDeserialize(reader, json);
		JsonArray jsonContext = json.getJsonArray("pointsOnContext");
		if (jsonContext != null) {
			pointsOnContext.clear();
			DataPointDao dataPointDao = new DataPointDao();

			for (JsonValue jv : jsonContext.getElements()) {
				JsonObject jo = jv.toJsonObject();
				String xid = jo.getString("dataPointXid");
				if (xid == null)
					throw new LocalizableJsonException(
							"emport.error.meta.missing", "dataPointXid");

				DataPointVO dp = dataPointDao.getDataPoint(xid);
				if (dp == null)
					throw new LocalizableJsonException(
							"emport.error.missingPoint", xid);

				String var = jo.getString("varName");
				if (var == null)
					throw new LocalizableJsonException(
							"emport.error.meta.missing", "varName");

				pointsOnContext.put(dp.getId(), var);
			}
		}

		jsonContext = json.getJsonArray("objectsOnContext");
		if (jsonContext != null) {
			objectsOnContext.clear();

			for (JsonValue jv : jsonContext.getElements()) {
				JsonObject jo = jv.toJsonObject();
				int key = jo.getInt("objectId");

				ScriptContextObject.Type objectType = ScriptContextObject.Type
						.valueOf(key);

				if (objectType == null)
					throw new LocalizableJsonException(
							"emport.error.missingPoint", key);

				String var = jo.getString("varName");
				if (var == null)
					throw new LocalizableJsonException(
							"emport.error.meta.missing", "varName");

				objectsOnContext.put(key, var);
			}
		}

	}

	@Override
	public void jsonSerialize(Map<String, Object> map) {
		super.jsonSerialize(map);
		List<Map<String, Object>> pointList = new ArrayList<Map<String, Object>>();
		for (Integer pointId : pointsOnContext.keySet()) {
			DataPointVO dp = new DataPointDao().getDataPoint(pointId);
			if (dp != null) {
				Map<String, Object> point = new HashMap<String, Object>();
				pointList.add(point);
				point.put("varName", pointsOnContext.get(pointId));
				point.put("dataPointXid", dp.getXid());
			}
		}
		map.put("pointsOnContext", pointList);

		List<Map<String, Object>> objectsList = new ArrayList<Map<String, Object>>();
		for (Integer pointId : objectsOnContext.keySet()) {
			Map<String, Object> point = new HashMap<String, Object>();
			objectsList.add(point);
			point.put("varName", objectsOnContext.get(pointId));
			point.put("objectId", pointId);
		}

		map.put("objectsOnContext", objectsList);

	}

}
