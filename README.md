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

### 温馨提示：
   1.pom文件配置问题

                 <!--V8:谷歌开源的运行JavaScript脚本的库-->
                        <dependency>
                            <groupId>com.eclipsesource.j2v8</groupId>
                            <artifactId>j2v8_linux_x86_64</artifactId>
                <!--            <artifactId>j2v8_macosx_x86_64</artifactId>-->
                <!--            <artifactId>j2v8_win32_x86</artifactId>-->
                            <version>4.8.0</version>
                        </dependency>
  脚本库需根据使用场景进行灵活更换；如部署在linux服务器改成 j2v8_linux_x86_64，在本地运行win电脑改成 j2v8_win32_x86。


   2.js代码注入问题
   
              body = body.trim()
                            .replace("<script>", "")
                            .replace("</script>", "");
                    StringBuilder sb = new StringBuilder(body);
                    sb.insert(0, "var window={ua:''};var setTimeout=function(f,t){f()};
                    location={};document={};alert={}; ");
                    sb.append(";function test() {\n" +
                            "        return document.cookie;\n" +
                            "    }test();");
                    body = sb.toString();
                    
   该代码块js原生组件注入需根据网站返回结果进行自定义，查看报错文件即可。
## History 版本历史

* 1.4
    * 增加无需反爬虫场景 适应更多需求 代码更加健壮 
* 1.3
    * 修改配置文件为yml格式，结构更清晰 
* 1.2
    * 修改日志模块
* 1.1.0
    * 定时任务修改
* 1.0
    * 代码首次提交

## Authors 关于作者

* **Evan** -  [hongqueban](https://hongqueban.cn)


## License 授权协议

这个项目 MIT 协议， 请点击 [LICENSE](/LICENSE) 了解更多细节。
