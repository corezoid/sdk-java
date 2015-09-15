package com.corezoid.sdk.test;

import com.corezoid.sdk.entity.ResponseOperation;
import com.corezoid.sdk.entity.CorezoidMessage;
import com.corezoid.sdk.entity.RequestOperation;
import java.util.*;
import junit.framework.TestCase;

import static junit.framework.Assert.assertEquals;
import net.sf.json.JSONObject;

/**
 *
 * @author Corezoid <support@corezoid.com>
 */
public class UtiisTest extends TestCase {

    public UtiisTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Valid signature check test
     */
    public void testCheckSignTrue() {
        String sign = "e01f19285c09c8eff449a60fef9dbc3c8f3915db";
        String key = "tgGsR8FTuLXzzVBsp3rngeSgOB2E2pMFJDwAXarbrs92GmLiiy";
        String time = "1389956320";
        String content = "{\n"
                + "   \"ops\":[\n"
                + "      {\n"
                + "         \"ref\":null,\n"
                + "         \"type\":\"data\",\n"
                + "         \"obj_id\":\"52d8d74610e87b10d98c8d90\",\n"
                + "         \"conv_id\":\"270\",\n"
                + "         \"node_id\":\"52d837d210e87b10d979e98c\",\n"
                + "         \"data\":{\n"
                + "            \"id\":1077413\n"
                + "         },\n"
                + "         \"extra\":{\n"
                + "            \"id\":\"1412737\",\n"
                + "            \"hide_products\":\"N\",\n"
                + "            \"properties\":\"Y\",\n"
                + "            \"show_all\":\"Y\",\n"
                + "            \"codeProduct\" : \"CRED\" \n"
                + "         }\n"
                + "      }\n"
                + "   ]\n"
                + "}";
        boolean expResult = true;
        boolean result = CorezoidMessage.checkSign(sign, key, time, content);
        assertEquals(expResult, result);
        System.out.println(">>checkSignTrue :\t" + result);

    }

    /**
     * Invalid signature check test
     */
    public void testCheckSignFalse() {
        String sign = "1";
        String key = "tgGsR8FTuLXzzVBsp3rngeSgOB2E2pMFJDwAXarbrs92GmLiiy";
        String time = "1389956320";
        String content = "{\n"
                + "   \"ops\":[\n"
                + "      {\n"
                + "         \"ref\":null,\n"
                + "         \"type\":\"data\",\n"
                + "         \"obj_id\":\"52d8d74610e87b10d98c8d90\",\n"
                + "         \"conv_id\":\"270\",\n"
                + "         \"node_id\":\"52d837d210e87b10d979e98c\",\n"
                + "         \"data\":{\n"
                + "            \"id\":1077413\n"
                + "         },\n"
                + "         \"extra\":{\n"
                + "            \"id\":\"1412737\",\n"
                + "            \"hide_products\":\"N\",\n"
                + "            \"properties\":\"Y\",\n"
                + "            \"show_all\":\"Y\",\n"
                + "            \"codeProduct\" : \"CRED\" \n"
                + "         }\n"
                + "      }\n"
                + "   ]\n"
                + "}";
        boolean expResult = false;
        boolean result = CorezoidMessage.checkSign(sign, key, time, content);
        assertEquals(expResult, result);
        System.out.println(">>checkSignFalse :\t" + result);

    }

    /**
     * Parse conveyor answer test
     *
     * @throws Exception
     */
    public void testCheckAnswerReturnsNotEmptyMap() throws Exception {
        String answerOk = "{\n"
                + "    \"request_proc\":\"ok\",\n"
                + "    \"ops\":[\n"
                + "   {\"id\":\"\",\"proc\":\"ok\",\"obj\":\"task\",\"ref\":\"PB11345969838\",\"obj_id\":\"52e266a05f845f6ec484d611\"}\n"
                + "    ]\n"
                + "}";
        Map<String, String> map = CorezoidMessage.parseAnswer(answerOk);
        assertEquals(true, !map.isEmpty());
        System.out.println(">>testCheckAnswerReturnsNotEmptyMap :\t" + map.toString());

    }

