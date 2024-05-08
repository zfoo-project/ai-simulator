import SignalAttachment from './attachment/SignalAttachment.mjs';
import Message from './common/Message.mjs';
import Error from './common/Error.mjs';
import Heartbeat from './common/Heartbeat.mjs';
import Ping from './common/Ping.mjs';
import Pong from './common/Pong.mjs';
import SimulatorRegisterAsk from './packet/SimulatorRegisterAsk.mjs';
import SimulatorRegisterAnswer from './packet/SimulatorRegisterAnswer.mjs';
import SimulatorChatAsk from './packet/SimulatorChatAsk.mjs';
import SimulatorChatAnswer from './packet/SimulatorChatAnswer.mjs';
import SimulatorStatusAsk from './packet/SimulatorStatusAsk.mjs';
import ChatBotRequest from './packet/ChatBotRequest.mjs';
import ChatBotNotice from './packet/ChatBotNotice.mjs';
import ChatBotRegisterRequest from './packet/ChatBotRegisterRequest.mjs';
import ChatBotRegisterResponse from './packet/ChatBotRegisterResponse.mjs';
import ClipboardLockAsk from './packet/ClipboardLockAsk.mjs';
import ClipboardLockAnswer from './packet/ClipboardLockAnswer.mjs';
import ClipboardUnlockAsk from './packet/ClipboardUnlockAsk.mjs';
import ClipboardUnlockAnswer from './packet/ClipboardUnlockAnswer.mjs';

const protocols = new Map();

// initProtocol
protocols.set(0, SignalAttachment);
protocols.set(100, Message);
protocols.set(101, Error);
protocols.set(102, Heartbeat);
protocols.set(103, Ping);
protocols.set(104, Pong);
protocols.set(1000, SimulatorRegisterAsk);
protocols.set(1001, SimulatorRegisterAnswer);
protocols.set(1010, SimulatorChatAsk);
protocols.set(1011, SimulatorChatAnswer);
protocols.set(1021, SimulatorStatusAsk);
protocols.set(1100, ChatBotRequest);
protocols.set(1101, ChatBotNotice);
protocols.set(1102, ChatBotRegisterRequest);
protocols.set(1103, ChatBotRegisterResponse);
protocols.set(1200, ClipboardLockAsk);
protocols.set(1201, ClipboardLockAnswer);
protocols.set(1202, ClipboardUnlockAsk);
protocols.set(1203, ClipboardUnlockAnswer);

class ProtocolManager {
    static getProtocol(protocolId) {
        const protocol = protocols.get(protocolId);
        if (protocol === null) {
            throw new Error('[protocolId:' + protocolId + ']协议不存在');
        }
        return protocol;
    }

    static write(buffer, packet) {
        const protocolId = packet.protocolId();
        buffer.writeShort(protocolId);
        const protocol = ProtocolManager.getProtocol(protocolId);
        protocol.write(buffer, packet);
    }

    static read(buffer) {
        const protocolId = buffer.readShort();
        const protocol = ProtocolManager.getProtocol(protocolId);
        const packet = protocol.read(buffer);
        return packet;
    }
}


export default ProtocolManager;
