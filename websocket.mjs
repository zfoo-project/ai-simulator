import WebSocket from "ws";

import ByteBuffer from './zfooes/buffer/ByteBuffer.mjs';
import SignalAttachment from './zfooes/attachment/SignalAttachment.mjs';
import ProtocolManager from './zfooes/ProtocolManager.mjs';
import Error from './zfooes/common/Error.mjs';
import Message from './zfooes/common/Message.mjs';
import Ping from './zfooes/common/Ping.mjs';
import Pong from './zfooes/common/Pong.mjs';
import SimulatorRegisterAsk from "./zfooes/packet/SimulatorRegisterAsk.mjs";
import SimulatorRegisterAnswer from "./zfooes/packet/SimulatorRegisterAnswer.mjs";


const wsUrl = "ws://127.0.0.1:17313/websocket";
let simulator = 0;
let pingTime = new Date().getTime();
let tickTime = 20 * 1000;
let ws = null;
let uuid = 0;

// number, EncodedPacketInfo
const signalAttachmentMap = new Map();

export function startWebsocketClient(chatAI) {
    simulator = chatAI;
    ws = connect("init websocket");
}

setInterval(() => reconnect(), tickTime);

// 如果服务器长时间没有回应，则重新连接
function reconnect() {
    if (ws == null) {
        return;
    }
    if (!isWebsocketReady()) {
        console.log("正在连接服务器");
        ws.close(3999);
        ws = connect("timeout and reconnect");
        return;
    }
    send(new Ping())
}

// readyState的状态码定义
// 0 (CONNECTING)，正在链接中
// 1 (OPEN)，已经链接并且可以通讯
// 2 (CLOSING)，连接正在关闭
// 3 (CLOSED)，连接已关闭或者没有链接成功
function connect(desc) {
    console.log(new Date(), 'start connect websocket: ' + desc);

    const webSocket = new WebSocket(wsUrl);

    webSocket.binaryType = 'arraybuffer';

    webSocket.onopen = async function () {
        console.log(new Date(), 'websocket open success');

        // websocket连接成功过后，先发送ping同步服务器时间，再发送登录请求
        send(new Ping());

        // 登录
        const registerAsk = new SimulatorRegisterAsk();
        registerAsk.simulator = simulator;
        const answer = await asyncAsk(registerAsk);
        console.log("----------------------------------");
        console.log(answer);
    };


    webSocket.onmessage = function (event) {
        const data = event.data;

        const buffer = new ByteBuffer();
        buffer.writeBytes(data);
        buffer.setReadOffset(4);
        const packet = ProtocolManager.read(buffer);

        if (packet.protocolId() === Error.prototype.protocolId()) {
            console.error(packet);
            return;
        }

        let attachment = null;
        if (buffer.isReadable() && buffer.readBoolean()) {
            console.log(new Date(), "Websocket async answer <-- ", packet);
            attachment = ProtocolManager.read(buffer);
            const encodedPacketInfo = signalAttachmentMap.get(attachment.signalId);
            if (encodedPacketInfo == null) {
                throw "timeout SignalAttachment:" + attachment;
            }
            encodedPacketInfo.promiseResolve(packet);
            return;
        }
        console.log(new Date(), "Websocket response <-- ", packet);
        if (packet.protocolId() === Pong.prototype.protocolId()) {
            if (Number.isInteger(packet.time)) {
                pingTime = packet.time;
            } else {
                pingTime = Number.parseInt(packet.time);
            }
            return;
        }

        route(packet);
    };

    webSocket.onerror = function (event) {
        console.log(new Date(), 'websocket error');
    };

    webSocket.onclose = function (event) {
        console.log(new Date(), 'websocket close');
    };
    return webSocket;
}

export function isWebsocketReady() {
    return ws !== null && ws.readyState === 1;
}

export function send(packet, attachment) {
    if (!isWebsocketReady()) {
        console.error("websocket is not ready");
        return;
    }
    switch (ws.readyState) {
        case 0:
            console.log(new Date(), "0, ws connecting server");
            break;
        case 1:
            const buffer = new ByteBuffer();
            buffer.setWriteOffset(4);
            ProtocolManager.write(buffer, packet);
            if (attachment == null) {
                buffer.writeBoolean(false);
                console.log(new Date(), "Websocket send request --> ", packet)
            } else {
                buffer.writeBoolean(true);
                ProtocolManager.write(buffer, attachment)
                console.log(new Date(), "Websocket async ask --> ", packet)
            }
            const writeOffset = buffer.writeOffset;
            buffer.setWriteOffset(0);
            buffer.writeRawInt(writeOffset - 4);
            buffer.setWriteOffset(writeOffset);
            ws.send(buffer.buffer);
            break;
        case 2:
            pingTime = pingTime - 60 * 1000;
            console.log(new Date(), "2, ws is closing, trying to reconnect");
            break;
        case 3:
            pingTime = pingTime - 60 * 1000;
            console.log(new Date(), "3, ws is closing, trying to reconnect");
            break;
        default:
            console.log(new Date(), "4, server error");
    }
}

class EncodedPacketInfo {
    constructor(promiseResolve, promiseReject, attachment) {
        this.promiseResolve = promiseResolve;
        this.promiseReject = promiseReject;
        this.attachment = attachment;
    }
}

export async function asyncAsk(packet) {
    const currentTime = new Date().getTime();
    const attachment = new SignalAttachment();
    uuid++;
    const signalId = uuid;
    attachment.timestamp = currentTime;
    attachment.signalId = signalId;
    attachment.taskExecutorHash = -1;
    attachment.client = 12;
    const encodedPacketInfo = new EncodedPacketInfo();
    encodedPacketInfo.attachment = attachment;
    const promise = new Promise((resolve, reject) => {
        encodedPacketInfo.promiseResolve = resolve;
        encodedPacketInfo.promiseReject = reject;
    });
    // 遍历删除旧的attachment
    const deleteList = [];
    signalAttachmentMap.forEach((value, key) => {
        if (value == null || value.attachment == null) {
            deleteList.push(key);
        }
        const att = value.attachment;
        if (att == null) {
            deleteList.push(key);
        }
        const time = att == null ? 0 : att.timestamp;
        if (currentTime - time > 60000) {
            deleteList.push(key);
        }
    });
    deleteList.forEach(it => signalAttachmentMap.delete(it));
    signalAttachmentMap.set(signalId, encodedPacketInfo);
    send(packet, attachment);
    return promise;
}

// number, function
const receiverMap = new Map();

export function registerPacketReceiver(protocolId, fun) {
    receiverMap.set(protocolId, fun);
}

function route(packet) {
    const receiver = receiverMap.get(packet.protocolId());
    if (receiver == null) {
        console.log("router not exist ", packet);
        return;
    }
    receiver(packet);
}

export function delay(time) {
    return new Promise(function (resolve) {
        setTimeout(resolve, time)
    });
}