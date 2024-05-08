package com.zfoo.ai.simulator.packet;

import lombok.Data;

import java.util.List;

/**
 * @author godotg
 */
@Data
public class ChatBotRequest {

    private long requestId;

    private List<String> messages;

}
