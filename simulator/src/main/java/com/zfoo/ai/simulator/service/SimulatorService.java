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
import com.zfoo.ai.simulator.model.VersionConfig;
import com.zfoo.ai.simulator.packet.SimulatorChatAsk;
import com.zfoo.ai.simulator.util.CommandUtils;
import com.zfoo.ai.simulator.util.EnvUtils;
import com.zfoo.ai.simulator.util.HttpUtils;
import com.zfoo.event.model.AppStartEvent;
import com.zfoo.net.NetContext;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.websocket.WebsocketServer;
import com.zfoo.net.util.security.ZipUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.collection.concurrent.ConcurrentHashSet;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.manager.SchedulerBus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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

    public String readme = "欢迎使用AI模拟器！";

    @SneakyThrows
    @Override
    public void onApplicationEvent(AppStartEvent event) {
        // 非开发环境，优先使用外部的配置文件
        if (!EnvUtils.isDevelopment()) {
            var configFile = new File("ai-config.yaml");
            if (configFile.exists()) {
                var yaml = new Yaml();
                var customConfig = yaml.loadAs(FileUtils.readFileToString(configFile), SimulatorConfig.class);
                simulatorConfig.updateConfig(customConfig);
            }
        }

        // 更新热更文件
        updateVersion();
        loadReadme();

        // 启动websocket服务器
        var port = simulatorConfig.getPort();
        var brokerServer = new WebsocketServer(HostAndPort.valueOf("0.0.0.0", port));
        brokerServer.start();
        log.info("Tomcat start at [http://localhost:17333]");

        // 打开默认地址
        // start chrome.exe https://www.baidu.com
        if (!EnvUtils.isDevelopment()) {
            CommandUtils.execCommand("cmd /c start http://localhost:17333");
        }

        // 启动ai 模拟器
        for (var simulator : simulatorConfig.getSimulators()) {
            createSimulator(simulator);
        }
    }

    private void updateVersion() {
        // 本地配置
        var localVersionFile = new File("version.json");
        VersionConfig localVersionConfig = new VersionConfig();
        if (localVersionFile.exists()) {
            var localVersionConfigJson = FileUtils.readFileToString(localVersionFile);
            localVersionConfig = JsonUtils.string2Object(localVersionConfigJson, VersionConfig.class);
        }

        try {
            var versionUrl = simulatorConfig.getVersionUrl();
            if (StringUtils.isBlank(versionUrl)) {
                return;
            }
            log.info("获取版本地址[{}]", versionUrl);
            var remoteVersionJson = HttpUtils.get(versionUrl);
            log.info("remoteVersion:[{}]", remoteVersionJson);
            var remoteVersionConfig = JsonUtils.string2Object(remoteVersionJson, VersionConfig.class);

            if (remoteVersionConfig.getVersion().equals(localVersionConfig.getVersion())) {
                log.info("版本相同无须更新");
                return;
            }

            // 下载并解压热更新文件
            var bytes = HttpUtils.getBytes(remoteVersionConfig.getUpdateUrl());
            ZipUtils.unzip(new ByteArrayInputStream(bytes), "./");

            // 解压完成后覆盖本地的version.json文件
            FileUtils.writeStringToFile(localVersionFile, remoteVersionJson, false);
        } catch (Exception e) {
            log.info("update version exception", e);
        }
    }

    private void loadReadme() {
        var readmeFile = new File("README.md");
        if (readmeFile.exists()) {
            readme = FileUtils.readFileToString(readmeFile);
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
                var command = StringUtils.format("{} {}/browser/{}.mjs {} {}", nodePath, workingPath, simulator, chromePath, headless);
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
            var sessionIds = entry.getValue();
            for (var sid : sessionIds) {
                if (sid == sidOfSimulator) {
                    return simulator;
                }
            }
        }
        return null;
    }
}
