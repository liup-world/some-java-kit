package com.bobsystem.exercise.commons;

import org.apache.commons.lang.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URI;
import java.security.KeyStore;
import java.util.Arrays;

/*
 * 使用 HTTP 连接池 创建 GET、POST 请求
 */
public class HttpKit {

    //region CONSTANT
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpKit.class);

    private static final String CHARSET_DEFAULT = "utf-8";

    private static final HttpClientBuilder HTTP_CLIENT_BUILDER_POOLING;
    private static final PoolingHttpClientConnectionManager HTTP_CONN_MANAGER;

    private static final int HTTP_CONN_MANAGER_INACTIVITY = 2000;
    private static final int HTTP_CONN_MANAGER_MAX_PER_ROUTE = 50;

    private static final int BUFFER_SIZE = 4096;

    static {
        HTTP_CONN_MANAGER = new PoolingHttpClientConnectionManager();
        HTTP_CONN_MANAGER.setMaxTotal(SystemParameter.httpMaxTotal);
        HTTP_CONN_MANAGER.setValidateAfterInactivity(HTTP_CONN_MANAGER_INACTIVITY);
        HTTP_CONN_MANAGER.setDefaultMaxPerRoute(HTTP_CONN_MANAGER_MAX_PER_ROUTE);

        HttpClientBuilder builder = HttpClients.custom();
        HTTP_CLIENT_BUILDER_POOLING = builder.setConnectionManager(HTTP_CONN_MANAGER);
    }
    //endregion

    //region HTTP request
    //region get request
    public static String getRequest(String url) {
        return getRequest(url, CHARSET_DEFAULT);
    }

    public static String getRequest(String url, String charset) {
        return getRequest(new HttpGet(url), charset);
    }

    public static String getRequest(HttpGet getReq) {
        return getRequest(getReq, CHARSET_DEFAULT);
    }

    public static String getRequest(HttpGet getReq, String charset) {
        CloseableHttpClient httpClient;
        CloseableHttpResponse response = null;
        try {
            httpClient = buildHttpClient();
            response = httpClient.execute(getReq);
            HttpEntity entity = response.getEntity();
            if(entity != null) {
                return EntityUtils.toString(entity, charset);
            }
        }
        catch (Exception ex) {
            LOGGER.error(getReq.getURI().getPath(), ex);
        }
        finally {
            //region response.close();
            if (response != null) {
                try {
                    response.close();
                }
                catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            //endregion
            //region //httpClient.close(); 注释原因：关闭 HttpClient 会关闭整个池
            //if (httpClient != null) {
            //    try {
            //        httpClient.close();
            //    }
            //    catch (Exception ex) {
            //        LOGGER.error(ex.getMessage(), ex);
            //    }
            //}
            //endregion
            //region getReq.releaseConnection();
            if (getReq != null) {
                getReq.releaseConnection();
            }
            //endregion
        }
        return "";
    }
    //endregion

    //region post request
    public static String postRequest(String url, String data) {
        return postRequest(url, data, CHARSET_DEFAULT);
    }

    public static String postRequest(String url, String data, String charset) {
        return postRequest(new HttpPost(url), data, charset);
    }

    public static String postRequest(HttpPost postReq, String data) {
        return postRequest(postReq, data, CHARSET_DEFAULT);
    }

    public static String postRequest(HttpPost postReq, String data, String charset) {
        return postRequest(postReq, buildHttpClient(), data, charset);
    }

    public static String postRequest(HttpPost postReq, CloseableHttpClient httpClient,
                                     String data, String charset) {
        CloseableHttpResponse response = null;
        try {
            if (StringUtils.isNotBlank(data)) {
                postReq.setEntity(new StringEntity(data, charset));
            }
            response = httpClient.execute(postReq);
            return EntityUtils.toString(response.getEntity(), charset);
        }
        catch (Exception ex) {
            LOGGER.error(postReq.getURI().getPath(), ex);
        }
        finally {
            //region response.close();
            if(response != null) {
                try {
                    response.close();
                }
                catch (IOException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            //endregion
            //region httpClient.close(); 注释原因：关闭 HttpClient 会关闭整个池
            //if (httpClient != null) {
            //    try {
            //        httpClient.close();
            //    }
            //    catch (Exception ex) {
            //        LOGGER.error(ex.getMessage(), ex);
            //    }
            //}
            //endregion
            //region postReq.releaseConnection();
            if (postReq != null) {
                postReq.releaseConnection();
            }
            //endregion
        }
        return "";
    }
    //endregion
    //endregion

    //region HTTP download
    // @return 记得关闭 数据流
    public static InputStream download(String url) {
        return download(new HttpGet(url));
    }

    // @return 记得关闭 数据流
    public static InputStream download(HttpGet getReq) {
        CloseableHttpClient httpClient;
        CloseableHttpResponse response = null;
        try {
            httpClient = buildHttpClient();
            response = httpClient.execute(getReq);
            HttpEntity entity = response.getEntity();
            if (entity == null) return null;
            return entity.getContent();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        finally {
            //region response.close();
            if (response != null) {
                try {
                    response.close();
                }
                catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            //endregion
            //region //httpClient.close(); 注释原因：关闭 HttpClient 会关闭整个池
            //if (httpClient != null) {
            //    try {
            //        httpClient.close();
            //    }
            //    catch (Exception ex) {
            //        LOGGER.error(ex.getMessage(), ex);
            //    }
            //}
            //endregion
            //region getReq.releaseConnection();
            if (getReq != null) {
                getReq.releaseConnection();
            }
            //endregion
        }
        return null;
    }

    public static boolean download(String url, String destPath) {
        return download(new HttpGet(url), destPath);
    }

    public static boolean download(String url, File file) {
        return download(new HttpGet(url), file);
    }

    public static boolean download(HttpGet getReq, String destPath) {
        return download(getReq, new File(destPath));
    }

    public static boolean download(HttpGet getReq, File destFile) {
        CloseableHttpClient httpClient;
        CloseableHttpResponse response = null;
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            httpClient = buildHttpClient();
            response = httpClient.execute(getReq);
            HttpEntity entity = response.getEntity();
            if (entity == null) return false;
            //ContentType contentType = ContentType.get(entity);
            //String extension;
            //String mime = contentType.getMimeType();
            //if ("image/jpeg".equals(mime)) { }
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            inStream = entity.getContent();
            outStream = new FileOutputStream(destFile);
            while ((length = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, length);
                Arrays.fill(buffer, 0, length, (byte)0);
            }
            return true;
        }
        catch (Exception ex) {
            LOGGER.error(getReq.getURI().getPath(), ex);
        }
        finally {
            //region inStream.close();
            if (inStream != null) {
                try {
                    inStream.close();
                }
                catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            //endregion
            //region outStream.close();
            if (outStream != null) {
                try {
                    outStream.close();
                }
                catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            //endregion
            //region response.close();
            if (response != null) {
                try {
                    response.close();
                }
                catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            //endregion
            //region //httpClient.close(); 注释原因：关闭 HttpClient 会关闭整个池
            //if (httpClient != null) {
            //    try {
            //        httpClient.close();
            //    }
            //    catch (Exception ex) {
            //        LOGGER.error(ex.getMessage(), ex);
            //    }
            //}
            //endregion
            //region getReq.releaseConnection();
            if (getReq != null) {
                getReq.releaseConnection();
            }
            //endregion
        }
        return false;
    }
    //endregion

    //region HTTP upload
    public static String upload(String url, String partName, File file) {
        return upload(new HttpPost(url), partName, file, "utf-8");
    }

    public static String upload(String url, String partName, File file, String charset) {
        return upload(new HttpPost(url), partName, file, charset);
    }

    public static String upload(HttpPost postReq, String partName, File file) {
        return upload(postReq, partName, file, "utf-8");
    }

    public static String upload(HttpPost postReq, String partName, File file,
                                String charset) {
        if (!file.exists()) return "";
        CloseableHttpClient httpClient;
        CloseableHttpResponse response = null;
        try {
            //region http entity
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addBinaryBody(partName, file);
            postReq.setEntity(entityBuilder.build());
            httpClient = buildHttpClient();
            //endregion
            response = httpClient.execute(postReq);
            //region process response
            HttpEntity entity = response.getEntity();
            if (entity == null) return "";
            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) return "";
            //endregion
            String result = EntityUtils.toString(entity, charset);
            if (StringUtils.isBlank(result)) result = "ok";
            return result;
        }
        catch (Exception ex) {
            LOGGER.error(postReq.getURI().getPath(), ex);
        }
        finally {
            //region response.close();
            if (response != null) {
                try {
                    response.close();
                }
                catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            //endregion
            //region //httpClient.close(); 注释原因：关闭 HttpClient 会关闭整个池
            //if (httpClient != null) {
            //    try {
            //        httpClient.close();
            //    }
            //    catch (Exception ex) {
            //        LOGGER.error(ex.getMessage(), ex);
            //    }
            //}
            //endregion
            //region postReq.releaseConnection();
            if (postReq != null) {
                postReq.releaseConnection();
            }
            //endregion
        }
        return "";
    }
    //endregion

    //region get http client
    public static CloseableHttpClient buildHttpClient() {
        return HTTP_CLIENT_BUILDER_POOLING.build();
    }

    public static CloseableHttpClient buildSSLHttpClient(URI certUri, String password) {
        InputStream inStream = null;
        SSLConnectionSocketFactory sslSocketFactory;
        try {
            inStream = new FileInputStream(new File(certUri));
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(inStream, password.toCharArray());
            // build a ssl context
            SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, password.toCharArray()) // 这里也是写密码的
                .build();
            // get ssl connection factory
            sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext, new String[] { "TLSv1" }, null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            HttpClientBuilder builder = HttpClients.custom();
            return builder.setSSLSocketFactory(sslSocketFactory).build();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        finally {
            //region inStream.close();
            try {
                if (inStream != null) {
                    inStream.close();
                }
            }
            catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            //endregion
        }
        return null;
    }
    //endregion

    public static void shutdown() {
        HTTP_CONN_MANAGER.shutdown();
    }
}
