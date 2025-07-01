# Corezoid Java SDK - Quick Reference

A concise reference for the most commonly used features of the Corezoid Java SDK.

## Basic Setup

```java
// Initialize HTTP manager (reuse this instance)
HttpManager httpManager = new HttpManager(20, 5000, 15000);

// Your API credentials
String apiSecret = "your_api_secret";
String apiLogin = "your_api_login";
String conveyorId = "your_conveyor_id";
```

## Common Operations

### Create Task

```java
// Prepare task data
ObjectMapper mapper = new ObjectMapper();
ObjectNode taskData = mapper.createObjectNode();
taskData.put("customer_id", "12345");
taskData.put("amount", 100.50);

// Create and send
String reference = "task-" + System.currentTimeMillis();
RequestOperation operation = RequestOperation.create(conveyorId, reference, taskData);
CorezoidMessage message = CorezoidMessage.request(apiSecret, apiLogin, 
                                                 Collections.singletonList(operation));

String response = httpManager.send(message);
Map<String, String> result = CorezoidMessage.parseAnswer(response);
```

### Modify Task by Reference

```java
ObjectNode updateData = mapper.createObjectNode();
updateData.put("status", "processed");

RequestOperation operation = RequestOperation.modifyRef(conveyorId, reference, updateData);
CorezoidMessage message = CorezoidMessage.request(apiSecret, apiLogin, 
                                                 Collections.singletonList(operation));
```

### Modify Task by ID

```java
ObjectNode updateData = mapper.createObjectNode();
updateData.put("notes", "Updated via API");

RequestOperation operation = RequestOperation.modifyId(conveyorId, taskId, updateData);
CorezoidMessage message = CorezoidMessage.request(apiSecret, apiLogin, 
                                                 Collections.singletonList(operation));
```

### Batch Operations

```java
List<RequestOperation> operations = Arrays.asList(
    RequestOperation.create(conveyorId, "ref-1", data1),
    RequestOperation.create(conveyorId, "ref-2", data2),
    RequestOperation.modifyRef(conveyorId, "existing-ref", updateData)
);

CorezoidMessage message = CorezoidMessage.request(apiSecret, apiLogin, operations);
String response = httpManager.send(message);
Map<String, String> results = CorezoidMessage.parseAnswer(response);
```

## Error Handling

```java
try {
    String response = httpManager.send(message);
    Map<String, String> results = CorezoidMessage.parseAnswer(response);
    
    for (Map.Entry<String, String> entry : results.entrySet()) {
        if ("ok".equals(entry.getValue())) {
            System.out.println("Success: " + entry.getKey());
        } else {
            System.err.println("Failed: " + entry.getKey() + " - " + entry.getValue());
        }
    }
    
} catch (HttpException e) {
    // Network/HTTP errors
    System.err.println("HTTP Error: " + e.getMessage());
    
} catch (Exception e) {
    // Parsing/other errors
    System.err.println("Error: " + e.getMessage());
}
```

## Response Verification (Callbacks)

```java
// Verify incoming callback signatures
public boolean verifyCallback(HttpServletRequest request, String requestBody) {
    String signature = request.getParameter("signature");
    String timestamp = request.getParameter("gmt_unixtime");
    
    return CorezoidMessage.checkSign(signature, apiSecret, timestamp, requestBody);
}
```

## Configuration Examples

### Development Environment
```java
HttpManager devManager = new HttpManager(5, 3000, 10000);
```

### Production Environment
```java
HttpManager prodManager = new HttpManager(50, 5000, 15000);
```

### Slow Network
```java
HttpManager slowManager = new HttpManager(10, 10000, 30000);
```

## Common Patterns

### Unique Reference Generation
```java
public String generateReference(String prefix) {
    return prefix + "-" + System.currentTimeMillis() + "-" + 
           ThreadLocalRandom.current().nextInt(1000, 9999);
}
```

### Retry Logic
```java
public String sendWithRetry(CorezoidMessage message, int maxRetries) throws HttpException {
    HttpException lastException = null;
    
    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            return httpManager.send(message);
        } catch (HttpException e) {
            lastException = e;
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(1000 * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    throw lastException;
}
```

### Async Processing
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    try {
        return httpManager.send(message);
    } catch (HttpException e) {
        throw new RuntimeException(e);
    }
});

future.thenAccept(response -> {
    // Process response asynchronously
});
```

## Status Codes

| Status | Meaning |
|--------|---------|
| `ok` | Operation successful |
| `fail` | Operation failed |
| `error` | System error occurred |

## HTTP Error Codes

| Code | Meaning | Action |
|------|---------|--------|
| 401 | Unauthorized | Check API credentials |
| 403 | Forbidden | Check conveyor permissions |
| 404 | Not Found | Verify conveyor ID |
| 429 | Rate Limited | Implement retry with backoff |
| 500 | Server Error | Retry with exponential backoff |

## Best Practices

- ✅ **Reuse HttpManager** - Create once, use throughout application
- ✅ **Handle exceptions** - Always catch HttpException and parsing errors
- ✅ **Use batch operations** - More efficient than individual requests
- ✅ **Generate unique references** - Include timestamps or UUIDs
- ✅ **Verify signatures** - Always validate callback signatures
- ✅ **Configure timeouts** - Set appropriate values for your network
- ✅ **Enable logging** - Use DEBUG level for troubleshooting

## Common Mistakes

- ❌ Creating new HttpManager for each request
- ❌ Not handling exceptions properly
- ❌ Using placeholder API credentials
- ❌ Not verifying callback signatures
- ❌ Hardcoding sensitive data
- ❌ Not using unique task references

## Quick Debugging

```java
// Enable debug logging
logger.debug("Request URL: {}", message.url);
logger.debug("Request Body: {}", message.body);
logger.debug("Response: {}", response);

// Manual signature verification
String expectedSignature = generateSignature(time, apiSecret, body);
logger.debug("Expected: {}, Received: {}", expectedSignature, receivedSignature);
```

## Links

- [Full Documentation](README.md)
- [Troubleshooting Guide](TROUBLESHOOTING.md)
- [Corezoid API Docs](https://doc.corezoid.com/)