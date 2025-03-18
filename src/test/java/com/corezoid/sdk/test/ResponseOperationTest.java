import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.corezoid.sdk.entity.ResponseOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ResponseOperationTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testOkOperation() {
        ObjectNode data = mapper.createObjectNode();
        data.put("result", "success");
        
        ResponseOperation operation = ResponseOperation.ok("123", "ref-123", data);
        ObjectNode json = operation.toJSONObject();
        
        assertEquals("123", json.get("conv_id").asText());
        assertEquals("ref-123", json.get("ref").asText());
        assertEquals("ok", json.get("proc").asText());
        assertEquals("success", json.get("res_data").get("result").asText());
    }

    @Test
    public void testFailOperation() {
        ObjectNode data = mapper.createObjectNode();
        data.put("error", "validation failed");
        
        ResponseOperation operation = ResponseOperation.fail("123", "ref-123", data);
        ObjectNode json = operation.toJSONObject();
        
        assertEquals("123", json.get("conv_id").asText());
        assertEquals("ref-123", json.get("ref").asText());
        assertEquals("fail", json.get("proc").asText());
        assertEquals("validation failed", json.get("res_data").get("error").asText());
    }

    @Test
    public void testInvalidInputs() {
        ObjectNode data = mapper.createObjectNode();
        
        assertThrows(IllegalArgumentException.class, () -> 
            ResponseOperation.ok(null, "ref", data));
        
        assertThrows(IllegalArgumentException.class, () -> 
            ResponseOperation.ok("123", null, data));
            
        assertThrows(IllegalArgumentException.class, () -> 
            ResponseOperation.ok("123", "ref", null));
            
        assertThrows(IllegalArgumentException.class, () -> 
            ResponseOperation.fail("", "ref", data));
    }
}
