import clipboardy from 'clipboardy';
import TurndownService from 'turndown';
import {send, asyncAsk} from './websocket.mjs';
import ClipboardLockAsk from "./zfooes/packet/ClipboardLockAsk.mjs";
import ClipboardUnlockAsk from "./zfooes/packet/ClipboardUnlockAsk.mjs";
import SimulatorStatusAsk from "./zfooes/packet/SimulatorStatusAsk.mjs";


// ---------------------------------------------------------------------------------------------------------------------
let oldClipboard = "";

export async function copyBefore() {
    const lockAnswer = await asyncAsk(new ClipboardLockAsk());
    oldClipboard = clipboardy.readSync();
}

// 一直复制，直到剪贴板内容变化
export async function copyAfter() {
    let clipboard = "";
    for (let i = 0; i < 10000; i++) {
        clipboard = clipboardy.readSync();
        if (oldClipboard !== clipboard) {
            break;
        }
    }
    const unlockAnswer = await asyncAsk(new ClipboardUnlockAsk());
    return clipboard;
}

// ---------------------------------------------------------------------------------------------------------------------
const turndownService = new TurndownService({
    preformattedCode: true
});

export function htmlToMarkdown(html) {
    const markdown = turndownService.turndown(html);
    return markdown;
}

// ---------------------------------------------------------------------------------------------------------------------
export function sendNotLoginStatus(simulator) {
    const ask = new SimulatorStatusAsk();
    ask.message = `${simulator} 没有登录，请您在AI模拟器打开的浏览器中登录，或者如果不想使用这个ai请在config.yaml中移除改配置`;
    send(ask);
}

export function sendRestartStatus(simulator) {
    const ask = new SimulatorStatusAsk();
    ask.message = `${simulator} 页面超时，重启浏览器`;
    send(ask);
    throw new Error(ask.message);
}