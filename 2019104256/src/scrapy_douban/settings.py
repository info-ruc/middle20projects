BOT_NAME = 'scrapy_douban'

SPIDER_MODULES = ['scrapy_douban.spiders']
NEWSPIDER_MODULE = 'scrapy_douban.spiders'

LOG_LEVEL = 'INFO'
USER_AGENT = 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36'

ROBOTSTXT_OBEY = False

DOWNLOAD_DELAY = 2

COOKIES_ENABLED = False

DOWNLOADER_MIDDLEWARES = {
    'scrapy.downloadermiddlewares.useragent.UserAgentMiddleware': None,  # 把本来的代理中间件废掉
    'scrapy_douban.middlewares.RotateUserAgentMiddleware': 400,  # 切换agent
}

ITEM_PIPELINES = {
   'scrapy_douban.pipelines.ScrapyYzdPipeline': 300,
}
