import puppeteer from 'puppeteer-extra';
import StealthPlugin from 'puppeteer-extra-plugin-stealth';
import {startWebsocketClient, registerPacketReceiver, send, delay} from './websocket.mjs';
import SimulatorStatusAsk from "./zfooes/packet/SimulatorStatusAsk.mjs";
import SimulatorChatAsk from "./zfooes/packet/SimulatorChatAsk.mjs";
import SimulatorChatAnswer from "./zfooes/packet/SimulatorChatAnswer.mjs";
import {copyBefore, copyAfter} from './simulator.mjs';

const simulator = 400;
const simulatorName = 'alibaba';
const url = 'https://tongyi.aliyun.com/qianwen';

// status
let login = false;

// question queue
const questions = [];
let currentQuestion = null;

// generate
let generating = false;
let generateTime = 0;
let lastGenerateText = "";

// config
let chromePath = '';
let headless = false;
if (process.argv.length >= 4) {
    chromePath = process.argv[2];
    headless = process.argv[3] === "true";
}
console.log(`${simulator} simulator:[${simulatorName}] chromePath:[${chromePath}] headless:[${headless}]`);
// ---------------------------------------------------------------------------------------------------------------------
// C:\Users\Administrator\.cache\puppeteer
// open browser
puppeteer.use(StealthPlugin());
// Launch the browser and open a new blank page
const browser = await puppeteer.launch(
    {
        headless: headless,
        executablePath: chromePath,
        userDataDir: './userData/' + simulatorName
    }
);
const context = browser.defaultBrowserContext();
await context.overridePermissions(url, ['clipboard-read', 'clipboard-write', 'clipboard-sanitized-write']);
const page = await browser.newPage();
await page.setViewport({
    width: 1280,
    height: 768,
    deviceScaleFactor: 1
});
await page.goto(url, {waitUntil: 'networkidle0'});

// ---------------------------------------------------------------------------------------------------------------------
const checkLoginStatues = async () => {
    const loginButton = await page.$('.tagBtn--g8CQ4gKW');
    if (loginButton == null) {
        login = true;
        return;
    }
    const ask = new SimulatorStatusAsk();
    ask.message = `${simulatorName} - ${simulator} 没有登录，请您在浏览器登录，如果不希望使用这个ai请在config.yaml中移除配置`;
    send(ask);
    login = false;
}

// ---------------------------------------------------------------------------------------------------------------------
const atSimulatorChatAsk = async (packet) => {
    console.log("atSimulatorChatAsk --> " + packet.message);
    questions.unshift(packet);
}

const checkGenerateStatues = async () => {
    if (!login || !generating) {
        return;
    }
    const now = new Date().getTime();
    // 超时没有输出，则重新加载页面
    if (now - generateTime < 70 * 1000) {
        return;
    }
    console.log("页面超时，重新加载页面")
    page.reload();
    generating = false;
}

const askQuestion = async () => {
    if (questions.length === 0 || !login || generating) {
        return;
    }
    currentQuestion = questions.pop();
    const inputSelector = '.textarea--g7EUvnQR';
    await page.waitForSelector(inputSelector);
    await page.focus(inputSelector);
    await page.click(inputSelector);
    await page.type(inputSelector, currentQuestion.message, {delay: 100});
    await page.keyboard.press("Enter");

    generating = true;
    generateTime = new Date().getTime();
    console.log("ask question: " + currentQuestion.message);
    await delay(5000);
}
// ---------------------------------------------------------------------------------------------------------------------
const updateQuestion = async () => {
    if (!login || !generating) {
        return;
    }

    const answers = await page.$$('.tongyi-ui-markdown');
    if (answers.length <= 0) {
        return;
    }

    var lastElement = answers[answers.length - 1];
    const text = await lastElement?.evaluate(el => el.textContent);
    if (text === lastGenerateText) {
        return;
    }
    lastGenerateText = text;
    generateTime = new Date().getTime();

    const html = await lastElement?.evaluate(el => el.outerHTML);
    const chatAnswer = new SimulatorChatAnswer();
    chatAnswer.requestId = currentQuestion.requestId;
    chatAnswer.simulator = simulator;
    chatAnswer.html = html;
    send(chatAnswer);

    console.log("1-text-----------------------------------------------------------------------------------------------------------");
    console.log(text);
    console.log("2-html-----------------------------------------------------------------------------------------------------------");
    console.log(html);
}

const completeQuestion = async () => {
    if (!login || !generating) {
        return;
    }
    const generatingButton = await page.$('.stop--L_ctTut8');
    if (generatingButton != null) {
        return;
    }
    const now = new Date().getTime();
    if (now - generateTime < 7 * 1000) {
        return;
    }
    const copyEles = await page.$$('.btn--Bw0FbWYV');
    const length = copyEles.length;
    if (length === 0) {
        return;
    }
    await copyBefore();
    const copyButton = copyEles[length - 3]
    await copyButton.focus();
    await copyButton.click();
    const clipboard = await copyAfter();
    console.log("copy-----------------------------------------------------------------------------------------------------------");
    console.log(clipboard);

    const chatAnswer = new SimulatorChatAnswer();
    chatAnswer.requestId = currentQuestion.requestId;
    chatAnswer.simulator = simulator;
    chatAnswer.markdown = clipboard;
    send(chatAnswer);

    generating = false;
}

// ---------------------------------------------------------------------------------------------------------------------
const tick = async () => {
    await page.keyboard.press("PageDown");
}
// ---------------------------------------------------------------------------------------------------------------------
// initWebsocket
registerPacketReceiver(SimulatorChatAsk.PROTOCOL_ID, atSimulatorChatAsk);
startWebsocketClient(simulator);

let updateFlag = true;
setInterval(async () => {
    try {
        if (updateFlag) {
            updateFlag = false;
            await checkLoginStatues();
            await checkGenerateStatues();
            await tick();
            await askQuestion();
            await updateQuestion();
            await completeQuestion();
            updateFlag = true;
        }
    } catch (e) {
        updateFlag = true;
        console.error(e);
    }
}, 700);
