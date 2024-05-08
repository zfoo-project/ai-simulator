
class SimulatorChatAsk {
    requestId = 0; // number
    message = ""; // string

    static PROTOCOL_ID = 1010;

    protocolId() {
        return SimulatorChatAsk.PROTOCOL_ID;
    }

    static write(buffer, packet) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(-1);
        buffer.writeString(packet.message);
        buffer.writeLong(packet.requestId);
    }

    static read(buffer) {
        const length = buffer.readInt();
        if (length === 0) {
            return null;
        }
        const beforeReadIndex = buffer.getReadOffset();
        const packet = new SimulatorChatAsk();
        const result0 = buffer.readString();
        packet.message = result0;
        const result1 = buffer.readLong();
        packet.requestId = result1;
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }

}
export default SimulatorChatAsk;
