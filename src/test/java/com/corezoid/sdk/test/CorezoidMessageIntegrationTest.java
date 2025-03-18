import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.corezoid.sdk.entity.CorezoidMessage;
import com.corezoid.sdk.entity.RequestOperation;
import com.corezoid.sdk.entity.ResponseOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class CorezoidMessageIntegrationTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testRequestResponseIntegration() throws Exception {
        // Create test data
        ObjectNode taskData = mapper.createObjectNode();
        taskData.put("task_key", "test_value");
        
        // Create request operations
        RequestOperation createOp = RequestOperation.create("123", "ref-create", taskData);
        RequestOperation modifyOp = RequestOperation.modifyRef("123", "ref-modify", taskData);
        
        // Create and verify request message
        CorezoidMessage requestMsg = CorezoidMessage.request("test_secret", "test_login", 
            Arrays.asList(createOp, modifyOp));
        
        assertNotNull(requestMsg);
        assertTrue(requestMsg.body.contains("test_value"));
        assertTrue(requestMsg.body.contains("ref-create"));
        assertTrue(requestMsg.body.contains("ref-modify"));
        
        // Create and verify response message
        ObjectNode responseData = mapper.createObjectNode();
        responseData.put("status", "completed");
        
        ResponseOperation okResponse = ResponseOperation.ok("123", "ref-create", responseData);
        String responseContent = CorezoidMessage.response(Collections.singletonList(okResponse));
        
        assertNotNull(responseContent);
        assertTrue(responseContent.contains("completed"));
        assertTrue(responseContent.contains("ref-create"));
        
        // Test response parsing
        Map<String, String> parsedResponse = CorezoidMessage.parseAnswer(
            "{\"request_proc\":\"ok\",\"ops\":[{\"ref\":\"ref-create\",\"proc\":\"ok\"}]}");
        assertEquals("ok", parsedResponse.get("ref-create"));
    }

    @Test
    public void testMessageSigning() {
        String apiSecret = "test_secret";
        String apiLogin = "test_login";
        RequestOperation operation = RequestOperation.create("123", "ref-test", 
            mapper.createObjectNode().put("key", "value"));
        
        CorezoidMessage message = CorezoidMessage.request(apiSecret, apiLogin, 
            Collections.singletonList(operation));
        
        assertNotNull(message.sign);
        assertTrue(message.sign.length() > 0);
        
        // Verify signature checking
        assertTrue(CorezoidMessage.checkSign(
            message.sign,
            apiSecret,
            String.valueOf(System.currentTimeMillis() / 1000),
            message.body
        ));
    }

    @Test
    public void testErrorHandling() {
        // Test null parameters
        assertThrows(IllegalArgumentException.class, () ->
            CorezoidMessage.request(null, "login", Collections.emptyList()));
            
        assertThrows(IllegalArgumentException.class, () ->
            CorezoidMessage.request("secret", null, Collections.emptyList()));
            
        assertThrows(IllegalArgumentException.class, () ->
            CorezoidMessage.request("secret", "login", null));
            
        // Test invalid response parsing
        assertThrows(Exception.class, () ->
            CorezoidMessage.parseAnswer("{\"request_proc\":\"error\"}"));
            
        // Test empty response
        assertThrows(Exception.class, () ->
            CorezoidMessage.parseAnswer("{}"));
    }
}
