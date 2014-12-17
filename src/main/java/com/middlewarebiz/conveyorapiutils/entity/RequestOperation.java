package com.middlewarebiz.conveyorapiutils.entity;

import net.sf.json.JSONObject;

/**
 * Request operation
 *
 * @author Middleware <support@middleware.biz>
 */
public class RequestOperation {
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Builder for 'Create task' oparation
     *
     * @param convId - conveyor id
     * @param ref - task reference
     * @param data - task data
     * @return
     */
    public static RequestOperation create(String convId, String ref,
                                          JSONObject data) {

        return new RequestOperation(Query.create, convId, ref, data);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Builder for 'Modify task' operation
     *
     * @param convId - conveyor id
     * @param ref - task reference
     * @param data -task data
     * @return
     */
    public static RequestOperation modify(String convId, String ref,
                                          JSONObject data) {
        
        return new RequestOperation(Query.modify, convId, ref, data);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * @param type - request type
     * @param convId - conveyor id
     * @param ref - task reference
     * @param data - task data
     */
    private RequestOperation(Query type, String convId, String ref,
                             JSONObject data) {
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
        this.type = type;
        this.obj = "task";
        this.convId = convId;
        this.data = data;
    }

//----------------------------------------------------------------------------------------------------------------------
    public JSONObject toJSONObject() {
        JSONObject res = new JSONObject()
                .element("ref", ref)
                .element("conv_id", convId)
                .element("type", type)
                .element("obj", obj)
                .element("data", data);
        return res;
    }
//----------------------------------------------------------------------------------------------------------------------
    private final String convId;
    private final String ref;
    private final Query type;
    private final String obj;
    private final JSONObject data;

    private enum Query {

        create, modify
    }
}
