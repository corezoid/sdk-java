# Corezoid Java SDK - Troubleshooting Guide

This guide covers common issues, error handling patterns, and best practices when using the Corezoid Java SDK.

## Table of Contents

- [Common Issues](#common-issues)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)
- [Performance Optimization](#performance-optimization)
- [Security Considerations](#security-considerations)
- [Debugging Tips](#debugging-tips)

## Common Issues

### 1. Authentication Failures

**Problem**: Getting 401 Unauthorized or signature validation errors

**Symptoms**:
```
HttpException: Fail to send POST request to url https://api.corezoid.com/api/1/json/your_login/timestamp/signature, code : '401'
```

**Solutions**:
```java
// ✅ Correct: Ensure API credentials are valid
String apiSecret = "your_actual_api_secret"; // Not placeholder text
String apiLogin = "your_actual_api_login";   // Not placeholder text

// ✅ Correct: Verify conveyor ID is correct
String conveyorId = "12345"; // Use actual conveyor ID from Corezoid dashboard

// ❌ Common mistake: Using placeholder values
String apiSecret = "your_api_secret"; // This will fail!
```

**Debug Steps**:
1. Verify your API credentials in the Corezoid dashboard
2. Check that the conveyor ID exists and is accessible
3. Ensure system clock is synchronized (signatures are time-sensitive)
4. Test with a simple create operation first

### 2. Connection Timeouts

**Problem**: Requests timing out or connection refused

**Symptoms**:
```
HttpException: Fail to send POST request, Connection timeout
```

**Solutions**:
```java
// ✅ Increase timeouts for slow networks
HttpManager httpManager = new HttpManager(
    10,     // maxConnections
    10000,  // connectionTimeout: 10 seconds
    30000   // responseTimeout: 30 seconds
);

// ✅ Configure retry logic
public String sendWithRetry(CorezoidMessage message, int maxRetries) {
    HttpManager httpManager = new HttpManager();
    
    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            return httpManager.send(message);
        } catch (HttpException e) {
            if (attempt == maxRetries) {
                throw e; // Re-throw on final attempt
            }
            
            // Exponential backoff
            try {
                Thread.sleep(1000 * attempt);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during retry", ie);
            }
        }
    }
    return null; // Never reached
}
```

### 3. JSON Parsing Errors

**Problem**: Invalid JSON in request data or response parsing failures

**Symptoms**:
```
Exception: Cannot parse response JSON
```

**Solutions**:
```java
// ✅ Validate your data before sending
ObjectMapper mapper = new ObjectMapper();
ObjectNode taskData = mapper.createObjectNode();

// Ensure all values are properly typed
taskData.put("customer_id", "12345");           // String
taskData.put("amount", new BigDecimal("100.50")); // Use BigDecimal for money
taskData.put("timestamp", System.currentTimeMillis()); // Long
taskData.put("active", true);                   // Boolean

// ✅ Handle parsing errors gracefully
try {
    Map<String, String> result = CorezoidMessage.parseAnswer(response);
    // Process result...
} catch (Exception e) {
    logger.error("Failed to parse Corezoid response: {}", response, e);
    // Handle parsing failure...
}
```

### 4. Memory Leaks with HttpManager

**Problem**: Application running out of memory or connection pool exhaustion

**Solutions**:
```java
// ❌ Wrong: Creating new HttpManager for each request
public void processTask(TaskData data) {
    HttpManager httpManager = new HttpManager(); // Creates new connection pool!
    // ... use httpManager
} // Connection pool not properly closed

// ✅ Correct: Reuse HttpManager instance
public class CorezoidService {
    private final HttpManager httpManager;
    
    public CorezoidService() {
        this.httpManager = new HttpManager(20, 5000, 15000);
    }
    
    public void processTask(TaskData data) {
        // Reuse the same HttpManager instance
        String response = httpManager.send(message);
    }
    
    // ✅ Implement proper cleanup if needed
    @PreDestroy
    public void cleanup() {
        // HttpManager uses Apache HttpClient which handles cleanup automatically
        // No manual cleanup needed in most cases
    }
}
```

## Error Handling

### Comprehensive Error Handling Pattern

```java
public class CorezoidService {
    private static final Logger logger = LoggerFactory.getLogger(CorezoidService.class);
    private final HttpManager httpManager;
    
    public CorezoidService() {
        this.httpManager = new HttpManager(10, 5000, 10000);
    }
    
    public TaskResult createTask(String conveyorId, String reference, ObjectNode data) {
        try {
            // Create operation
            RequestOperation operation = RequestOperation.create(conveyorId, reference, data);
            CorezoidMessage message = CorezoidMessage.request(
                apiSecret, apiLogin, Collections.singletonList(operation)
            );
            
            // Send request
            String response = httpManager.send(message);
            logger.debug("Corezoid response: {}", response);
            
            // Parse response
            Map<String, String> result = CorezoidMessage.parseAnswer(response);
            String status = result.get(reference);
            
            if ("ok".equals(status)) {
                return TaskResult.success(reference, response);
            } else {
                return TaskResult.failure(reference, "Task processing failed: " + status);
            }
            
        } catch (HttpException e) {
            logger.error("HTTP error creating task {}: {}", reference, e.getMessage(), e);
            return TaskResult.error(reference, "Network error: " + e.getMessage());
            
        } catch (Exception e) {
            logger.error("Unexpected error creating task {}: {}", reference, e.getMessage(), e);
            return TaskResult.error(reference, "Internal error: " + e.getMessage());
        }
    }
    
    // Result wrapper class
    public static class TaskResult {
        private final boolean success;
        private final String reference;
        private final String message;
        private final String response;
        
        private TaskResult(boolean success, String reference, String message, String response) {
            this.success = success;
            this.reference = reference;
            this.message = message;
            this.response = response;
        }
        
        public static TaskResult success(String reference, String response) {
            return new TaskResult(true, reference, "Success", response);
        }
        
        public static TaskResult failure(String reference, String message) {
            return new TaskResult(false, reference, message, null);
        }
        
        public static TaskResult error(String reference, String message) {
            return new TaskResult(false, reference, message, null);
        }
        
        // Getters...
        public boolean isSuccess() { return success; }
        public String getReference() { return reference; }
        public String getMessage() { return message; }
        public String getResponse() { return response; }
    }
}
```

## Best Practices

### 1. Thread Safety

```java
// ✅ HttpManager is thread-safe - can be shared across threads
public class CorezoidService {
    private final HttpManager httpManager = new HttpManager();
    
    // This method can be called from multiple threads safely
    public void processTask(TaskData data) {
        String response = httpManager.send(message);
        // Process response...
    }
}

// ✅ CorezoidMessage is immutable - thread-safe
CorezoidMessage message = CorezoidMessage.request(apiSecret, apiLogin, operations);
// Can be safely used across multiple threads
```

### 2. Connection Pool Management

```java
// ✅ Configure appropriate connection pool size
HttpManager httpManager = new HttpManager(
    50,     // maxConnections: Based on expected concurrent requests
    5000,   // connectionTimeout: 5 seconds
    15000   // responseTimeout: 15 seconds
);

// ✅ For high-throughput applications
HttpManager highThroughputManager = new HttpManager(
    100,    // Higher connection pool
    3000,   // Faster connection timeout
    10000   // Reasonable response timeout
);
```

### 3. Reference Generation

```java
// ✅ Generate unique references
public String generateReference(String prefix) {
    return prefix + "-" + System.currentTimeMillis() + "-" + 
           ThreadLocalRandom.current().nextInt(1000, 9999);
}

// ✅ For distributed systems, include instance ID
public String generateDistributedReference(String prefix, String instanceId) {
    return prefix + "-" + instanceId + "-" + System.currentTimeMillis() + "-" + 
           UUID.randomUUID().toString().substring(0, 8);
}
```

### 4. Batch Operations

```java
// ✅ Process multiple operations in a single request
public void processBatch(List<TaskData> tasks) {
    List<RequestOperation> operations = tasks.stream()
        .map(task -> RequestOperation.create(
            task.getConveyorId(),
            generateReference("batch"),
            task.getData()
        ))
        .collect(Collectors.toList());
    
    CorezoidMessage message = CorezoidMessage.request(apiSecret, apiLogin, operations);
    String response = httpManager.send(message);
    
    // Parse batch response
    Map<String, String> results = CorezoidMessage.parseAnswer(response);
    // Process results...
}
```

## Performance Optimization

### 1. Connection Reuse

```java
// ✅ Singleton pattern for HttpManager
@Component
public class CorezoidHttpManager {
    private final HttpManager httpManager;
    
    public CorezoidHttpManager(@Value("${corezoid.max-connections:20}") int maxConnections,
                               @Value("${corezoid.connection-timeout:5000}") int connectionTimeout,
                               @Value("${corezoid.response-timeout:15000}") int responseTimeout) {
        this.httpManager = new HttpManager(maxConnections, connectionTimeout, responseTimeout);
    }
    
    public HttpManager getHttpManager() {
        return httpManager;
    }
}
```

### 2. Async Processing Pattern

```java
// ✅ Async processing with CompletableFuture
@Service
public class AsyncCorezoidService {
    private final HttpManager httpManager;
    private final Executor executor;
    
    public AsyncCorezoidService() {
        this.httpManager = new HttpManager();
        this.executor = ForkJoinPool.commonPool();
    }
    
    public CompletableFuture<TaskResult> createTaskAsync(String conveyorId, String reference, ObjectNode data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                RequestOperation operation = RequestOperation.create(conveyorId, reference, data);
                CorezoidMessage message = CorezoidMessage.request(
                    apiSecret, apiLogin, Collections.singletonList(operation)
                );
                
                String response = httpManager.send(message);
                Map<String, String> result = CorezoidMessage.parseAnswer(response);
                
                return TaskResult.success(reference, response);
            } catch (Exception e) {
                return TaskResult.error(reference, e.getMessage());
            }
        }, executor);
    }
}
```

## Security Considerations

### 1. API Credential Management

```java
// ✅ Use environment variables or secure configuration
public class CorezoidConfig {
    @Value("${corezoid.api.secret}")
    private String apiSecret;
    
    @Value("${corezoid.api.login}")
    private String apiLogin;
    
    // ❌ Never hard-code credentials
    // private String apiSecret = "actual_secret_here"; // DON'T DO THIS!
}
```

### 2. Data Sanitization

```java
// ✅ Sanitize sensitive data before sending
public ObjectNode sanitizeTaskData(ObjectNode originalData) {
    ObjectNode sanitized = originalData.deepCopy();
    
    // Remove or mask sensitive fields
    if (sanitized.has("ssn")) {
        sanitized.put("ssn", "***-**-" + sanitized.get("ssn").asText().substring(7));
    }
    
    if (sanitized.has("credit_card")) {
        String cc = sanitized.get("credit_card").asText();
        sanitized.put("credit_card", "**** **** **** " + cc.substring(cc.length() - 4));
    }
    
    return sanitized;
}
```

## Debugging Tips

### 1. Enable Debug Logging

```xml
<!-- logback-spring.xml -->
<configuration>
    <logger name="com.corezoid.sdk" level="DEBUG"/>
    <logger name="org.apache.hc.client5" level="DEBUG"/>
</configuration>
```

### 2. Request/Response Logging

```java
public class DebugCorezoidService {
    private static final Logger logger = LoggerFactory.getLogger(DebugCorezoidService.class);
    
    public String sendWithLogging(CorezoidMessage message) throws HttpException {
        logger.debug("Sending request to URL: {}", message.url);
        logger.debug("Request body: {}", message.body);
        
        long startTime = System.currentTimeMillis();
        String response = httpManager.send(message);
        long duration = System.currentTimeMillis() - startTime;
        
        logger.debug("Response received in {}ms: {}", duration, response);
        return response;
    }
}
```

### 3. Common Error Codes

| HTTP Code | Meaning | Solution |
|-----------|---------|----------|
| 401 | Unauthorized | Check API credentials and signature |
| 403 | Forbidden | Verify conveyor access permissions |
| 404 | Not Found | Check conveyor ID and API endpoint |
| 429 | Rate Limited | Implement exponential backoff |
| 500 | Server Error | Retry with backoff, check Corezoid status |

### 4. Signature Debugging

```java
// Debug signature generation
public void debugSignature(String time, String apiSecret, String body) {
    String stringToSign = time + apiSecret + body + apiSecret;
    logger.debug("String to sign: {}", stringToSign);
    
    // Manual signature calculation for debugging
    try {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] bytes = stringToSign.getBytes("UTF-8");
        byte[] digest = sha1.digest(bytes);
        String signature = HexFormat.of().formatHex(digest).toLowerCase();
        logger.debug("Generated signature: {}", signature);
    } catch (Exception e) {
        logger.error("Signature generation failed", e);
    }
}
```

## Getting Help

If you're still experiencing issues:

1. **Check the logs** - Enable DEBUG logging for detailed information
2. **Verify credentials** - Test with Corezoid dashboard or API tools
3. **Check network connectivity** - Ensure you can reach api.corezoid.com
4. **Review API documentation** - [Corezoid API Docs](https://doc.corezoid.com/)
5. **Contact support** - Reach out to Corezoid support with logs and error details

## FAQ

**Q: Why am I getting "Invalid signature" errors?**
A: The SDK uses SHA-1 for signature generation as required by the Corezoid API. Ensure your system clock is synchronized and API credentials are correct.

**Q: Can I use this SDK in a multi-threaded environment?**
A: Yes, the SDK is thread-safe. You can share a single HttpManager instance across multiple threads.

**Q: How do I handle rate limiting?**
A: Implement exponential backoff when you receive 429 responses. Start with 1-second delays and double the delay on each retry.

**Q: What's the maximum number of operations per request?**
A: Check the Corezoid API documentation for current limits. Generally, batch operations are more efficient than individual requests.