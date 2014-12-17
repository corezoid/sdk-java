package com.middlewarebiz.conveyorapiutils.entity;

import net.sf.json.JSONObject;

/**
 * Response operation
 *
 * @author Middleware <support@middleware.biz>
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
                                         JSONObject data) {
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
                                       JSONObject data) {
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
        this.convId = convId;
        this.data = data;
        this.proc = proc;
    }

//----------------------------------------------------------------------------------------------------------------------
    public JSONObject toJSONObject() {
        JSONObject res = new JSONObject()
                .element("ref", ref)
                .element("conv_id", convId)
                .element("proc", proc)
                .element("res_data", data);
        return res;
    }
//----------------------------------------------------------------------------------------------------------------------
    private final String convId;
    private final String ref;
    private final ResultType proc;
    private final JSONObject data;

    private enum ResultType {

        ok, fail
    }
}