    /**
     * Parsing conveyor answer test throws exception
     *
     * @throws Exception
     */
    public void testcheckAnswerShouldThrowException() throws Exception {
        String answerFail = "{\n"
                + "    \"request_proc\":\"fail\",\n"
                + "    \"ops\":[\n"
                + "    {\"id\":\"\",\"proc\":\"ok\",\"obj\":\"task\",\"ref\":\"PB11345969838\",\"obj_id\":\"52e266a05f845f6ec484d611\"}\n"
                + "    ]\n"
                + "}";
        try {
            Map<String, String> map = CorezoidMessage.parseAnswer(answerFail);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testcheckAnswerShouldThrowException :\t" + ex.getMessage());
        }

    }

    /**
     * Operation builder exceptions test
     */
    public void testOperationBuilderShouldThrowEsception() {
        JSONObject res_data = new JSONObject()
                .element("id", "1")
                .element("test", "2");
        String conv_id = "11";
        String ref = "22";
        // response
        try {
            ResponseOperation.ok(null, ref, res_data);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testResponseOperationBuilderShouldThrowEsception :\t" + ex.getMessage());
        }
        try {
            ResponseOperation.ok("", ref, res_data);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testResponseOperationBuilderShouldThrowEsception :\t" + ex.getMessage());
        }
        try {
            ResponseOperation.ok(conv_id, null, res_data);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testResponseOperationBuilderShouldThrowEsception :\t" + ex.getMessage());
        }
        try {
            ResponseOperation.ok(conv_id, "", res_data);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testResponseOperationBuilderShouldThrowEsception :\t" + ex.getMessage());
        }
        try {
            ResponseOperation.ok(conv_id, ref, null);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testResponseOperationBuilderShouldThrowEsception :\t" + ex.getMessage());
        }
        // request
        try {
            RequestOperation.create(null, ref, res_data);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testRequestOperationBuilderShouldThrowEsception :\t" + ex.getMessage());
        }
        try {
            RequestOperation.create("", ref, res_data);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testRequestOperationBuilderShouldThrowEsception :\t" + ex.getMessage());
        }
        try {
            RequestOperation.create(conv_id, null, res_data);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testRequestOperationBuilderShouldThrowEsception :\t" + ex.getMessage());
        }
        try {
            RequestOperation.create(conv_id, "", res_data);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testRequestOperationBuilderShouldThrowEsception :\t" + ex.getMessage());
        }
        try {
            RequestOperation.create(conv_id, ref, null);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testRequestOperationBuilderShouldThrowEsception :\t" + ex.getMessage());
        }

    }

    /**
     * Answer builder test
     */
    public void testGetConAnswerMessage() {

        JSONObject res_data = new JSONObject()
                .element("id", "1")
                .element("test", "2");
        String conv_id = "11";
        String ref = "22";
        String body = CorezoidMessage.response(Arrays.asList(ResponseOperation.ok(conv_id, ref, res_data)));
        System.out.println(">>testGetConAnswerMessage :");
        System.out.println("body \t\t" + body);
        String expBody = "{\"request_proc\":\"ok\",\"ops\":[{\"ref\":\"22\",\"conv_id\":\"11\",\"proc\":\"ok\",\"res_data\":{\"id\":\"1\",\"test\":\"2\"}}]}";
        assertEquals(expBody, body);

    }

