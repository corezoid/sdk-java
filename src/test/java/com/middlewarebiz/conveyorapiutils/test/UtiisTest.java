
package com.middlewarebiz.conveyorapiutils.test;

import com.middlewarebiz.conveyorapiutils.entity.ResponseOperation;
import com.middlewarebiz.conveyorapiutils.entity.ConveyorMessage;
import com.middlewarebiz.conveyorapiutils.entity.RequestOperation;
import java.util.*;
import junit.framework.TestCase;

import static junit.framework.Assert.assertEquals;

/**
 *
 * @author dn300986zav
 */
public class UtiisTest extends TestCase {
    public UtiisTest( String testName ) {
        super( testName );
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
        boolean result = ConveyorMessage.checkSign( sign, key, time, content );
        assertEquals( expResult, result );
        System.out.println( ">>checkSignTrue :\t" + result );

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
        boolean result = ConveyorMessage.checkSign( sign, key, time, content );
        assertEquals( expResult, result );
        System.out.println( ">>checkSignFalse :\t" + result );

    }

    /**
     * Parse conveyor answer  test
     * @throws Exception
     */
    public void testCheckAnswerReturnsNotEmptyMap() throws Exception {
        String answerOk = "{\n"
                + "    \"request_proc\":\"ok\",\n"
                + "    \"ops\":[\n"
                + "   {\"id\":\"\",\"proc\":\"ok\",\"obj\":\"task\",\"ref\":\"PB11345969838\",\"obj_id\":\"52e266a05f845f6ec484d611\"}\n"
                + "    ]\n"
                + "}";
        Map<String, String> map = ConveyorMessage.parseAnswer( answerOk );
        assertEquals( true, !map.isEmpty() );
        System.out.println( ">>testCheckAnswerReturnsNotEmptyMap :\t" + map.toString() );

    }

    /**
     * Parsing conveyor answer  test throws exception
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
            Map<String, String> map = ConveyorMessage.parseAnswer( answerFail );
            fail( "Exception was expected" );
        } catch ( Exception ex ) {
            System.out.println( ">>testcheckAnswerShouldThrowException :\t" + ex.getMessage() );
        }

    }

    /**
     * Answer builder test
     */
    public void testGetConAnswerMessage() {

        Map<String, Object> res_data = new LinkedHashMap<String, Object>();
        res_data.put( "id", "1" );
        res_data.put( "test", "2" );
        String conv_id = "11";
        String ref = "22";
        String body = ConveyorMessage.response( Arrays.asList( ResponseOperation.ok( conv_id, ref, res_data ) ) );
        System.out.println( ">>testGetConAnswerMessage :" );
        System.out.println( "body \t\t" + body );
        String expBody = "{\"request_proc\":\"ok\",\"ops\":[{\"ref\":\"22\",\"conv_id\":\"11\",\"proc\":\"ok\",\"res_data\":{\"id\":\"1\",\"test\":\"2\"}}]}";
        assertEquals( expBody, body );

    }

    /**
     * Requst builder test
     */
    public void testGetConvQueryMessage() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put( "phone", "1" );
        data.put( "card", "2" );
        String ref = "11";
        String conv_id = "1234";
        String key = "123";
        ConveyorMessage mes = ConveyorMessage.request( key, Arrays.asList( RequestOperation.create( conv_id, ref, data ) ) );
        System.out.println( ">>testGetConvQueryMessage :" );
        System.out.println( "key \t\t" + mes.key );
        System.out.println( "time \t\t" + mes.time );
        System.out.println( "sign \t\t" + mes.signCode );
        System.out.println( "body \t\t" + mes.body );
        String expBody = "{\"ops\":[{\"ref\":\"11\",\"conv_id\":\"1234\",\"type\":\"create\",\"obj\":\"task\",\"data\":{\"phone\":\"1\",\"card\":\"2\"}}]}";
        assertEquals( expBody, mes.body );


    }
}
