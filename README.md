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

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Additional Resources

- [Corezoid API Documentation](https://doc.corezoid.com/en/api/upload_modify.html)
- [Corezoid Website](https://corezoid.com/)
