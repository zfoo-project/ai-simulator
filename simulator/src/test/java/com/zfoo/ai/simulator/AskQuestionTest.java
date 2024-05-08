package com.zfoo.ai.simulator;

import com.zfoo.ai.simulator.packet.ChatBotRequest;
import com.zfoo.net.NetContext;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.websocket.WebsocketClient;
import com.zfoo.net.util.NetUtils;
import com.zfoo.protocol.util.RandomUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * @author godotg
 */
@Ignore
public class AskQuestionTest {


    @Test
    public void ask() {
        var context = new ClassPathXmlApplicationContext("config.xml");

        var questions = List.of("你是？", "你叫什么名字", "你是谁开发的", "who are you", "what is your name");
        var question = RandomUtils.randomEle(questions);


        var webSocketClientProtocolConfig = WebSocketClientProtocolConfig.newBuilder()
                .webSocketUri("ws://127.0.0.1:17313/websocket")
                .build();

        var client = new WebsocketClient(HostAndPort.valueOf(StringUtils.format("{}:{}", NetUtils.getLocalhostStr(), 17313)), webSocketClientProtocolConfig);
        var session = client.start();

        ThreadUtils.sleep(1000);

        var request = new ChatBotRequest();
        request.setRequestId(RandomUtils.randomInt(0, Integer.MAX_VALUE));
        request.setMessages(List.of(question));
        NetContext.getRouter().send(session, request);

        ThreadUtils.sleep(1000);
    }

    @Test
    public void ask1() {
        var context = new ClassPathXmlApplicationContext("config.xml");

        var questions = List.of("1", "2", "3");
        var question = RandomUtils.randomEle(questions);


        var webSocketClientProtocolConfig = WebSocketClientProtocolConfig.newBuilder()
                .webSocketUri("ws://127.0.0.1:17313/websocket")
                .build();

        var client = new WebsocketClient(HostAndPort.valueOf(StringUtils.format("{}:{}", NetUtils.getLocalhostStr(), 17313)), webSocketClientProtocolConfig);
        var session = client.start();

        ThreadUtils.sleep(1000);

        var request = new ChatBotRequest();
        request.setRequestId(RandomUtils.randomInt(0, Integer.MAX_VALUE));
        request.setMessages(List.of(question));
        NetContext.getRouter().send(session, request);

        ThreadUtils.sleep(1000);
    }

}
