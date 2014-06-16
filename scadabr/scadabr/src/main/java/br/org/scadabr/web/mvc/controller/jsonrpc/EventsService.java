package br.org.scadabr.web.mvc.controller.jsonrpc;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import java.util.Collection;

@JsonRpcService("/jsonrpc/events.json")
public interface EventsService {
    Collection<JsonEventInstance> acknowledgePendingEvent(@JsonRpcParam("eventId")int eventid);
    Collection<JsonEventInstance> acknowledgeAllPendingEvents();
}