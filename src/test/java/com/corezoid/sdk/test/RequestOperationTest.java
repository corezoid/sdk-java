import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.corezoid.sdk.entity.RequestOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RequestOperationTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCreateOperation() {
        ObjectNode data = mapper.createObjectNode();
        data.put("key", "value");
        
        RequestOperation operation = RequestOperation.create("123", "ref-123", data);
        ObjectNode json = operation.toJSONObject();
        
        assertEquals("123", json.get("conv_id").asText());
        assertEquals("ref-123", json.get("ref").asText());
        assertEquals("create", json.get("type").asText());
        assertEquals("task", json.get("obj").asText());
        assertEquals("value", json.get("data").get("key").asText());
    }

    @Test
    public void testModifyIdOperation() {
        ObjectNode data = mapper.createObjectNode();
        data.put("status", "completed");
        
        RequestOperation operation = RequestOperation.modifyId("123", "task-123", data);
        ObjectNode json = operation.toJSONObject();
        
        assertEquals("123", json.get("conv_id").asText());
        assertEquals("task-123", json.get("obj_id").asText());
        assertFalse(json.has("ref"));
        assertEquals("modify", json.get("type").asText());
        assertEquals("completed", json.get("data").get("status").asText());
    }

    @Test
    public void testModifyRefOperation() {
        ObjectNode data = mapper.createObjectNode();
        data.put("priority", "high");
        
        RequestOperation operation = RequestOperation.modifyRef("123", "ref-123", data);
        ObjectNode json = operation.toJSONObject();
        
        assertEquals("123", json.get("conv_id").asText());
        assertEquals("ref-123", json.get("ref").asText());
        assertFalse(json.has("obj_id"));
        assertEquals("modify", json.get("type").asText());
        assertEquals("high", json.get("data").get("priority").asText());
    }

    @Test
    public void testInvalidInputs() {
        ObjectNode data = mapper.createObjectNode();
        
        assertThrows(IllegalArgumentException.class, () -> 
            RequestOperation.create(null, "ref", data));
        
        assertThrows(IllegalArgumentException.class, () -> 
            RequestOperation.create("123", null, data));
            
        assertThrows(IllegalArgumentException.class, () -> 
            RequestOperation.create("123", "ref", null));
    }
}
