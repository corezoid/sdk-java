package com.corezoid.sdk.entity;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.*;
import javax.xml.bind.DatatypeConverter;

import net.sf.json.*;

/**
 * Message
 *
 * @author Corezoid <support@corezoid.com>
 */
public final class CorezoidMessage {
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Builder for response body
     *
     * @param operations
     * @return
     */
    public static String response(List<ResponseOperation> operations) {
        if (operations == null) {
            throw new IllegalArgumentException("operations is null");
        }
        JSONObject obj = new JSONObject();
        obj.element(request_proc, ResultType.ok);
        obj.element(ops, new JSONArray());
        JSONArray jsOps = new JSONArray();
        for (ResponseOperation op : operations) {
            jsOps.add(op.toJSONObject());
        }
        obj.element(ops, jsOps);
        String content = obj.toString();
        return content;
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Builder for request message
     */
    public static CorezoidMessage request(String apiSecret, String apiLogin,
                                      List<RequestOperation> operations) {
        return request(host, apiSecret, apiLogin, operations);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Builder for request message
     * @param host override default host value
     * @param apiSecret
     * @param apiLogin
     * @param operations
     * @return
     */
    public static CorezoidMessage request(String host, String apiSecret, String apiLogin,
                                          List<RequestOperation> operations) {
        if (apiSecret == null || apiSecret.equals("")) {
            throw new IllegalArgumentException("apiSecret is null or empty");
        }
        if (apiLogin == null || apiLogin.equals("")) {
            throw new IllegalArgumentException("apiLogin is null or empty");
        }
        if (operations == null) {
            throw new IllegalArgumentException("operations is null");
        }
        JSONObject obj = new JSONObject();
        obj.element(ops, new JSONArray());
        JSONArray jsOps = new JSONArray();
        for (RequestOperation op : operations) {
            jsOps.add(op.toJSONObject());
        }
        obj.element(ops, jsOps);
        String content = obj.toString();
        String unixTime = String.valueOf(System.currentTimeMillis() / 1000);

        return new CorezoidMessage(content, unixTime, apiSecret, apiLogin, host);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Check signature
     *
     * @param sign      - @QueruParam SIGNATURE from url
     * @param apiSecret - api secret key
     * @param time      - @QueruParam GMT_UNIXTIME from url
     * @param content   - request body
     * @return true if signature is valid, or false
     */
    public static boolean checkSign(String sign, String apiSecret, String time,
                                    String content) {
        CorezoidMessage resivedSign = new CorezoidMessage(sign);
        CorezoidMessage calculatedSign = new CorezoidMessage(content, time, apiSecret, "");
        return resivedSign.equals(calculatedSign);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Parse conveyor answer to ref-status map
     *
     * @param jsonString - request body
     * @return map "task referece"-"process status"
     * @throws Exception if packet processing was failed
     */
    public static Map<String, String> parseAnswer(String jsonString) throws Exception {
        JSONObject obj = parseJson(jsonString);
        String reqProc = obj.getString(request_proc);
        if (!reqProc.equals("ok")) {
            throw new Exception(String.format("Request processing failed, request_proc = %s ", reqProc));
        }
        JSONArray operations = obj.getJSONArray(ops);
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < operations.size(); i++) {
            JSONObject op = operations.getJSONObject(i);
            String opRef = op.getString(ref);
            String opProc = op.getString(proc);
            result.put(opRef, opProc);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------------------
    private CorezoidMessage(String body, String time, String apiSecret,
                            String apiLogin) {
        this(body, time, apiSecret, apiLogin, host);
    }

    //----------------------------------------------------------------------------------------------------------------------
    private CorezoidMessage(String body, String time, String apiSecret,
                            String apiLogin, String baseUri) {
        this.body = body;
        this.time = time;
        this.apiSecret = apiSecret;
        this.signCode = generateSign(time, apiSecret, body);
        this.url = new StringBuilder()
                .append(baseUri).append("/api/")
                .append(version).append(slash)
                .append(format).append(slash)
                .append(apiLogin).append(slash)
                .append(time).append(slash)
                .append(signCode).toString();
    }
//----------------------------------------------------------------------------------------------------------------------

    private CorezoidMessage(String signCode) {
        this.body = null;
        this.time = null;
        this.apiSecret = null;
        this.url = null;
        this.signCode = signCode;
    }
//----------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CorezoidMessage other = (CorezoidMessage) obj;
        return this.signCode.equals(other.signCode);
    }
//----------------------------------------------------------------------------------------------------------------------

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.body != null ? this.body.hashCode() : 0);
        hash = 89 * hash + (this.time != null ? this.time.hashCode() : 0);
        hash = 89 * hash + (this.apiSecret != null ? this.apiSecret.hashCode() : 0);
        hash = 89 * hash + (this.signCode != null ? this.signCode.hashCode() : 0);
        hash = 89 * hash + (this.url != null ? this.url.hashCode() : 0);
        return hash;
    }

//----------------------------------------------------------------------------------------------------------------------

    /**
     * Genarate signature {SIGNATURE} = hex( sha1({GMT_UNIXTIME} + {API_SECRET}
     * + {CONTENT} + {API_SECRET}) )
     *
     * @param time      - time
     * @param apiSecret - apiSecret
     * @param body      - request body
     * @return - signature
     */
    private static String generateSign(String time, String apiSecret,
                                       String body) {
        MessageDigest sha1 = messageDigest.get();
        sha1.reset();
        byte[] bytes;
        String sha1hex;
        try {
            bytes = (time + apiSecret + body + apiSecret).getBytes("UTF-8");
            sha1hex = DatatypeConverter.printHexBinary(sha1.digest(bytes)).toLowerCase();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("generateSign error", e);
        }
        return sha1hex;
    }

    //----------------------------------------------------------------------------------------------------------------------
    private static final ThreadLocal<MessageDigest> messageDigest = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException("MessageDigest init error", ex);
            }
        }
    };

    //----------------------------------------------------------------------------------------------------------------------
    private static JSONObject parseJson(Object content) throws Exception {
        try {
            return (JSONObject) JSONSerializer.toJSON(content);
        } catch (Exception ex) {
            throw new Exception(String.format("json parsing error. %s", ex.getMessage()));
        }
    }

    //----------------------------------------------------------------------------------------------------------------------
    public final String body;
    public final String url;
    private final String time;
    private final String apiSecret;
    private final String signCode;
    private static final String ops = "ops";
    private static final String request_proc = "request_proc";
    private static final String proc = "proc";
    private static final String ref = "ref";
    private static final String slash = "/";
    private static final String host = "https://api.corezoid.com";
    private static final String version = "1";
    private static final String format = "json";

    private enum ResultType {

        ok, fail
    }
//----------------------------------------------------------------------------------------------------------------------
}
