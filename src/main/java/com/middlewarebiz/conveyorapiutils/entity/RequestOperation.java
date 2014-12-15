
package com.middlewarebiz.conveyorapiutils.entity;

import java.util.Map;
import net.sf.json.JSONObject;

/**
 * Request operation
 * @author dn300986zav
 */
public class RequestOperation {
//----------------------------------------------------------------------------------------------------------------------
    /**
     * Create task in conveyor
     * @param cinvId - conveyor id
     * @param ref - task reference
     * @param data - data to send, Map<key,value>
     * key - A key string.
     * value - An object which is the value.
     * It should be of one of these types:
     * Boolean, Double, Integer, JSONArray, JSONObject, Long, String, or the JSONNull object.
     * @return
     */
    public static RequestOperation create( String cinvId, String ref, Map<String, Object> data ) {

        return new RequestOperation( Query.create, cinvId, ref, data );
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Modify task in conveyor
     * @param cinvId - conveyor id
     * @param ref - task reference
     * @param data -data to send, Map<key,value>
     * key - A key string.
     * value - An object which is the value.
     * It should be of one of these types:
     * Boolean, Double, Integer, JSONArray, JSONObject, Long, String, or the JSONNull object.
     * @return
     */
    public static RequestOperation modify( String cinvId, String ref, Map<String, Object> data ) {
        return new RequestOperation( Query.modify, cinvId, ref, data );
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param type - request type
     * @param cinvId - conveyor id
     * @param ref - task reference
     * @param data -Map<key,value>
     * key - A key string.
     * value - An object which is the value.
     * It should be of one of these types:
     * Boolean, Double, Integer, JSONArray, JSONObject, Long, String, or the JSONNull object.
     */
    private RequestOperation( Query type, String cinvId, String ref, Map<String, Object> data ) {
        this.ref = ref;
        this.type = type;
        this.obj = "task";
        this.convId = cinvId;
        this.data = data;
    }

//----------------------------------------------------------------------------------------------------------------------
    public JSONObject toJSONObject() {
        JSONObject res = new JSONObject()
                .element( "ref", ref )
                .element( "conv_id", convId )
                .element( "type", type )
                .element( "obj", obj );
        JSONObject res_data = new JSONObject();
        for ( String key : data.keySet() ) {
            res_data.element( key, data.get( key ) );
        }
        res.element( "data", res_data );
        return res;
    }
//----------------------------------------------------------------------------------------------------------------------
    private final String convId;
    private final String ref;
    private final Query type;
    private final String obj;
    private final Map<String, Object> data;

    private enum Query {
        create, modify
    }
}
