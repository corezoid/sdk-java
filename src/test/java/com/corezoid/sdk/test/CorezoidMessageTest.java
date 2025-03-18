import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.corezoid.sdk.entity.CorezoidMessage;
import com.corezoid.sdk.entity.RequestOperation;
import com.corezoid.sdk.entity.ResponseOperation;

import java.util.Collections;
import java.util.Map;

public class CorezoidMessageTest {

    @Test
    public void testRequestCreation() {
        CorezoidMessage message = CorezoidMessage.request("secret", "login", Collections.emptyList());
        assertNotNull(message);
        assertTrue(message.body.contains("ops"));
        assertTrue(message.sign.length() > 0);
    }

    @Test
    public void testResponseCreation() {
        String response = CorezoidMessage.response(Collections.emptyList());
        assertNotNull(response);
        assertTrue(response.contains("ok"));
    }

    @Test
    public void testCheckSign() {
        boolean isValid = CorezoidMessage.checkSign("sign", "secret", "time", "content");
        assertFalse(isValid);
    }

    @Test
    public void testParseAnswer() throws Exception {
        String jsonString = "{\"request_proc\":\"ok\",\"ops\":[{\"ref\":\"123\",\"proc\":\"ok\"}]}";
        Map<String, String> result = CorezoidMessage.parseAnswer(jsonString);
        assertEquals("ok", result.get("123"));
    }
}
