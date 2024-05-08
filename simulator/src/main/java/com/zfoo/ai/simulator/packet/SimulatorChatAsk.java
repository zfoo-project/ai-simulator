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
public class SimulatorChatAsk {

    private long requestId;
    private String message;

}
