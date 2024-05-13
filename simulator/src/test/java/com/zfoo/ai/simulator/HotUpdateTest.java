package com.zfoo.ai.simulator;

import com.zfoo.net.util.security.ZipUtils;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.util.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author godotg
 */
@Ignore
public class HotUpdateTest {

    @Test
    public void test() {
        var sourceDirectory = "D:/Project/ai-simulator";
        var sources = new String[]{"alibaba.mjs", "baidu.mjs", "google.mjs", "openai.mjs", "tencent.mjs"};
        var sourceList = Arrays.stream(sources)
                .map(it -> StringUtils.format("{}/{}", sourceDirectory, it))
                .toList();
        var sourceFilePaths = ArrayUtils.listToArray(sourceList, String.class);

        ZipUtils.zip(sourceFilePaths, "ai-simulator.zip");
    }

}
