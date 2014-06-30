package br.org.scadabr.web.mvc.controller.jsonrpc;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

/**
 *
 * @author aploese
 */
@JsonRpcService("/rpc/watchlists.json")
public interface WatchListService {
    JsonWatchList getWatchList(@JsonRpcParam("watchlistId")int watchlistId);
    JsonWatchList getSelectedWatchlist();
    JsonWatchList addPointToWatchlist(@JsonRpcParam("watchlistId")int watchlistId, @JsonRpcParam("index")int index, @JsonRpcParam("dataPointId")int dataPointId);
}
