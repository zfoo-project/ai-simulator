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

package com.zfoo.ai.simulator.controller;

import com.zfoo.ai.simulator.packet.SimulatorChatAnswer;
import com.zfoo.ai.simulator.packet.SimulatorRegisterAnswer;
import com.zfoo.ai.simulator.packet.SimulatorRegisterAsk;
import com.zfoo.ai.simulator.packet.SimulatorStatusAsk;
import com.zfoo.ai.simulator.service.ChatBotService;
import com.zfoo.ai.simulator.service.SimulatorService;
import com.zfoo.event.anno.EventReceiver;
import com.zfoo.net.NetContext;
import com.zfoo.net.anno.PacketReceiver;
import com.zfoo.net.anno.Task;
import com.zfoo.net.core.event.ServerSessionInactiveEvent;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.collection.concurrent.ConcurrentHashSet;
import com.zfoo.protocol.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author godotg
 */
@Slf4j
@Component
public class SimulatorController {

    @Autowired
    private ChatBotService chatBotService;
    @Autowired
    private SimulatorService simulatorService;

    @PacketReceiver(Task.NettyIO)
    public void atSimulatorStatusAsk(Session session, SimulatorStatusAsk ask) {
        var sid = session.getSid();
        var simulator = simulatorService.simulator(sid);
        var message = ask.getMessage();
        log.info("atSimulatorStatusAsk sid:[{}] simulator:[{}] message:[{}]", sid, simulator, message);
        chatBotService.sendToChatBot(simulator, message);
    }

    @PacketReceiver(Task.NettyIO)
    public void atSimulatorRegisterAsk(Session session, SimulatorRegisterAsk ask) {
        var sid = session.getSid();
        var simulator = ask.getSimulator();
        log.info("atSimulatorRegisterAsk [sid:{}] simulator:[{}] 注册成功", sid, simulator);

        var simulatorSessionIds = simulatorService.simulatorSessionMap.computeIfAbsent(simulator, it -> new ConcurrentHashSet<>());
        simulatorSessionIds.add(sid);
        NetContext.getRouter().send(session, new SimulatorRegisterAnswer());
        chatBotService.sendToChatBot(simulator, StringUtils.format("模拟器 {} 注册成功，成功连接服务器 sid:[{}]", simulator, sid));
    }

    @PacketReceiver
    public void atSimulatorChatAnswer(Session session, SimulatorChatAnswer answer) {
        var requestId = answer.getRequestId();
        var simulator = answer.getSimulator();
        var markdown = answer.getMarkdown();
        // 只打印最后一句话
        var logMarkdown = substringAfterLastRegex(markdown, "[，,\\s]+");
        var simulatorRequestId = Long.parseLong(StringUtils.format("{}{}", requestId, chatBotService.simulatorId(simulator)));
        log.info("atSimulatorChatAnswer requestId:[{}] simulator:[{}] simulatorRequestId:[{}] markdown:[{}]", requestId, simulator, simulatorRequestId, logMarkdown);
        chatBotService.sendToChatBot(requestId, simulator, markdown);
    }

    public static String substringAfterLastRegex(final String str, final String regex) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        if (StringUtils.isEmpty(regex)) {
            return StringUtils.EMPTY;
        }
        var splits = str.split(regex);
        if (ArrayUtils.isEmpty(splits)) {
            return str;
        }
        return splits[splits.length - 1];
    }

    @EventReceiver
    public void onServerSessionInactiveEvent(ServerSessionInactiveEvent event) {
        for (var sessionIds : simulatorService.simulatorSessionMap.values()) {
            sessionIds.remove(event.getSession().getSid());
        }
    }
}
