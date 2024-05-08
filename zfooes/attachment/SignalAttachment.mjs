
class SignalAttachment {
    signalId = 0; // number
    taskExecutorHash = 0; // number
    // 0 for the server, 1 or 2 for the sync or async native client, 12 for the outside client such as browser, mobile
    client = 0; // number
    timestamp = 0; // number

    static PROTOCOL_ID = 0;

    protocolId() {
        return SignalAttachment.PROTOCOL_ID;
    }

    static write(buffer, packet) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(-1);
        buffer.writeByte(packet.client);
        buffer.writeInt(packet.signalId);
        buffer.writeInt(packet.taskExecutorHash);
        buffer.writeLong(packet.timestamp);
    }

    static read(buffer) {
        const length = buffer.readInt();
        if (length === 0) {
            return null;
        }
        const beforeReadIndex = buffer.getReadOffset();
        const packet = new SignalAttachment();
        const result0 = buffer.readByte();
        packet.client = result0;
        const result1 = buffer.readInt();
        packet.signalId = result1;
        const result2 = buffer.readInt();
        packet.taskExecutorHash = result2;
        const result3 = buffer.readLong();
        packet.timestamp = result3;
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }

}
export default SignalAttachment;
