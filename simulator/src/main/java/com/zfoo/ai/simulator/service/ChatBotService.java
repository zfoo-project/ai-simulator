/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.ai.simulator.service;

import com.zfoo.net.NetContext;
import com.zfoo.protocol.collection.concurrent.ConcurrentHashSet;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.ai.simulator.packet.ChatBotNotice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author godotg
 */
@Slf4j
@Component
public class ChatBotService {

    public ConcurrentHashSet<Long> chatBotSessions = new ConcurrentHashSet<>();

    public void sendToChatBot(long requestId, int simulator, String message) {
        var simulatorRequestId = Long.parseLong(StringUtils.format("{}{}", simulator, requestId));
        var notice = new ChatBotNotice(simulatorRequestId, simulator, message);
        for (var sid : chatBotSessions) {
            var session = NetContext.getSessionManager().getServerSession(sid);
            NetContext.getRouter().send(session, notice);
        }
    }

}
