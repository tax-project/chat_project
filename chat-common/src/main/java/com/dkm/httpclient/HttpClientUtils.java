package com.dkm.httpclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Map;

/**
 * @Author qf
 * @Date 2019/9/21
 * @Version 1.0
 */
@Component
public class HttpClientUtils {

    private CloseableHttpClient httpClient;

    public HttpClientUtils() {
        //打开浏览器
        httpClient = HttpClients.createDefault();
    }

    /**
     * GET请求
     * @param url
     * @return
     * @throws Exception
     */
    public HttpResult get(String url) throws Exception {

        // 声明URIBuilder
        URIBuilder uriBuilder = new URIBuilder(url);

        // 2 创建httpGet对象，相当于设置url请求地址
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        // 3 使用HttpClient执行httpGet，相当于按回车，发起请求
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            HttpResult httpResult = new HttpResult();
            httpResult.setCode(404);
            httpResult.setBody("请求失败");
            return httpResult;
        }

        // 4 解析结果，封装返回对象httpResult，相当于显示相应的结果
       return resultSort(response);
    }

    private HttpResult resultSort (CloseableHttpResponse response) throws IOException {
        // 4 解析结果，封装返回对象httpResult，相当于显示相应的结果
        String charSet = "UTF-8";
        HttpResult httpResult = new HttpResult();
        // 解析数据封装HttpResult
        if (response.getEntity() != null) {
            httpResult.setCode(response.getStatusLine().getStatusCode());
            httpResult.setBody(EntityUtils.toString(response.getEntity(),charSet));

        } else {
            httpResult.setCode(response.getStatusLine().getStatusCode());
        }

        // 返回
        return httpResult;
    }


    /**
     * POST请求
     * @param url
     * @param jsonInfo
     * @return
     * @throws Exception
     */
    public HttpResult post(String url, String jsonInfo) throws Exception {
        // 声明httpPost请求
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Content-Type", "application/json");
        String charSet = "UTF-8";
        StringEntity entity = new StringEntity(jsonInfo, charSet);
        httpPost.setEntity(entity);

        // 使用HttpClient发起请求，返回response
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            HttpResult httpResult = new HttpResult();
            httpResult.setCode(404);
            httpResult.setBody("请求失败");
            return httpResult;
        }

        // 解析response封装返回对象httpResult
        return resultSort(response);
    }


    /**
     * PUT请求
     * @param url
     * @param jsonInfo
     * @return
     * @throws Exception
     */
    public HttpResult put(String url, String jsonInfo) throws Exception {
        // 声明httpPost请求
        HttpPut httpPut = new HttpPut(url);

        httpPut.setHeader("Content-Type", "application/json");
        String charSet = "UTF-8";
        StringEntity entity = new StringEntity(jsonInfo, charSet);
        httpPut.setEntity(entity);

        // 使用HttpClient发起请求，返回response
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(httpPut);
        } catch (IOException e) {
            HttpResult httpResult = new HttpResult();
            httpResult.setCode(404);
            httpResult.setBody("请求失败");
            return httpResult;
        }

        // 解析response封装返回对象httpResult
        return resultSort(response);
    }

    /**
     * Delete请求
     * @param url
     * @param map
     * @return
     * @throws Exception
     */
    public HttpResult delete(String url, Map<String, Object> map) throws Exception {

        // 声明URIBuilder
        URIBuilder uriBuilder = new URIBuilder(url);

        // 判断参数map是否为非空
        if (map != null) {
            // 遍历参数
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                // 设置参数
                uriBuilder.setParameter(entry.getKey(), entry.getValue().toString());
            }
        }

        // 2 创建httpGet对象，相当于设置url请求地址
        HttpDelete httpDelete = new HttpDelete(uriBuilder.build());

        // 3 使用HttpClient执行httpGet，相当于按回车，发起请求
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(httpDelete);
        } catch (IOException e) {
            HttpResult httpResult = new HttpResult();
            httpResult.setCode(404);
            httpResult.setBody("请求失败");
            return httpResult;

        }

        // 4 解析结果，封装返回对象httpResult，相当于显示相应的结果
        return resultSort(response);
    }

    /**
     * 带token请求get
     * @return
     */
    public HttpResult get (String url, String tokenString) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);

        try {
            if (tokenString != null && !tokenString.equals("")) {
                //自定义header头，用于token验证使用
                get.addHeader("token", tokenString);
                get.addHeader("Content-Type", "application/json");
                HttpResponse response = httpClient.execute(get);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    //返回json格式
                    HttpResult res = new HttpResult();
                    if (response.getEntity() != null) {
                        res.setCode(response.getStatusLine().getStatusCode());
                        res.setBody(EntityUtils.toString(response.getEntity(),"UTF-8"));

                    } else {
                        res.setCode(response.getStatusLine().getStatusCode());
                        res.setBody("调用错误");
                    }
                    return res;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }


    /**
     * 带token请求Post
     * @param url
     * @param jsonInfo
     * @return
     * @throws Exception
     */
    public HttpResult post(String url, String jsonInfo,String token) throws Exception {
        // 声明httpPost请求
        HttpPost httpPost = new HttpPost(url);

        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Authorization",token);
        String charSet = "UTF-8";
        StringEntity entity = new StringEntity(jsonInfo, charSet);
        httpPost.setEntity(entity);

        // 使用HttpClient发起请求，返回response
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            HttpResult httpResult = new HttpResult();
            httpResult.setCode(404);
            httpResult.setBody("请求失败");
            return httpResult;
        }

        // 解析response封装返回对象httpResult
        return resultSort(response);
    }


    /**
     * 转file
     * @param ins
     * @param file
     */
    public static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 带token请求Post
     * 文件上传
     * 调文件服务器的httpClient方法
     * @param file
     * @param token
     * @param url
     * @return
     * @throws Exception
     */
    public HttpResult filePost(MultipartFile file, String token, String url) throws Exception {
        //创建httpPost
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        //装token
        httpPost.addHeader("Authorization",token);

        //把文件加到HTTP的post请求中
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file",
              file.getInputStream(),
              ContentType.APPLICATION_OCTET_STREAM,
              file.getOriginalFilename());

        HttpEntity entity = builder.build();
        httpPost.setEntity(entity);
        HttpResponse response = httpClient.execute(httpPost);
        /**请求发送成功，并得到响应**/
        HttpResult httpResult = new HttpResult();
        // 解析数据封装HttpResult
        String charSet = "UTF-8";
        if (response.getEntity() != null) {
            httpResult.setCode(response.getStatusLine().getStatusCode());
            httpResult.setBody(EntityUtils.toString(response.getEntity(),charSet));

        } else {
            httpResult.setCode(response.getStatusLine().getStatusCode());
        }

        // 返回结果
        return httpResult;
    }


}
