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
public class SimulatorChatAnswer {

    private long requestId;

    private int simulator;

    private String markdown;

}
