import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.corezoid.sdk.entity.CorezoidMessage;
import com.corezoid.sdk.entity.RequestOperation;
import com.corezoid.sdk.entity.ResponseOperation;

import java.util.Collections;

public class CorezoidMessageTest {

    @Test
    public void testRequestCreation() {
        CorezoidMessage message = CorezoidMessage.request("secret", "login", Collections.emptyList());
        assertNotNull(message);
    }

    @Test
    public void testResponseCreation() {
        String response = CorezoidMessage.response(Collections.emptyList());
        assertNotNull(response);
    }

    @Test
    public void testCheckSign() {
        boolean isValid = CorezoidMessage.checkSign("sign", "secret", "time", "content");
        assertFalse(isValid);
    }
}
