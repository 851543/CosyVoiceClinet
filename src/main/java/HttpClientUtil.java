import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http工具类
 */
public class HttpClientUtil {

    static final int TIMEOUT_MSEC = 60 * 1000;

    /**
     * 发送GET方式请求
     *
     * @param url
     * @param paramMap
     * @return
     */
    public static String doGet(String url, Map<String, String> paramMap) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String result = "";
        CloseableHttpResponse response = null;

        try {
            URIBuilder builder = new URIBuilder(url);
            if (paramMap != null) {
                for (String key : paramMap.keySet()) {
                    builder.addParameter(key, paramMap.get(key));
                }
            }
            URI uri = builder.build();

            //创建GET请求
            HttpGet httpGet = new HttpGet(uri);

            //发送请求
            response = httpClient.execute(httpGet);

            //判断响应状态
            if (response.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 发送POST方式请求
     *
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 创建参数列表
            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 发送POST方式请求
     *
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                //构造json格式数据
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    jsonObject.put(param.getKey(), param.getValue());
                }
                StringEntity entity = new StringEntity(jsonObject.toString(), "utf-8");
                //设置请求编码
                entity.setContentEncoding("utf-8");
                //设置数据类型
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 发送POST方式请求
     *
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static void doPost4JsonStream(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                //构造json格式数据
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    jsonObject.put(param.getKey(), param.getValue());
                }
                StringEntity entity = new StringEntity(jsonObject.toString(), "utf-8");
                //设置请求编码
                entity.setContentEncoding("utf-8");
                //设置数据类型
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            InputStream inputStream = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // 处理每一行数据
                JSONObject jsonObject = JSON.parseObject(line);
                String responsePart = jsonObject.getString("response");
                System.out.print(responsePart); // 使用 print 而不是 println 避免自动换行
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送POST方式请求
     *
     * @param url
     * @param paramMap
     * @param promptWav
     * @return
     * @throws IOException
     */
    public static byte[] doPostByte(String url, Map<String, String> paramMap, String promptWav) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        byte[] wavByte = null;

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 构建multipart请求体
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            // 添加表单参数
            if (paramMap != null) {
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    builder.addTextBody(param.getKey(), param.getValue(),
                            ContentType.create("text/plain", "UTF-8"));
                }
            }

            // 添加音频文件
            if (promptWav != null) {
                // 添加文件
                builder.addBinaryBody("prompt_wav", ResourceReader.class.getClassLoader().getResourceAsStream(promptWav),
                        ContentType.create("application/octet-stream"), "prompt_wav");
            }

            HttpEntity multipart = builder.build();

            httpPost.setEntity(multipart);
            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                responseEntity.writeTo(baos);
                wavByte = baos.toByteArray();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return wavByte;
    }

    /**
     * 发送POST方式请求
     *
     * @param url
     * @param paramMap
     * @param promptWav
     * @return
     * @throws IOException
     */
    public static byte[] doPostBytes(String url, Map<String, String> paramMap, String promptWav) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        byte[] wavByte = null;

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 构建multipart请求体
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            // 添加表单参数
            if (paramMap != null) {
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    StringBody contentBody = new StringBody(param.getValue(),
                            "text/plain", Charset.forName("UTF-8"));
                    entity.addPart(param.getKey(), contentBody);
                }
            }

            // 添加音频文件
            if (promptWav != null) {
                // 添加文件
                ByteArrayBody audioBody = new ByteArrayBody(
                        IOUtils.toByteArray(ResourceReader.class.getClassLoader().getResourceAsStream(promptWav)),
                        ContentType.APPLICATION_OCTET_STREAM,
                        "prompt_wav"
                );
                entity.addPart("prompt_wav", audioBody);
            }

            httpPost.setEntity(entity);
            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                responseEntity.writeTo(baos);
                wavByte = baos.toByteArray();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return wavByte;
    }

    public static byte[] doPostRestTemplateByte(String url, Map<String, String> paramMap, String promptWav) throws IOException {
        // 创建配置化的RestTemplate
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
                .build();

        // 创建multipart/form-data请求体
        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();

        // 添加表单参数
        if (paramMap != null && !paramMap.isEmpty()) {
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                params.add(entry.getKey(), entry.getValue());
            }
        }

        // 添加音频文件
        if (promptWav != null) {
            Resource resource = new ClassPathResource(promptWav);
            params.add("prompt_wav", resource);
        }

        // 发送POST请求并获取响应
        ResponseEntity<byte[]> response = restTemplate.postForEntity(url, params, byte[].class);

        // 返回响应体字节数组
        return response.getBody();
    }

    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC)
                .setConnectionRequestTimeout(TIMEOUT_MSEC)
                .setSocketTimeout(TIMEOUT_MSEC).build();
    }

}