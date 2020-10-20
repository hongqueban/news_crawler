package com.ustcinfo.hftnews.service;

import com.eclipsesource.v8.V8;

import com.eclipsesource.v8.V8Object;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.Mapper;
import com.ejlchina.okhttps.OkHttps;
import com.ustcinfo.hftnews.mapper.SaveNewsInfoMapper;
import com.ustcinfo.hftnews.model.News;
import com.ustcinfo.hftnews.utils.DeleteFileUtil;
import com.ustcinfo.hftnews.utils.IOUtils;
import com.ustcinfo.hftnews.utils.UUIDUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 定时任务类
 *
 * @author zhang.yifan
 * @date 2019/10/9 14:31
 */
@Component
@Service
public class ScheduledTask {

    @Autowired
    private SaveNewsInfoMapper saveNewsInfoMapper;

    private Logger logger = LoggerFactory.getLogger(ScheduledTask.class);

    /**
     * 爬虫每次爬取间隔时间（ms）
     */
    final long timeInterval = 61000;

    /**
     * 定时任务
     *
     * @param
     * @return void
     * @date 2019/10/9
     */
    @Scheduled(cron = "0 50 09 * * ?")
    @Async
    public void cron() {
        logger.info("定时器执行...");
        //页面模块前缀
        String[] prefix = {"zwyw", "csbb", "bmts", "xxkd"};
        //图说模块前缀
        String[] prefixToTs = {"cs", "jj", "fg"};
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 20, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5));
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String prefixType : prefix) {
                        
                        //爬取新闻详情
                        getNewsInfo("http://www.hefei.gov.cn/ssxw/" + prefixType + "/index.html", "listnews");
                        Thread.sleep(timeInterval);
                    }
                    for (String prefixTsType : prefixToTs) {
                        //爬取新闻详情-图说
                        getNewsInfo("http://www.hefei.gov.cn/mlhf/ts/" + prefixTsType + "/index.html", "picture-list");
                        Thread.sleep(timeInterval);
                    }
                } catch (InterruptedException e) {
                    logger.error("定时器出现异常");
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 爬取新闻详情
     *
     * @param url       爬取链接
     * @param className 类名
     * @return void
     * @date 2020-01-16
     */
    private void getNewsInfo(String url, String className) {
       
        try {
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

            Connection connect = Jsoup.connect(url);
            connect.header("Cookie", __jsluid + "; " + __jsl_clearance1);
            connect.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            Document doc = connect.get();
            Elements element = doc.getElementsByClass(className);
            Elements eleHref = element.select("a[href][title]");
            for (Element ele : eleHref) {
                String href = ele.attr("href");
                Connection connection = Jsoup.connect(href);
                connection.header("Cookie", __jsluid + "; " + __jsl_clearance1);
                connection.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
                Document docc = connection.get();
                //新闻标题
                String newsTitle = docc.select("h1.newstitle").text();
                //新闻日期和来源
                String newsInfo = docc.select("div.newsinfoleft").text();
                //新闻日期
                String newsDate = newsInfo.substring(0, 10);
                //新闻来源
                String newsSource = newsInfo.substring(newsInfo.substring(0, newsInfo.indexOf("来源")).length() + 3, newsInfo.length()).replaceAll("浏览量：", "");
                //判断是否包含微信端的内容 有就剔除
                boolean contains = newsInfo.contains("微信端");
                //新闻内容
                String newsContent = docc.select("div.j-fontContent").html();
                //校验新闻内容是否有<founder-content></founder-content>标签，有则剔除，编辑器不支持该标签
                newsContent = newsContent.replaceAll("<founder-content>", "").replaceAll("</founder-content>", "").replaceAll("<founder-content deep=\"5\">", "");
                //删除首个<p>标签中出现的text-indent: 2em; 属性
                newsContent = newsContent.replaceFirst("text-indent: 2em;", "");
                //打印结果
                logger.info(newsContent);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String nowTime = df.format(new Date());
                if (newsDate.equals(nowTime) && !contains) {
                    //校验是否有图片
                    List<String> containImgList = isContainImg(newsContent);
                    News news = new News();
                    if (containImgList.size() > 0) {
                        for (String imgUrl : containImgList) {
                            //获取图片路径后再次访问存储在文件服务器
                            String fileName = UUIDUtil.generateUUID();
                            File imgFile = getImgFile("http://www.hefei.gov.cn/" + imgUrl, fileName);
                            //调用上传文件接口将图片上传到服务器
                            String imgUrlTrue = updateImg(imgFile);
                            //上传完成后删除本地文件
                            if(imgFile.exists()){
                                imgFile.delete();
                            }
                            news.setImageUrl(imgUrlTrue);
                            //替换文本中的图片地址
                            newsContent = newsContent.replaceAll(imgUrl, imgUrlTrue);
                        }
                    }
                    //生成流程号
                    String id = UUIDUtil.generateUUID();
                    news.setId(id);
                    news.setNewsTitle(newsTitle);
                    news.setNewsSource(newsSource);
                    news.setNewsContent(newsContent);
                    saveNewsInfoMapper.updataNewsInfo(news);
                }
            }
        } catch (IOException e) {
            logger.error("爬虫出现异常{}" + e.getMessage());
            e.printStackTrace();
           
        }
    }

    /**
     * 调接口上传文件
     *
     * @param imgFile 文件
     * @return java.lang.String
     * @date 2020/10/12
     */
    public String updateImg(File imgFile) {
        HttpResult httpResult = OkHttps.sync("url")
                .nothrow()
                .addFilePara("file", imgFile)
                .post();
        // 判断执行状态
        switch (httpResult.getState()) {
            // 网络错误，说明用户没网了
            case NETWORK_ERROR:
                IOException error = httpResult.getError();
                logger.error("文件上传接口调用失败：" + error.getMessage());
                return "";
            // 请求超时
            case TIMEOUT:
                logger.error("文件上传接口调用失败：请求超时");
                return "";
            // 其它异常
            case EXCEPTION:
                logger.error("文件上传接口调用失败：其它网络异常");
                return "";
            default:
        }
        Mapper mapper = httpResult.getBody().toMapper();
        String code = mapper.getString("code");
        if ("0".equals(code)) {
            String imgUrl = mapper.getString("data");
            
            return imgUrl;
        } else {
            String message = mapper.getString("message");
            logger.error(message);
            return "";
        }
    }

    /**
     * 传入img文件的URL, 返回下载好的img文件
     *
     * @param imgUrl img文件的URL地址
     * @return java.io.File
     * @date 2020/10/10
     */
    public File getImgFile(String imgUrl, String fileName) {
        File file = new File(fileName + ".jpg");
        try {
            HttpClient client = HttpClients.createDefault();
            HttpGet get = new HttpGet(imgUrl);

            get.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            HttpResponse response = client.execute(get);

            String __jsluid = getJsluid(response);
            String body = getResponseBodyAsString(response);

            String __jsl_clearance = getJslClearance(body);

            HttpGet get1 = new HttpGet(imgUrl);
            get1.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            get1.setHeader("Cookie", __jsluid + "; " + __jsl_clearance);
            HttpResponse response1 = client.execute(get1);

            String body1 = getResponseBodyAsString(response1);

            String __jsl_clearance1 = getJslSecondClearance(body1);

            HttpGet getImgFile = new HttpGet(imgUrl);
            getImgFile.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            getImgFile.setHeader("Cookie", __jsluid + "; " + __jsl_clearance1);

            response = client.execute(getImgFile);
            output(response, file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return file;
    }


    /**
     * 将HttpResponse输出到文件, 即将pdf输入流写到硬盘.
     *
     * @param response http响应
     * @param file     落地文件
     * @throws IOException IO异常
     */
    private static void output(HttpResponse response, File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(getResponseBodyAsBytes(response));
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    /**
     * 将HTTP响应体转换为byte数组返回
     *
     * @param response HTTP响应
     * @return 响应体的byte数组形式
     * @throws IOException IO异常
     */
    private static byte[] getResponseBodyAsBytes(HttpResponse response) throws IOException {
        return IOUtils.readStreamAsByteArray(response.getEntity().getContent());
    }

    /**
     * 将HTTP响应体转换为字符串返回
     *
     * @param response HTTP响应
     * @return 响应体的字符串形式
     * @throws IOException IO异常
     */
    private static String getResponseBodyAsString(HttpResponse response) throws IOException {
        return IOUtils.readStreamAsString(response.getEntity().getContent(), "UTF-8");
    }


    /**
     * 校验是否有图片并返回
     *
     * @param newsContent 新闻内容
     * @return java.lang.String
     * @date 2020-01-16
     */
    private static List<String> isContainImg(String newsContent) {
        List<String> pics = new ArrayList<String>();
        String regExImg = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        Pattern pImage = Pattern.compile
                (regExImg, Pattern.CASE_INSENSITIVE);
        Matcher mImage = pImage.matcher(newsContent);
        StringBuffer stringBuilder = new StringBuffer(newsContent);
        while (mImage.find()) {
            // 得到<img />数据
            String img = mImage.group();
            // 匹配<img>中的src数据
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                pics.add(m.group(1));
            }
        }
        return pics;
    }

    /**
     * 通过破解动态JavaScript脚本,
     * 获取cookie名为 __jsl_clearance的值
     *
     * @param body 相应内容(一般为第一次请求获取到的动态js字符串)
     * @return cookie名为 __jsl_clearance的值
     */
    private String getJslClearance(String body) {
        try {
            //V8:谷歌开源的运行JavaScript脚本的库. 参数:globalAlias=window, 表示window为全局别名,
            // 告诉V8在运行JavaScript代码时, 不要从代码里找window的定义.
            V8 runtime = V8.createV8Runtime("window");
            //将第一次请求pdf资源时获取到的字符串提取成V8可执行的JavaScript代码
            logger.info("定时器执行5...");
            body = body.trim()
                    .replace("<script>", "")
                    .replace("</script>", "")
                    .replace("document.cookie=", "")
                    .replace(";location.href=location.pathname+location.search", "");
            //用V8执行该段代码获取新的动态JavaScript脚本
            String result = runtime.executeStringScript(body);
            logger.info(result);
            //获取 jsl_clearance
            int i = result.indexOf(";");
            logger.info(result.substring(0, i));
            return result.substring(0, i);
        }catch (Exception exception){
            logger.error("V8解析出现异常{}" + exception.getMessage());
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * 获取第二个cookie
     *
     * @param body 相应内容(一般为第一次请求获取到的动态js字符串)
     * @return java.lang.String
     * @date 2020/10/9
     */
    private String getJslSecondClearance(String body) {
        //V8:谷歌开源的运行JavaScript脚本的库. 参数:globalAlias=window, 表示window为全局别名,
        // 告诉V8在运行JavaScript代码时, 不要从代码里找window的定义.
        V8 runtime = V8.createV8Runtime("window");
        V8Object v8Object = new V8Object(runtime);
        v8Object.add("userAgent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
        //v8Object.add("setTimeout","function(f,t){f()}");
        runtime.add("navigator", v8Object);
        runtime.add("window", v8Object);
        runtime.add("setTimeout", "function(f,t){f()}");
        body = body.trim()
                .replace("<script>", "")
                .replace("</script>", "");
        StringBuilder sb = new StringBuilder(body);
        sb.insert(0, "var window={ua:''};var setTimeout=function(f,t){f()};location={};document={};alert={}; ");
        sb.append(";function test() {\n" +
                "        return document.cookie;\n" +
                "    }test();");
        body = sb.toString();
        //用V8执行该段代码获取新的动态JavaScript脚本
        String result = runtime.executeStringScript(body);
        //获取 jsl_clearance
        int i = result.indexOf(";");
        logger.info(result.substring(0, i));
        return result.substring(0, i);
    }


    /**
     * 通过响应头的set-cookie
     * 获取cookie名称为__jsluid的值
     *
     * @param response HttpResponse
     * @return __jsluid的值
     */
    private String getJsluid(HttpResponse response) {
        Header header = response.getFirstHeader("set-cookie");
        String[] split = header.getValue().split(";");
        for (String s : split) {
            logger.info(s);
            if (s.contains("__jsluid_h")) {
                return s.trim();
            }
        }
        return "";
    }
}

