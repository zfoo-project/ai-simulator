package com.zfoo.ai.simulator.util;

import com.zfoo.protocol.util.StringUtils;

/**
 * @author godotg
 */
public abstract class EnvUtils {

    public static boolean isDevelopment() {
        return env().equals("dev");
    }

    public static String env() {
        var profile = System.getProperty("spring.profiles.active");
        return StringUtils.trim(profile);
    }

}
