package com.corezoid.sdk.utils;

import com.corezoid.sdk.entity.CorezoidMessage;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import static org.apache.http.Consts.*;

/**
 * @author Corezoid <support@corezoid.com>
 */
public class HttpManager {
//----------------------------------------------------------------------------------------------------------------------

    private final CloseableHttpClient httpClient;
//----------------------------------------------------------------------------------------------------------------------

    /**
     *
     * create PoolingClientConnectionManager.
     *
     * @param maxCount max total connection, default max connection per route
     * @param connectionTimeout milliseconds
     * @param answerTimeout milliseconds
     */
    public HttpManager(int maxCount, int connectionTimeout, int answerTimeout) {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(maxCount);
        manager.setMaxTotal(maxCount);

        httpClient = HttpClients.custom()
                .setConnectionManager(manager)
                .setConnectionTimeToLive(TimeValue.ofMilliseconds(connectionTimeout))
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(Timeout.ofMilliseconds(answerTimeout))
                        .build())
                .build();
    }

    //----------------------------------------------------------------------------------------------------------------------
    public HttpManager() {
        this(15, 1000, 10000);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * send request
     *
     * @param message -  corezoid message
     * @return
     * @throws org.apache.http.HttpException
     */
    public String send(CorezoidMessage message) throws HttpException {
        HttpPost post = new HttpPost(message.url);
        post.setEntity(new StringEntity(message.body, ContentType.APPLICATION_JSON));
        return sendBasic(post);
    }
//----------------------------------------------------------------------------------------------------------------------

    private String sendBasic(HttpUriRequestBase request) throws HttpException {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            String body = "";
            if (entity != null) {
                body = EntityUtils.toString(entity, UTF_8);
                if (entity.getContentType() == null) {
                    body = new String(body.getBytes(ISO_8859_1), UTF_8);
                }
            }
            int code = response.getCode();
            if (code < 200 || code >= 300) {
                throw new Exception(String.format(" code : '%s' , body : '%s'", code, body));
            }
            return body;
        } catch (Exception ex) {
            throw new HttpException("Fail to send " + request.getMethod() + " request to url " + request.getUri() + ", " + ex.getMessage(), ex);
        }
    }
//----------------------------------------------------------------------------------------------------------------------
    private static final ContentType jsonUTF8 = ContentType.create("application/json", "UTF-8");
}
