package com.zfoo.ai.simulator.packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author godotg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatBotNotice {

    private long requestId;

    // ChatAIEnum
    private int simulator;

    private String choice;

}
