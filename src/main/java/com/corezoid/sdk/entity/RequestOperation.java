package com.corezoid.sdk.entity;

import net.sf.json.JSONObject;

/**
 * Request operation
 *
 * @author Corezoid <support@corezoid.com>
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

        return new RequestOperation(Query.create, convId, ref, null, data);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * @deprecated, please use  modifyId or modifyRef methods
     * @param convId
     * @param ref
     * @param data
     * @return
     */
    @Deprecated
    public static RequestOperation modify(String convId, String ref,
                                          JSONObject data) {

        return new RequestOperation(Query.modify, convId, ref, null, data);
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Builder for 'Modify task by id' operation
     *
     * @param convId - conveyor id
     * @param taskId - task id
     * @param data -task data
     * @return
     */
    public static RequestOperation modifyId(String convId, String taskId,
                                            JSONObject data) {

        return new RequestOperation(Query.modify, convId, null, taskId, data);
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Builder for 'Modify task by ref' operation
     *
     * @param convId - conveyor id
     * @param ref - task reference
     * @param data -task data
     * @return
     */
    public static RequestOperation modifyRef(String convId, String ref,
                                             JSONObject data) {

        return new RequestOperation(Query.modify, convId, ref, null, data);
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * @param type - request type
     * @param convId - conveyor id
     * @param ref - task reference
     * @param data - task data
     */
    private RequestOperation(Query type, String convId, String ref, String objId,
                             JSONObject data) {
        if(convId == null || convId.equals("") ){
            throw new IllegalArgumentException("convId is null or empty");
        }
        if(objId == null && ref == null ){
            throw new IllegalArgumentException("ref and taskId is null");
        }
        if(ref == null && objId.equals("") ){
            throw new IllegalArgumentException("taskId is empty");
        }
        if(objId == null && ref.equals("") ){
            throw new IllegalArgumentException("ref is empty");
        }
        if(data == null ){
            throw new IllegalArgumentException("data is null");
        }
        this.ref = ref;
        this.objId = objId;
        this.type = type;
        this.obj = "task";
        this.convId = convId;
        this.data = data;
    }

    //----------------------------------------------------------------------------------------------------------------------
    public JSONObject toJSONObject() {
        JSONObject res = new JSONObject();
        if(ref!= null){
            res.element("ref", ref);
        }
        if(objId!= null){
            res.element("obj_id", objId);
        }
        res.element("conv_id", convId);
        res.element("type", type);
        res.element("obj", obj);
        res.element("data", data);
        return res;
    }
    //----------------------------------------------------------------------------------------------------------------------
    private final String convId;
    private final String objId;
    private final String ref;
    private final Query type;
    private final String obj;
    private final JSONObject data;

    private enum Query {

        create, modify
    }
}
