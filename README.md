# Corezoid Java SDK

A modern Java client library for interacting with the [Corezoid Process Engine](https://corezoid.com/) platform.

## Overview

The Corezoid SDK for Java provides a simplified interface for creating, modifying, and managing tasks within Corezoid processes (also called conveyors). This SDK handles complex aspects like secure communication, message signing, HTTP connection management, and request/response formatting.

## Requirements

- Java 17 or higher
- Maven or Gradle build system

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.corezoid</groupId>
    <artifactId>corezoid-sdk</artifactId>
    <version>2.4</version>
</dependency>
```

### Gradle

Add the following dependency to your `build.gradle`:

```groovy
implementation 'com.corezoid:corezoid-sdk:2.4'
```

## Usage Examples

### Creating a New Task

```java
import com.corezoid.sdk.entity.CorezoidMessage;
import com.corezoid.sdk.entity.RequestOperation;
import com.corezoid.sdk.utils.HttpManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collections;

public class CreateTaskExample {
    public static void main(String[] args) throws Exception {
        // Initialize HTTP manager
        HttpManager httpManager = new HttpManager(10, 5000, 10000);
        
        // Create task data
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode taskData = mapper.createObjectNode();
        taskData.put("customer_id", "12345");
        taskData.put("amount", 100.50);
        taskData.put("currency", "USD");
        
        // Create operation
        String conveyorId = "1234"; // Your conveyor ID
        String reference = "order-" + System.currentTimeMillis(); // Unique reference
        RequestOperation createOperation = RequestOperation.create(conveyorId, reference, taskData);
        
        // Create and send request
        String apiSecret = "your_api_secret";
        String apiLogin = "your_api_login";
        CorezoidMessage message = CorezoidMessage.request(apiSecret, apiLogin, 
                                                         Collections.singletonList(createOperation));
        
        // Send request and parse response
        String response = httpManager.send(message);
        System.out.println("Response: " + response);
        
        // Parse response to get operation status
        Map<String, String> result = CorezoidMessage.parseAnswer(response);
        System.out.println("Operation status for " + reference + ": " + result.get(reference));
    }
}
```

### Modifying an Existing Task

```java
// Modify task by reference
ObjectNode updateData = mapper.createObjectNode();
updateData.put("status", "processed");
updateData.put("processed_at", System.currentTimeMillis());

RequestOperation modifyOperation = RequestOperation.modifyRef(conveyorId, reference, updateData);
CorezoidMessage message = CorezoidMessage.request(apiSecret, apiLogin, 
                                                Collections.singletonList(modifyOperation));

String response = httpManager.send(message);
Map<String, String> result = CorezoidMessage.parseAnswer(response);
System.out.println("Update status: " + result.get(reference));
```

### Error Handling

```java
import com.corezoid.sdk.utils.HttpException;

public void createTaskWithErrorHandling() {
    try {
        // Create and send request
        RequestOperation operation = RequestOperation.create(conveyorId, reference, taskData);
        CorezoidMessage message = CorezoidMessage.request(apiSecret, apiLogin, 
                                                         Collections.singletonList(operation));
        
        String response = httpManager.send(message);
        Map<String, String> result = CorezoidMessage.parseAnswer(response);
        
        String status = result.get(reference);
        if ("ok".equals(status)) {
            System.out.println("Task created successfully!");
        } else {
            System.err.println("Task creation failed with status: " + status);
        }
        
    } catch (HttpException e) {
        System.err.println("Network error: " + e.getMessage());
        // Handle connection issues, timeouts, HTTP errors
        
    } catch (Exception e) {
        System.err.println("Parsing error: " + e.getMessage());
        // Handle JSON parsing errors, invalid responses
    }
}
```

### Batch Operations

```java
// Process multiple tasks in a single request
List<RequestOperation> operations = new ArrayList<>();

// Add multiple operations
operations.add(RequestOperation.create("1234", "order-1", taskData1));
operations.add(RequestOperation.create("1234", "order-2", taskData2));
operations.add(RequestOperation.modifyRef("1234", "existing-ref", updateData));

CorezoidMessage batchMessage = CorezoidMessage.request(apiSecret, apiLogin, operations);
String response = httpManager.send(batchMessage);

// Parse batch results
Map<String, String> results = CorezoidMessage.parseAnswer(response);
for (Map.Entry<String, String> entry : results.entrySet()) {
    System.out.println("Reference: " + entry.getKey() + ", Status: " + entry.getValue());
}
```

## Configuration

### HTTP Manager Configuration

The `HttpManager` class can be configured with the following parameters:

```java
// Parameters:
// - maxConnections: Maximum number of concurrent connections
// - connectionTimeout: Connection timeout in milliseconds
// - responseTimeout: Response timeout in milliseconds
HttpManager httpManager = new HttpManager(10, 5000, 10000);
```

## API Documentation

### Core Classes

#### CorezoidMessage

The central class for creating, signing, and verifying messages exchanged with the Corezoid platform.

```java
// Create a request message
CorezoidMessage.request(String apiSecret, String apiLogin, List<RequestOperation> operations)

// Create a response message
String response = CorezoidMessage.response(List<ResponseOperation> operations)

// Verify message signature
boolean isValid = CorezoidMessage.checkSign(String sign, String apiSecret, String time, String content)

// Parse response
Map<String, String> result = CorezoidMessage.parseAnswer(String jsonString)
```

#### RequestOperation

Provides builders for different types of operations that can be performed on Corezoid tasks.

```java
// Create a new task
RequestOperation.create(String convId, String ref, ObjectNode data)

// Modify a task by ID
RequestOperation.modifyId(String convId, String taskId, ObjectNode data)

// Modify a task by reference
RequestOperation.modifyRef(String convId, String ref, ObjectNode data)
```

#### ResponseOperation

Provides builders for creating response objects representing operation results.

```java
// Create a success response
ResponseOperation.ok(String convId, String ref, ObjectNode data)

// Create a failure response
ResponseOperation.fail(String convId, String ref, ObjectNode data)
```

#### HttpManager

Handles HTTP communication with the Corezoid platform, including connection pooling and request execution.

```java
// Initialize HTTP manager
HttpManager httpManager = new HttpManager(int maxConnections, int connectionTimeout, int responseTimeout)

// Send a message
String response = httpManager.send(CorezoidMessage message)
```

## Troubleshooting

For common issues, error handling patterns, and debugging tips, see the [Troubleshooting Guide](TROUBLESHOOTING.md).

Common issues include:
- **Authentication failures** - Check API credentials and conveyor permissions
- **Connection timeouts** - Adjust HttpManager timeout settings
- **JSON parsing errors** - Validate request data and response format
- **Memory leaks** - Reuse HttpManager instances instead of creating new ones

## Best Practices

- **Reuse HttpManager instances** - Create once, use many times for better performance
- **Handle errors gracefully** - Always catch HttpException and parsing exceptions
- **Use batch operations** - Process multiple tasks in single requests when possible
- **Generate unique references** - Include timestamps or UUIDs to avoid conflicts
- **Enable debug logging** - Use DEBUG level for troubleshooting issues

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Additional Resources

- [Quick Reference Guide](QUICK_REFERENCE.md) - Concise examples and common patterns
- [Troubleshooting Guide](TROUBLESHOOTING.md) - Common issues and solutions
- [Corezoid API Documentation](https://doc.corezoid.com/en/api/upload_modify.html)
- [Corezoid Website](https://corezoid.com/)
