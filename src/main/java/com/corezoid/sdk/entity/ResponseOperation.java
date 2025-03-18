package com.corezoid.sdk.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builder for response operations from the Corezoid API.
 * <p>
 * This class provides builder methods for creating response objects representing
 * operation results, such as successful or failed operations.
 * </p>
 *
 * @author Corezoid <support@corezoid.com>
 */
public class ResponseOperation {
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Builder for 'fail' operation
     *
     * @param convId
     * @param ref
     * @param data
     * @return
     */
    public static ResponseOperation fail(String convId, String ref,
                                         ObjectNode data) {
        return new ResponseOperation(ResultType.fail, convId, ref, data);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Builder for 'ok' operation
     *
     * @param convId
     * @param ref
     * @param data
     * @return
     */
    public static ResponseOperation ok(String convId, String ref,
                                       ObjectNode data) {
        return new ResponseOperation(ResultType.ok, convId, ref, data);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * @param proc
     * @param convId
     * @param ref
     * @param data
     */
    private ResponseOperation(ResultType proc, String convId, String ref,
                              ObjectNode data) {
        if(convId == null || convId.equals("") ){
            throw new IllegalArgumentException("convId is null or empty");
        }
        if(ref == null || ref.equals("") ){
            throw new IllegalArgumentException("ref is null or empty");
        }
        if(data == null ){
            throw new IllegalArgumentException("data is null");
        }
        this.ref = ref;
        this.convId = convId;
        this.data = data;
        this.proc = proc;
    }

//----------------------------------------------------------------------------------------------------------------------
    public ObjectNode toJSONObject() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode res = mapper.createObjectNode();
        res.put("ref", ref);
        res.put("conv_id", convId);
        res.put("proc", proc.name());
        res.set("res_data", data);
        return res;
    }
//----------------------------------------------------------------------------------------------------------------------
    private final String convId;
    private final String ref;
    private final ResultType proc;
    private final ObjectNode data;

    private enum ResultType {

        ok, fail
    }
}
