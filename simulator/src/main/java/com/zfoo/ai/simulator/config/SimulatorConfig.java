package com.zfoo.ai.simulator.config;

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

    private String updateUrl;

    private boolean headless;

    private List<String> simulators;

}
