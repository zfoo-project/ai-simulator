package com.zfoo.ai.simulator;

import com.zfoo.ai.simulator.model.VersionConfig;
import com.zfoo.protocol.util.JsonUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author godotg
 */
@Ignore
public class VersionTest {

    @Test
    public void test() {
        var versionConfig = new VersionConfig();
        versionConfig.setVersion("1.0.0");
        versionConfig.setUpdateUrl("http://simulator.godot.fun/simulator-1.0.0.rar");
        System.out.println(JsonUtils.object2String(versionConfig));
    }

}
