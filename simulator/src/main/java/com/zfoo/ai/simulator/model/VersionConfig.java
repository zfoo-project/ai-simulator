package com.zfoo.ai.simulator.model;

import com.zfoo.protocol.util.StringUtils;
import lombok.Data;

/**
 * @author godotg
 */
@Data
public class VersionConfig {

    private String simulatorVersion = StringUtils.EMPTY;
    private String simulatorResourceUrl = StringUtils.EMPTY;

}
