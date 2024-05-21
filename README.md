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

# Attention：本项目仅供学习交流使用，严禁用于任何商业用途


## FAQ

- 为什么需要账号登录
```text
ai模拟器，顾名思义，模拟用户本地浏览器操作的一个模拟程序，最终还是用户自己本地电脑的访问操作
```

- 为什么不能用ChatGPT
```text
不提供任何VPN代理服务，完全是一个本地运行的模拟程序
```

## 相关的依赖

- [zfoo全场景RPC框架](https://github.com/zfoo-project/zfoo)
- [lux-ui前端UI](https://github.com/yangjiakai/lux-ui)
- [puppeteer浏览器控制](https://github.com/puppeteer/puppeteer)

### version-0.0.1 代号白漂

### userData 文件夹属于浏览器的缓存文件（登录信息也存储在这个文件夹中），可以直接复制这个文件夹到其它的 ai-simulator 版本使用（避免重复登录）