# news_crawler


> 对目前主流的反爬虫网站进行数据爬取。

针对状态码为521的网站进行多次请求后获取关键参数进行验证，绕过反爬虫策略。
代码中的例子为一网站的新闻爬取。


## news_crawler 使用指南

  springboot项目，导入后即可使用


### news_crawler example 使用示例

  关键代码：
  
            HttpClient client = HttpClients.createDefault();
            HttpGet get = new HttpGet(url);
            get.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            HttpResponse response = client.execute(get);
            String __jsluid = getJsluid(response);
            String body = getResponseBodyAsString(response);
            logger.info("body:" + body);
            String __jsl_clearance = getJslClearance(body);
            logger.info(__jsluid + "; " + __jsl_clearance);

            HttpGet get1 = new HttpGet(url);
            get1.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            get1.setHeader("Cookie", __jsluid + "; " + __jsl_clearance);
            HttpResponse response1 = client.execute(get1);

            String body1 = getResponseBodyAsString(response1);

            String __jsl_clearance1 = getJslSecondClearance(body1);
  

## Release History 版本历史

* 0.2.1
    * CHANGE: Update docs
* 0.2.0
    * CHANGE: Remove `README.md`
* 0.1.0
    * Work in progress

## Authors 关于作者

* **Evan** -  [hongqueban](https://hongqueban.cn)


## License 授权协议

这个项目 MIT 协议， 请点击 [LICENSE](/LICENSE) 了解更多细节。
