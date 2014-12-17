package com.middlewarebiz.conveyorapiutils.utils;

import com.middlewarebiz.conveyorapiutils.entity.ConveyorMessage;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.*;
import org.apache.http.util.EntityUtils;

import static org.apache.http.Consts.*;

/**
 * @author Middleware <support@middleware.biz>
 */
public class HttpManager {
//----------------------------------------------------------------------------------------------------------------------

    private final HttpClient httpClient;
//----------------------------------------------------------------------------------------------------------------------

    /**
     *
     * create PoolingClientConnectionManager
     *
     * @param maxCount max total connection, default max connection per route
     * @param connectionTimeout milliseconds
     * @param answerTimeout milliseconds
     */
    public HttpManager(int maxCount, int connectionTimeout, int answerTimeout) {
        PoolingClientConnectionManager manager = new PoolingClientConnectionManager();
        manager.setDefaultMaxPerRoute(maxCount);
        manager.setMaxTotal(maxCount);
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setSoTimeout(params, answerTimeout);

        httpClient = new DefaultHttpClient(manager, params);
    }

    //----------------------------------------------------------------------------------------------------------------------
    public HttpManager() {
        this(15, 1000, 10000);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * send request
     *
     * @param request - request to conveyor
     * @return
     * @throws org.apache.http.HttpException
     */
    public String send(ConveyorMessage request) throws HttpException {
        HttpPost post = new HttpPost(request.url);
        post.setEntity(new StringEntity(request.body, jsonUTF8));
        return sendBasic(post);
    }
//----------------------------------------------------------------------------------------------------------------------

    private String sendBasic(HttpRequestBase request) throws HttpException {
        try {
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String body = "";
            if (entity != null) {
                body = EntityUtils.toString(entity, UTF_8);
                if (entity.getContentType() == null) {
                    body = new String(body.getBytes(ISO_8859_1), UTF_8);
                }
            }
            int code = response.getStatusLine().getStatusCode();
            if (code < 200 || code >= 300) {
                throw new Exception(String.format(" code : '%s' , body : '%s'", code, body));
            }
            return body;
        } catch (Exception ex) {
            throw new HttpException("Fail to send " + request.getMethod() + " request to url " + request.getURI() + ", " + ex.getMessage(), ex);
        } finally {
            request.releaseConnection();
        }
    }
//----------------------------------------------------------------------------------------------------------------------
    private static final ContentType jsonUTF8 = ContentType.create("application/json", "UTF-8");
}
