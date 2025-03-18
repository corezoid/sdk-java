package com.corezoid.sdk.entity;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.*;
import java.util.HexFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Core message handling class for the Corezoid SDK.
 * <p>
 * This class is responsible for creating, signing, and verifying messages exchanged with the Corezoid platform.
 * It handles the creation of request messages with proper signatures, verification of incoming message signatures,
 * and parsing of response messages.
 * </p>
 *
 * @author Corezoid <support@corezoid.com>
 */
public final class CorezoidMessage {
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a response message from a list of response operations.
     * <p>
     * This method builds a JSON response containing the results of the operations.
     * </p>
     *
     * @param operations List of response operations to include in the message
     * @return JSON string containing the response message
     * @throws IllegalArgumentException if operations is null
     */
    public static String response(List<ResponseOperation> operations) {
        if (operations == null) {
            throw new IllegalArgumentException("operations is null");
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();
        obj.put(request_proc, ResultType.ok.name());
        ArrayNode jsOps = mapper.createArrayNode();
        for (ResponseOperation op : operations) {
            jsOps.add(op.toJSONObject());
        }
        obj.set(ops, jsOps);
        return obj.toString();
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a request message with the default host.
     * <p>
     * This method builds a signed request message containing the operations to be performed.
     * </p>
     *
     * @param apiSecret API secret key for signing the message
     * @param apiLogin API login identifier
     * @param operations List of request operations to include in the message
     * @return CorezoidMessage object containing the request message and signature
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public static CorezoidMessage request(String apiSecret, String apiLogin,
                                      List<RequestOperation> operations) {
        return request(host, apiSecret, apiLogin, operations);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a request message with a custom host.
     * <p>
     * This method builds a signed request message containing the operations to be performed,
     * using a custom host URL instead of the default one.
     * </p>
     *
     * @param host Custom host URL to use instead of the default
     * @param apiSecret API secret key for signing the message
     * @param apiLogin API login identifier
     * @param operations List of request operations to include in the message
     * @return CorezoidMessage object containing the request message and signature
     * @throws IllegalArgumentException if any parameter is null or empty
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
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();
        ArrayNode jsOps = mapper.createArrayNode();
        for (RequestOperation op : operations) {
            jsOps.add(op.toJSONObject());
        }
        obj.set(ops, jsOps);
        String content = obj.toString();
        String unixTime = String.valueOf(System.currentTimeMillis() / 1000);

        return new CorezoidMessage(content, unixTime, apiSecret, apiLogin, host);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Verifies the signature of a message.
     * <p>
     * This method checks if a received signature matches the calculated signature
     * for the given content, time, and API secret.
     * </p>
     *
     * @param sign The signature to verify (from the SIGNATURE query parameter)
     * @param apiSecret The API secret key used for signing
     * @param time The timestamp when the message was created (from the GMT_UNIXTIME query parameter)
     * @param content The message body content
     * @return true if the signature is valid, false otherwise
     */
    public static boolean checkSign(String sign, String apiSecret, String time,
                                    String content) {
        CorezoidMessage resivedSign = new CorezoidMessage(sign);
        CorezoidMessage calculatedSign = new CorezoidMessage(content, time, apiSecret, "");
        return resivedSign.equals(calculatedSign);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Parses a response from the Corezoid API into a map of reference-to-status pairs.
     * <p>
     * This method processes the JSON response from the Corezoid API and extracts
     * the status of each operation, indexed by the task reference.
     * </p>
     *
     * @param jsonString The JSON response string from the Corezoid API
     * @return A map where keys are task references and values are processing statuses
     * @throws Exception if the response indicates a failure or cannot be parsed
     */
    public static Map<String, String> parseAnswer(String jsonString) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = (ObjectNode) mapper.readTree(jsonString);
        String reqProc = obj.get(request_proc).asText();
        if (!reqProc.equals("ok")) {
            throw new Exception(String.format("Request processing failed, request_proc = %s ", reqProc));
        }
        ArrayNode operations = (ArrayNode) obj.get(ops);
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < operations.size(); i++) {
            ObjectNode op = (ObjectNode) operations.get(i);
            String opRef = op.get(ref).asText();
            String opProc = op.get(proc).asText();
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
            sha1hex = HexFormat.of().formatHex(sha1.digest(bytes)).toLowerCase();
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
    private static final String host = "https://www.corezoid.com";
    private static final String version = "1";
    private static final String format = "json";

    private enum ResultType {

        ok, fail
    }
//----------------------------------------------------------------------------------------------------------------------
}
// Dummy change for draft PR
