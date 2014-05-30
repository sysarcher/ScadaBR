/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.serotonin.mango.web.jsonrpc;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import com.serotonin.mango.vo.User;

/**
 *
 * @author aploese
 */
@JsonRpcService("/rpc/watchlist.json")
public class WatchListJsonRpcController {
    
    User getCurrentUser() {
        return new User();
    }
    User createUser(@JsonRpcParam("username") String userName, @JsonRpcParam("thePassword") String password) {
        return new User();
    }
}
