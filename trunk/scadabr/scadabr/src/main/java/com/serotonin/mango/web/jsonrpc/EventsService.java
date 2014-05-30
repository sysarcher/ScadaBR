package com.serotonin.mango.web.jsonrpc;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import java.util.Collection;

@JsonRpcService("/jsonrpc/events.json")
public interface EventsService {
    Collection<JsonEventInstance> acknowledgePendingEvent(@JsonRpcParam("eventId")int eventid);
    Collection<JsonEventInstance> acknowledgeAllPendingEvents();
}