    /**
     * Requst builder tests
     */
    public void testGetConvQueryCreateMessage() {
        JSONObject data = new JSONObject()
                .element("phone", "1")
                .element("card", "2");
        String ref = "11";
        String conv_id = "1234";
        String key = "123";
        String apiLogin = "12345";
        CorezoidMessage mes = CorezoidMessage.request(key, apiLogin, Arrays.asList(RequestOperation.create(conv_id, ref, data)));
        System.out.println(">>testGetConvQueryCreateMessage :");
        System.out.println("body \t\t" + mes.body);
        System.out.println("url \t\t " + mes.url);
        String expBody = "{\"ops\":[{\"ref\":\"11\",\"conv_id\":\"1234\",\"type\":\"create\",\"obj\":\"task\",\"data\":{\"phone\":\"1\",\"card\":\"2\"}}]}";
        assertEquals(expBody, mes.body);

    }
    /**
     * Requst builder tests
     */
    public void testGetConvQueryModifyRefMessage() {
        JSONObject data = new JSONObject()
                .element("phone", "1")
                .element("card", "2");
        String ref = "11";
        String conv_id = "1234";
        String key = "123";
        String apiLogin = "12345";
        CorezoidMessage mes = CorezoidMessage.request(key, apiLogin, Arrays.asList(RequestOperation.modifyRef(conv_id, ref, data)));
        System.out.println(">>testGetConvQueryModifyRefMessage :");
        System.out.println("body \t\t" + mes.body);
        System.out.println("url \t\t " + mes.url);
        String expBody = "{\"ops\":[{\"ref\":\"11\",\"conv_id\":\"1234\",\"type\":\"modify\",\"obj\":\"task\",\"data\":{\"phone\":\"1\",\"card\":\"2\"}}]}";
        assertEquals(expBody, mes.body);

    }
    /**
     * Requst builder tests
     */
    public void testGetConvQueryModifyIdMessage() {
        JSONObject data = new JSONObject()
                .element("phone", "1")
                .element("card", "2");
        String taskId = "11";
        String conv_id = "1234";
        String key = "123";
        String apiLogin = "12345";
        CorezoidMessage mes = CorezoidMessage.request(key, apiLogin, Arrays.asList(RequestOperation.modifyId(conv_id, taskId, data)));
        System.out.println(">>testGetConvQueryModifyRefMessage :");
        System.out.println("body \t\t" + mes.body);
        System.out.println("url \t\t " + mes.url);
        String expBody = "{\"ops\":[{\"obj_id\":\"11\",\"conv_id\":\"1234\",\"type\":\"modify\",\"obj\":\"task\",\"data\":{\"phone\":\"1\",\"card\":\"2\"}}]}";
        assertEquals(expBody, mes.body);

    }
    /**
     * MessageBuilder Exceptions test
     */
    public void testConveyorQueryShouldThrowException() {
        JSONObject data = new JSONObject()
                .element("phone", "1")
                .element("card", "2");
        String ref = "11";
        String conv_id = "1234";
        String apiSecret = "1234";
        String apiLogin = "12345";
        List<RequestOperation> operations = Arrays.asList(RequestOperation.create(conv_id, ref, data));
        try {
            CorezoidMessage mes = CorezoidMessage.request(null, apiLogin, operations);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testConveyorQueryBuilderShouldThrowException :\t" + ex.getMessage());
        }
        try {
            CorezoidMessage mes = CorezoidMessage.request("", apiLogin, operations);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testConveyorQueryBuilderShouldThrowException :\t" + ex.getMessage());
        }
        try {
            CorezoidMessage mes = CorezoidMessage.request(apiSecret, null, operations);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testConveyorQueryBuilderShouldThrowException :\t" + ex.getMessage());
        }
        try {
            CorezoidMessage mes = CorezoidMessage.request(apiSecret, "", operations);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testConveyorQueryBuilderShouldThrowException :\t" + ex.getMessage());
        }
        try {
            CorezoidMessage mes = CorezoidMessage.request(apiSecret, apiLogin, null);
            fail("Exception was expected");
        } catch (Exception ex) {
            System.out.println(">>testConveyorQueryBuilderShouldThrowException :\t" + ex.getMessage());
        }
    }
    public void testRequestOperationShouldThrowException(){
        JSONObject data = new JSONObject()
                .element("phone", "1")
                .element("card", "2");
        String ref = "11";
        String taskId = "12";
        String conv_id = "1234";
        String apiSecret = "1234";
        String apiLogin = "12345";
        RequestOperation op = null;
        try {
            op = RequestOperation.modifyId(conv_id, null, data);
        }catch (Exception ex){
            System.out.println(">>testRequestOperationShouldThrowException :\t" + ex.getMessage());
        }
        try {
            op = RequestOperation.modifyId(conv_id, "", data);
        }catch (Exception ex){
            System.out.println(">>testRequestOperationShouldThrowException :\t" + ex.getMessage());
        }
        try {
            op = RequestOperation.modifyRef(conv_id, null, data);
        }catch (Exception ex){
            System.out.println(">>testRequestOperationShouldThrowException :\t" + ex.getMessage());
        }
        try {
            op = RequestOperation.modifyRef(conv_id, "", data);
        }catch (Exception ex){
            System.out.println(">>testRequestOperationShouldThrowException :\t" + ex.getMessage());
        }
    }
}
