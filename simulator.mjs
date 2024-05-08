import clipboardy from 'clipboardy';
import {asyncAsk} from './websocket.mjs';
import ClipboardLockAsk from "./zfooes/packet/ClipboardLockAsk.mjs";
import ClipboardUnlockAsk from "./zfooes/packet/ClipboardUnlockAsk.mjs";

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