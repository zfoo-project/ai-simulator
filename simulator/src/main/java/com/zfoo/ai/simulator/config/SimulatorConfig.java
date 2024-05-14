package com.zfoo.ai.simulator.config;

import com.zfoo.net.util.NetUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.StringUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author godotg
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "simulator")
public class SimulatorConfig {

    private int port;

    private String nodePath;

    private String chromePath;

    private String workingPath;

    private String versionUrl;

    private boolean headless;

    private List<String> simulators;

    public void updateConfig(SimulatorConfig newConfig) {
        if (NetUtils.isAvailablePort(newConfig.port)) {
            this.port = newConfig.port;
        }
        if (StringUtils.isNotEmpty(newConfig.nodePath)) {
            this.nodePath = newConfig.nodePath;
        }
        if (StringUtils.isNotEmpty(newConfig.chromePath)) {
            this.chromePath = newConfig.chromePath;
        }
        if (StringUtils.isNotEmpty(newConfig.workingPath)) {
            this.workingPath = newConfig.workingPath;
        }
        if (StringUtils.isNotEmpty(newConfig.versionUrl)) {
            this.versionUrl = newConfig.versionUrl;
        }
        if (newConfig.headless) {
            this.headless = newConfig.headless;
        }
        if (CollectionUtils.isNotEmpty(newConfig.simulators)) {
            this.simulators = newConfig.simulators;
        }
    }

}
