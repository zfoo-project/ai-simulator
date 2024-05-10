
class SimulatorChatAnswer {
    requestId = 0; // number
    simulator = ""; // string
    markdown = ""; // string

    static PROTOCOL_ID = 1011;

    protocolId() {
        return SimulatorChatAnswer.PROTOCOL_ID;
    }

    static write(buffer, packet) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(-1);
        buffer.writeString(packet.markdown);
        buffer.writeLong(packet.requestId);
        buffer.writeString(packet.simulator);
    }

    static read(buffer) {
        const length = buffer.readInt();
        if (length === 0) {
            return null;
        }
        const beforeReadIndex = buffer.getReadOffset();
        const packet = new SimulatorChatAnswer();
        const result0 = buffer.readString();
        packet.markdown = result0;
        const result1 = buffer.readLong();
        packet.requestId = result1;
        const result2 = buffer.readString();
        packet.simulator = result2;
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }

}
export default SimulatorChatAnswer;
