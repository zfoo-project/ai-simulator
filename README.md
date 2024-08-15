## 这个项目已经停止维护，对应网站的页面更新同时也需要更新js源代码匹配对应的标签，所以没时间维护。

## 原理解析可以看下[视频教程](https://www.bilibili.com/video/BV19U411d7jM/?vd_source=4f3b881aea002f58e78c896adbef428d)，可以自己修改源代码。

## ai-simulator AI模拟器

[本地运行的免费AI模拟器](https://github.com/zfoo-project/ai-simulator)，理论上可以支持所有网页端的AI，目前支持以下AI

- 阿里-通义千问
- 腾讯-混元
- 百度-文心一言
- 字节-豆包
- 智谱清言
- 天工AI
- Kimi
- Bing(需科学上网)
- Google Gemini(需科学上网)
- OpenAI(需科学上网，近期OpenAI策略调整，暂时无法访问)

## 项目如何维护

因为完全是网页运行，如果网页发生变化会影响到程序的运行，这个时候需要做对应的修改以适配最新的网页元素

### 登录界面的维护

- 首先不要登录，分别启动每个ai，然后观察是否有报错，主要是观察html的登录标签是否生效

### 聊天界面的维护

- 登录过后，提问一下，然后观察是否有报错，主要是观察下面几个html的标签是否找的到
  - askQuestion 输入标签，用来输入问题的输入框
  - updateQuestion 回答标签，用来显示问题的回答
  - completeQuestion 停止标签，用来显示是否生成完毕
  - completeQuestion 复制标签，用来复制最终的MarkDown回答

# Attention：本项目仅供学习交流使用，严禁用于任何商业用途

## FAQ

- 为什么需要账号登录

```text
ai模拟器，顾名思义，模拟用户本地浏览器操作的一个模拟程序，最终还是用户自己本地电脑的访问操作
```

- ChatGPT访问不了

```text
不提供任何VPN代理服务，完全是一个本地运行的模拟程序
```

- userData文件夹是什么

```text
userData 文件夹属于浏览器的缓存文件（登录信息也存储在这个文件夹中），可以直接复制这个文件夹到其它的 ai-simulator 版本使用（避免重复登录）
```

## 相关的依赖，version-0.0.1 代号白漂

- [zfoo全场景RPC框架](https://github.com/zfoo-project/zfoo)
- [lux-ui前端UI](https://github.com/yangjiakai/lux-ui)
- [puppeteer浏览器控制](https://github.com/puppeteer/puppeteer)


