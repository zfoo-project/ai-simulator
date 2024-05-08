
class Message {
    code = 0; // number
    message = ""; // string

    static PROTOCOL_ID = 100;

    protocolId() {
        return Message.PROTOCOL_ID;
    }

    static write(buffer, packet) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(-1);
        buffer.writeInt(packet.code);
        buffer.writeString(packet.message);
    }

    static read(buffer) {
        const length = buffer.readInt();
        if (length === 0) {
            return null;
        }
        const beforeReadIndex = buffer.getReadOffset();
        const packet = new Message();
        const result0 = buffer.readInt();
        packet.code = result0;
        const result1 = buffer.readString();
        packet.message = result1;
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }

}
export default Message;
