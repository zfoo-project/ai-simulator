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
        return doExecCommand(command, null);
    }


    public static String execCommand(String command, String workingDirectory) {
        log.info("execCommand [{}] workingDirectory:[{}]", command, workingDirectory);
        FileUtils.createDirectory(workingDirectory);
        var wd = new File(workingDirectory);
        return doExecCommand(command, wd);
    }

    public static String doExecCommand(String command, File wd) {
        Process process = null;
        InputStream inputStream = null;
        try {
            var commandSplits = command.split(StringUtils.SPACE_REGEX);
            process = new ProcessBuilder(commandSplits)
                    .redirectErrorStream(true)
                    .directory(wd)
                    .start();

            //取得命令结果的输出流
            inputStream = process.getInputStream();
            var bytes = IOUtils.toByteArray(inputStream);
            var result = StringUtils.bytesToString(bytes);

            // 其他线程都等待这个线程完成
            process.waitFor();
            // 获取javac线程的退出值，0代表正常退出，非0代表异常中止
            int exitValue = process.exitValue();

            // 返回编译是否成功
            if (exitValue != 0) {
                throw new RunException("error executing command exitValue:[{}] result:[{}]", exitValue, result);
            }

            return result;
        } catch (Exception e) {
            log.error("----------------------------------------------------------------------------------------------------------");
            log.error("unknown exception in command execution", e);
            log.error("----------------------------------------------------------------------------------------------------------");
        } finally {
            if (process != null) {
                process.destroy();
            }
            IOUtils.closeIO(inputStream);
        }

        return StringUtils.EMPTY;
    }

}
