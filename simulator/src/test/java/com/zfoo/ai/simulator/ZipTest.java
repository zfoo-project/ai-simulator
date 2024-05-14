package com.zfoo.ai.simulator;

import com.zfoo.net.util.security.ZipUtils;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

/**
 * @author godotg
 */
@Ignore
public class ZipTest {

    @Test
    public void test() {
        var path = new File(FileUtils.getProAbsPath()).getParent();
        var projectFile = new File(path);
        var sourceFilePaths = new ArrayList<String>();
        for (var file : projectFile.listFiles()) {
            var name = file.getName();
            if (name.equals("websocket.mjs") || name.equals("simulator.mjs")) {
                continue;
            }
            if (name.endsWith(".mjs")) {
                sourceFilePaths.add(file.getAbsolutePath());
            }
        }
        var out = StringUtils.format("{}/ai-simulator.zip", path);
        ZipUtils.zip(ArrayUtils.listToArray(sourceFilePaths, String.class), out);
    }

}
