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

import com.zfoo.ai.simulator.config.SimulatorConfig;
import com.zfoo.ai.simulator.util.CommandUtils;
import com.zfoo.ai.simulator.util.EnvUtils;
import com.zfoo.event.model.AppStartEvent;
import com.zfoo.net.NetContext;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.websocket.WebsocketServer;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.collection.concurrent.ConcurrentHashSet;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.manager.SchedulerBus;
import com.zfoo.ai.simulator.packet.SimulatorChatAsk;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author godotg
 */
@Slf4j
@Component
public class SimulatorService implements ApplicationListener<AppStartEvent> {

    @Autowired
    private SimulatorConfig simulatorConfig;


    // key:simulatorType -> value:sessions
    public ConcurrentHashMap<String, ConcurrentHashSet<Long>> simulatorSessionMap = new ConcurrentHashMap<>();

    @Autowired
    private ChatBotService chatBotService;

    @SneakyThrows
    @Override
    public void onApplicationEvent(AppStartEvent event) {
        var port = simulatorConfig.getPort();
        var brokerServer = new WebsocketServer(HostAndPort.valueOf("0.0.0.0", port));
        brokerServer.start();

        // 非开发环境，优先使用外部的配置文件
        if (!EnvUtils.isDevelopment()) {
            var configFile = new File("config.yaml");
            if (configFile.exists()) {
                var yaml = new Yaml();
                var customConfig = yaml.loadAs(FileUtils.readFileToString(configFile), SimulatorConfig.class);
                simulatorConfig.setSimulators(customConfig.getSimulators());
                simulatorConfig.setHeadless(customConfig.isHeadless());
            }
        }

        // 启动ai 模拟器
        for (var simulator : simulatorConfig.getSimulators()) {
            createSimulator(simulator);
        }
    }

    public void createSimulator(String simulator) {
        var nodePath = simulatorConfig.getNodePath();
        var workingPath = simulatorConfig.getWorkingPath();
        var chromePath = simulatorConfig.getChromePath();
        var headless = simulatorConfig.isHeadless();
        var thread = new Thread(new Runnable() {
            @Override
            public void run() {
                var command = StringUtils.format("{} {}/{}.mjs {} {}", nodePath, workingPath, simulator, chromePath, headless);
                CommandUtils.execCommand(command, workingPath);
                SchedulerBus.schedule(() -> createSimulator(simulator), 5, TimeUnit.SECONDS);
            }
        });
        thread.start();
    }


    public boolean existSimulator(int simulator) {
        var simulatorSessions = simulatorSessionMap.get(simulator);
        return CollectionUtils.isNotEmpty(simulatorSessions);
    }

    public void sendToSimulators(long requestId, String message) {
        var ask = new SimulatorChatAsk(requestId, message);
        for (var sessionIds : simulatorSessionMap.values()) {
            for (var sid : sessionIds) {
                var session = NetContext.getSessionManager().getServerSession(sid);
                NetContext.getRouter().send(session, ask);
            }
        }
    }

    public String simulator(long sidOfSimulator) {
        for (var entry : simulatorSessionMap.entrySet()) {
            var simulator = entry.getKey();
            var sessionIds= entry.getValue();
            for (var sid : sessionIds) {
                if (sid == sidOfSimulator) {
                    return simulator;
                }
            }
        }
        return null;
    }
}
