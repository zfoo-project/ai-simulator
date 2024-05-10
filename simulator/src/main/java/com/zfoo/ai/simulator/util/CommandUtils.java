package com.zfoo.ai.simulator.util;

import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;

/**
 * @author godotg
 */
@Slf4j
public class CommandUtils {

    public static String execCommand(String command) {
        log.info("execCommand [{}]", command);
        return doExecCommand(command, (File)null);
    }

    public static String execCommand(String command, String workingDirectory) {
        log.info("execCommand [{}] workingDirectory:[{}]", command, workingDirectory);
        FileUtils.createDirectory(workingDirectory);
        File wd = new File(workingDirectory);
        return doExecCommand(command, wd);
    }

    public static String doExecCommand(String command, File wd) {
        Process process = null;
        InputStream inputStream = null;

        String var8;
        try {
            String[] commandSplits = command.split("\\s+");
            process = (new ProcessBuilder(commandSplits)).redirectErrorStream(true).directory(wd).start();
            inputStream = process.getInputStream();
            byte[] bytes = IOUtils.toByteArray(inputStream);
            String result = StringUtils.bytesToString(bytes);
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                throw new RunException("error executing command exitValue:[{}] result:[{}]", new Object[]{exitValue, result});
            }

            var8 = result;
        } catch (Exception var12) {
            log.error("unknown exception in command execution", var12);
            return "";
        } finally {
            if (process != null) {
                process.destroy();
            }

            IOUtils.closeIO(new Closeable[]{inputStream});
        }

        return var8;
    }


}
