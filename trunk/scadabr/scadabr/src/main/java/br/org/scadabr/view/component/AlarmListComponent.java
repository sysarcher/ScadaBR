package br.org.scadabr.view.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.mango.Common;
import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.view.ImplDefinition;
import com.serotonin.mango.web.dwr.BaseDwr;
import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;

@JsonRemoteEntity
public class AlarmListComponent extends CustomComponent {

	public static ImplDefinition DEFINITION = new ImplDefinition("alarmlist",
			"ALARMLIST", "graphic.alarmlist", EnumSet.noneOf(MangoDataType.class));

	@JsonRemoteProperty
	private AlarmLevels minAlarmLevel = AlarmLevels.INFORMATION;
	@JsonRemoteProperty
	private int maxListSize = 5;
	@JsonRemoteProperty
	private int width = 500;
    @Autowired
    private EventDao eventDao;
    @Autowired
    private Common common;

	private boolean hideIdColumn = true;
	private boolean hideAlarmLevelColumn = false;
	private boolean hideTimestampColumn = false;
	private boolean hideInactivityColumn = true;
	private boolean hideAckColumn = false;

	@Override
	public String generateContent() {
		Map<String, Object> model = new HashMap<String, Object>();
		WebContext webContext = WebContextFactory.get();
		HttpServletRequest request = webContext.getHttpServletRequest();
		List<EventInstance> events = eventDao.getPendingEvents(common
				.getUser().getId());

		filter(events, minAlarmLevel);

		int max = events.size() > maxListSize ? maxListSize : events.size();

		model.put("nome", "marlon");
		model.put("events", events.subList(0, max));
		model.put("width", width > 0 ? width : 500);
		model.put("hideIdColumn", hideIdColumn);
		model.put("hideAlarmLevelColumn", hideAlarmLevelColumn);
		model.put("hideTimestampColumn", hideTimestampColumn);
		model.put("hideInactivityColumn", hideInactivityColumn);
		model.put("hideAckColumn", hideAckColumn);

		String content = BaseDwr.generateContent(request, "alarmList.jsp",
				model);
		return content;
	}

	private void filter(List<EventInstance> list, AlarmLevels alarmLevel) {

		switch (alarmLevel) {case 
                        URGENT:
			removeAlarmLevel(list, AlarmLevels.INFORMATION);
		break;
                case CRITICAL:
			removeAlarmLevel(list, AlarmLevels.INFORMATION);
			removeAlarmLevel(list, AlarmLevels.URGENT);
		break;
                case LIFE_SAFETY:
			removeAlarmLevel(list, AlarmLevels.INFORMATION);
			removeAlarmLevel(list, AlarmLevels.URGENT);
			removeAlarmLevel(list, AlarmLevels.CRITICAL);
                    break;
                default:
		}
	}

	private void removeAlarmLevel(List<EventInstance> source, AlarmLevels alarmLevel) {
		List<EventInstance> copy = new ArrayList<EventInstance>();

		for (EventInstance eventInstance : source) {
			if (eventInstance.getAlarmLevel() == alarmLevel)
				copy.add(eventInstance);
		}

		source.removeAll(copy);

	}

	@Override
	public boolean containsValidVisibleDataPoint(int dataPointId) {
		return false;
	}

	@Override
	public ImplDefinition definition() {
		return DEFINITION;
	}

	@Override
	public String generateInfoContent() {
		return "<b> info content</b>";
	}

	public int getMaxListSize() {
		return maxListSize;
	}

	public void setMaxListSize(int maxListSize) {
		this.maxListSize = maxListSize;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isHideIdColumn() {
		return hideIdColumn;
	}

	public void setHideIdColumn(boolean hideIdColumn) {
		this.hideIdColumn = hideIdColumn;
	}

	public boolean isHideTimestampColumn() {
		return hideTimestampColumn;
	}

	public void setHideTimestampColumn(boolean hideTimestampColumn) {
		this.hideTimestampColumn = hideTimestampColumn;
	}

	public boolean isHideAlarmLevelColumn() {
		return hideAlarmLevelColumn;
	}

	public void setHideAlarmLevelColumn(boolean hideAlarmLevelColumn) {
		this.hideAlarmLevelColumn = hideAlarmLevelColumn;
	}

	public boolean isHideInactivityColumn() {
		return hideInactivityColumn;
	}

	public void setHideInactivityColumn(boolean hideInactivityColumn) {
		this.hideInactivityColumn = hideInactivityColumn;
	}

	private static final long serialVersionUID = -1;
	private static final int version = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(version);
		out.writeInt(minAlarmLevel.mangoId);
		out.writeInt(maxListSize);
		out.writeInt(width);
		out.writeBoolean(hideIdColumn);
		out.writeBoolean(hideAlarmLevelColumn);
		out.writeBoolean(hideTimestampColumn);
		out.writeBoolean(hideInactivityColumn);
		out.writeBoolean(hideAckColumn);

	}

	private void readObject(ObjectInputStream in) throws IOException {
		int ver = in.readInt();
		// Switch on the version of the class so that version changes can be
		// elegantly handled.
		if (ver == 1) {
			minAlarmLevel = AlarmLevels.fromMangoId(in.readInt());
			maxListSize = in.readInt();
			width = in.readInt();
			hideIdColumn = in.readBoolean();
			hideAlarmLevelColumn = in.readBoolean();
			hideTimestampColumn = in.readBoolean();
			hideInactivityColumn = in.readBoolean();
			hideAckColumn = in.readBoolean();
		}

	}

	public void setHideAckColumn(boolean hideAckColumn) {
		this.hideAckColumn = hideAckColumn;
	}

	public boolean isHideAckColumn() {
		return hideAckColumn;
	}

	public void setMinAlarmLevel(AlarmLevels minAlarmLevel) {
		this.minAlarmLevel = minAlarmLevel;
	}

	public AlarmLevels getMinAlarmLevel() {
		return minAlarmLevel;
	}

}
