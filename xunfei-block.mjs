import puppeteer from 'puppeteer-extra';
import StealthPlugin from 'puppeteer-extra-plugin-stealth';
import {startWebsocketClient, registerPacketReceiver, send, delay} from './websocket.mjs';
import SimulatorChatAsk from "./zfooes/packet/SimulatorChatAsk.mjs";
import SimulatorChatAnswer from "./zfooes/packet/SimulatorChatAnswer.mjs";
import {copyBefore, copyAfter, htmlToMarkdown, sendNotLoginStatus, sendRestartStatus} from './simulator.mjs';

const simulator = 'xunfei';
const url = 'https://xinghuo.xfyun.cn/';

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
console.log(`simulator:[${simulator}] chromePath:[${chromePath}] headless:[${headless}]`);
// ---------------------------------------------------------------------------------------------------------------------
// C:\Users\Administrator\.cache\puppeteer
// open browser
puppeteer.use(StealthPlugin());
// Launch the browser and open a new blank page
const browser = await puppeteer.launch(
    {
        headless: headless,
        executablePath: chromePath,
        userDataDir: './userData/' + simulator
    }
);
const context = browser.defaultBrowserContext();
await context.overridePermissions(url, ['clipboard-read', 'clipboard-write', 'clipboard-sanitized-write']);
const pages = await browser.pages();
const page = pages[0];
page.on('framenavigated', frame => {
    console.log('URL changed to ' + frame.url());
});
page.on('request', interceptedRequest => {
    const allowRedirect = false; // 禁止地址栏重定向
    if (interceptedRequest.isNavigationRequest() && allowRedirect === false) {
    } else {
    }
});

await page.goto(url, {waitUntil: 'networkidle0'});
await page.evaluate(() => {
    window.addEventListener('beforeunload', function(event) {
        event.preventDefault();
        event.returnValue = ''; // 设置为空字符串或者具体的提示信息
        // 弹出提示框，询问用户是否要离开页面
        // 返回值为空字符串时，用户可以继续离开；返回具体的提示信息时，用户会看到提示信息并确定是否要离开
    });
});

// ---------------------------------------------------------------------------------------------------------------------
const checkLoginStatues = async () => {
    const loginButton = await page.$('.header_login_button__bAAR8');
    if (loginButton == null) {
        login = true;
        return;
    }
    sendNotLoginStatus(simulator);
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
    await browser.close();
    sendRestartStatus(simulator);
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

    const lastElement = answers[answers.length - 1];
    const html = await lastElement?.evaluate(el => el.innerHTML);
    if (html === lastGenerateText) {
        return;
    }
    lastGenerateText = html;
    generateTime = new Date().getTime();

    const markdown = htmlToMarkdown(html);
    const chatAnswer = new SimulatorChatAnswer();
    chatAnswer.requestId = currentQuestion.requestId;
    chatAnswer.simulator = simulator;
    chatAnswer.markdown = markdown;
    send(chatAnswer);
    console.log("html to markdown-----------------------------------------------------------------------------------------------");
    console.log(markdown);
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
setInterval(async () => {
    await page.keyboard.press("Escape");
}, 1000);
setInterval(async () => {
    await checkGenerateStatues();
    try {
        await checkLoginStatues();
        await tick();
    } catch (e) {
        console.error(e);
    }
}, 3000);

let updateFlag = true;
setInterval(async () => {
    try {
        if (updateFlag) {
            updateFlag = false;
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
