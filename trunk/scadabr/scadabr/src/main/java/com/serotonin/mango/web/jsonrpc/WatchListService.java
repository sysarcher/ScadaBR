/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.serotonin.mango.web.jsonrpc;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

/**
 *
 * @author aploese
 */
@JsonRpcService("/rpc/watchlists.json")
public interface WatchListService {
    JsonWatchList getWatchList(@JsonRpcParam("watchlistId")int watchlistId);
}
