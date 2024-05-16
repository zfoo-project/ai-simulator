package com.zfoo.ai.simulator;

import com.zfoo.ai.simulator.model.VersionConfig;
import com.zfoo.net.util.security.ZipUtils;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author godotg
 */
@Slf4j
@Ignore
public class ZipTest {

    @Test
    public void zip_exe() {
        var path = new File(FileUtils.getProAbsPath()).getParent();
        var projectFile = new File(path);
        var sourceFilePaths = new ArrayList<String>();
        var ignores = List.of(".idea", ".git", ".gitignore", "package-lock.json"
                , "simulator", "ai-simulator", "userData", "log");
        for (var file : projectFile.listFiles()) {
            var name = file.getName();
            if (ignores.contains(name)) {
                continue;
            }
            if (name.endsWith(".zip")) {
                continue;
            }
            sourceFilePaths.add(file.getAbsolutePath());
        }
        // exe重命名
        var exeRenamePath = StringUtils.format("{}/simulator/target/ai-simulator.exe", path);
        var exeRenameFile = new File(exeRenamePath);
        sourceFilePaths.add(exeRenamePath);

        var exeFile = new File(StringUtils.format("{}/simulator/target/simulator.exe", path));
        if (exeFile.exists()) {
            exeFile.renameTo(exeRenameFile);
        }
        if (!exeRenameFile.exists()) {
            throw new RuntimeException(StringUtils.format("[{}]文件不存在", exeRenamePath));
        }

        var out = StringUtils.format("{}/ai-simulator.zip", path);
        ZipUtils.zip(ArrayUtils.listToArray(sourceFilePaths, String.class), out);
    }

    @Test
    public void zip_update() {
        var path = new File(FileUtils.getProAbsPath()).getParent();
        var sourceFilePaths = new ArrayList<String>();
        sourceFilePaths.add(StringUtils.format("{}/browser", path));
        sourceFilePaths.add(StringUtils.format("{}/README.md", path));

        var versionJson = FileUtils.readFileToString(new File(StringUtils.format("{}/version.json", path)));
        var versionConfig = JsonUtils.string2Object(versionJson, VersionConfig.class);

        var updateZip = StringUtils.format("update-{}.zip", versionConfig.getVersion());
        var out = StringUtils.format("{}/{}", path, updateZip);
        log.info("update zip file [{}]", updateZip);

        ZipUtils.zip(ArrayUtils.listToArray(sourceFilePaths, String.class), out);
    }

}
