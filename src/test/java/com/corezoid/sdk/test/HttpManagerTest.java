import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.corezoid.sdk.entity.CorezoidMessage;
import com.corezoid.sdk.utils.HttpManager;

public class HttpManagerTest {

    @Test
    public void testSend() {
        HttpManager httpManager = new HttpManager();
        CorezoidMessage message = CorezoidMessage.request("secret", "login", Collections.emptyList());
        assertThrows(Exception.class, () -> httpManager.send(message));
    }
}
