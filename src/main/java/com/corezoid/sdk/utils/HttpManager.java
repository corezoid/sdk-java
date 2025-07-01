package com.corezoid.sdk.utils;

import com.corezoid.sdk.entity.CorezoidMessage;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.nio.charset.StandardCharsets;

/**
 * Handles HTTP communication with the Corezoid platform.
 * <p>
 * This class manages HTTP connections to the Corezoid API, including connection pooling,
 * request execution, and response handling. It provides methods for sending messages
 * to the Corezoid API and processing the responses.
 * </p>
 * 
 * <p><strong>Thread Safety:</strong> This class is thread-safe and can be shared across
 * multiple threads. It's recommended to create a single instance and reuse it throughout
 * your application for optimal performance.</p>
 * 
 * <p><strong>Connection Pooling:</strong> Uses Apache HttpClient's connection pooling
 * for efficient connection reuse. The pool is automatically managed and cleaned up.</p>
 * 
 * <p><strong>Best Practices:</strong></p>
 * <ul>
 *   <li>Create one HttpManager instance per application, not per request</li>
 *   <li>Configure appropriate timeouts based on your network conditions</li>
 *   <li>Set maxConnections based on expected concurrent request volume</li>
 *   <li>Monitor connection pool usage in high-throughput scenarios</li>
 * </ul>
 *
 * @author Corezoid <support@corezoid.com>
 * @see CorezoidMessage for creating messages to send
 * @see HttpException for HTTP-related error handling
 */
public class HttpManager {
//----------------------------------------------------------------------------------------------------------------------

    private final CloseableHttpClient httpClient;
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Creates an HttpManager with custom connection pool and timeout settings.
     * <p>
     * This constructor allows you to configure the HTTP client for your specific
     * network conditions and performance requirements.
     * </p>
     *
     * @param maxCount Maximum total connections in the pool. This should be set based 
     *                 on your expected concurrent request volume. Default max connections 
     *                 per route is also set to this value.
     * @param connectionTimeout Connection establishment timeout in milliseconds. 
     *                         Time to wait when establishing a connection to the server.
     *                         Recommended: 3000-10000ms depending on network conditions.
     * @param answerTimeout Response timeout in milliseconds. Time to wait for the server 
     *                     to send a complete response after the connection is established.
     *                     Recommended: 10000-30000ms depending on expected response times.
     * 
     * @example
     * <pre>{@code
     * // For high-throughput applications
     * HttpManager highThroughput = new HttpManager(50, 5000, 15000);
     * 
     * // For slow networks
     * HttpManager slowNetwork = new HttpManager(10, 10000, 30000);
     * 
     * // For development/testing
     * HttpManager development = new HttpManager(5, 3000, 10000);
     * }</pre>
     */
    public HttpManager(int maxCount, int connectionTimeout, int answerTimeout) {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(maxCount);
        manager.setMaxTotal(maxCount);

        httpClient = HttpClients.custom()
                .setConnectionManager(manager)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(Timeout.ofMilliseconds(answerTimeout))
                        .build())
                .build();
    }

    //----------------------------------------------------------------------------------------------------------------------
    /**
     * Creates an HttpManager with default settings suitable for most applications.
     * <p>
     * Default configuration:
     * <ul>
     *   <li>Max connections: 15</li>
     *   <li>Connection timeout: 1000ms (1 second)</li>
     *   <li>Response timeout: 10000ms (10 seconds)</li>
     * </ul>
     * </p>
     * 
     * <p>These defaults work well for moderate-load applications. For high-throughput
     * or special network conditions, consider using the parameterized constructor.</p>
     * 
     * @see #HttpManager(int, int, int) for custom configuration
     */
    public HttpManager() {
        this(15, 1000, 10000);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Sends a CorezoidMessage to the Corezoid API and returns the response.
     * <p>
     * This method performs the actual HTTP POST request to the Corezoid API endpoint
     * specified in the message URL. It handles the complete request/response cycle
     * including connection management, error handling, and response parsing.
     * </p>
     * 
     * <p><strong>Error Handling:</strong> This method throws HttpException for various
     * failure scenarios including network errors, timeouts, and HTTP error responses.</p>
     *
     * @param message The CorezoidMessage containing the request URL, body, and signature
     * @return The raw JSON response string from the Corezoid API
     * @throws HttpException if the request fails due to network issues, timeouts, 
     *         HTTP error responses (4xx, 5xx), or other communication problems
     * 
     * @example
     * <pre>{@code
     * try {
     *     HttpManager httpManager = new HttpManager();
     *     CorezoidMessage message = CorezoidMessage.request(apiSecret, apiLogin, operations);
     *     
     *     String response = httpManager.send(message);
     *     Map<String, String> results = CorezoidMessage.parseAnswer(response);
     *     
     *     // Process results...
     *     
     * } catch (HttpException e) {
     *     // Handle network/HTTP errors
     *     logger.error("Failed to send request: " + e.getMessage(), e);
     * }
     * }</pre>
     * 
     * @see CorezoidMessage#request(String, String, List) for creating messages
     * @see CorezoidMessage#parseAnswer(String) for parsing responses
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
                body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                if (entity.getContentType() == null) {
                    body = new String(body.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                }
            }
            int code = response.getCode();
            if (code < 200 || code >= 300) {
                throw new Exception(String.format(" code : '%s' , body : '%s'", code, body));
            }
            return body;
        } catch (Exception ex) {
            try {
                throw new HttpException("Fail to send " + request.getMethod() + " request to url " + request.getUri() + ", " + ex.getMessage(), ex);
            } catch (java.net.URISyntaxException uriEx) {
                throw new HttpException("Invalid URI in request: " + uriEx.getMessage(), uriEx);
            }
        }
    }
//----------------------------------------------------------------------------------------------------------------------
}
