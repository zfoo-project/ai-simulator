/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.ai.simulator.controller;

import com.zfoo.ai.simulator.packet.ClipboardUnlockAnswer;
import com.zfoo.ai.simulator.service.SimulatorService;
import com.zfoo.net.NetContext;
import com.zfoo.net.anno.PacketReceiver;
import com.zfoo.net.anno.Task;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.util.ThreadUtils;
import com.zfoo.scheduler.anno.Scheduler;
import com.zfoo.scheduler.util.TimeUtils;

import com.zfoo.ai.simulator.packet.ClipboardLockAnswer;
import com.zfoo.ai.simulator.packet.ClipboardLockAsk;
import com.zfoo.ai.simulator.packet.ClipboardUnlockAsk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author godotg
 */
@Slf4j
@Component
public class ClipboardController {

    @Autowired
    private SimulatorService simulatorService;

    private AtomicBoolean lock = new AtomicBoolean(false);
    private volatile long lockTime;
    private String simulator;

    @Scheduler(cron = "0/1 * * * * ?")
    public void cronUnlock() {
        if (!lock.get()) {
            return;
        }
        if (lockTime <= 0) {
            return;
        }
        if (TimeUtils.now() - lockTime >= 13 * TimeUtils.MILLIS_PER_SECOND) {
            lockTime = 0;
            lock.setRelease(false);
            log.error("simulator:[{}] timeout copy from clipboard", simulator);
        }
    }

    @PacketReceiver(Task.EventBus)
    public void atClipboardLockAsk(Session session, ClipboardLockAsk ask) {
        var sid = session.getSid();
        var simulatorOfSid = simulatorService.simulator(sid);
        log.info("atClipboardLockAsk sid:[{}] simulator:[{}]", sid, simulatorOfSid);

        if (simulatorOfSid.equals(simulator)) {
            lockTime = TimeUtils.now();
            NetContext.getRouter().send(session, new ClipboardLockAnswer());
            return;
        }

        while (lock.compareAndSet(false, true)) {
            ThreadUtils.sleep(300);
        }
        lockTime = TimeUtils.now();
        this.simulator = simulatorOfSid;

        NetContext.getRouter().send(session, new ClipboardLockAnswer());
    }

    @PacketReceiver(Task.EventBus)
    public void atClipboardUnlockAsk(Session session, ClipboardUnlockAsk ask) {
        var sid = session.getSid();
        var simulator = simulatorService.simulator(sid);
        log.info("atClipboardUnlockAsk sid:[{}] simulator:[{}]", sid, simulator);

        // 释放剪贴板的锁
        lockTime = 0;
        lock.setRelease(false);

        NetContext.getRouter().send(session, new ClipboardUnlockAnswer());
    }

}
