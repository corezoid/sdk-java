
package com.middlewarebiz.conveyorapiutils.entity;

import java.util.Map;
import net.sf.json.JSONObject;

/** 
 * Response operation
 * @author dn300986zav
 */
public class ResponseOperation {
//----------------------------------------------------------------------------------------------------------------------
    /**
     * fail operation
     * @param convId - conveyor id
     * @param ref- reference
     * @param data -Map<key,value>
     * key - A key string.
     * value - An object which is the value.
     * It should be of one of these types:
     * Boolean, Double, Integer, JSONArray, JSONObject, Long, String, or the JSONNull object.
     * @return
     */
    public static ResponseOperation fail( String convId, String ref, Map<String, Object> data ) {
        return new ResponseOperation( ResultType.fail, convId, ref, data );
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * ok operation
     * @param convId - conveyor id
     * @param ref- reference
     * @param data -Map<key,value>
     * key - A key string.
     * value - An object which is the value.
     * It should be of one of these types:
     * Boolean, Double, Integer, JSONArray, JSONObject, Long, String, or the JSONNull object.
     * @return 
     */
    public static ResponseOperation ok( String convId, String ref, Map<String, Object> data ) {
        return new ResponseOperation( ResultType.ok, convId, ref, data );
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param proc - результат
     * @param convId - ид конвейера
     * @param ref- референс
     * @param data -Map<key,value>
     * key - A key string.
     * value - An object which is the value.
     * It should be of one of these types:
     * Boolean, Double, Integer, JSONArray, JSONObject, Long, String, or the JSONNull object.
     */
    private ResponseOperation( ResultType proc, String convId, String ref, Map<String, Object> data ) {
        this.ref = ref;
        this.convId = convId;
        this.data = data;
        this.proc = proc;
    }

//----------------------------------------------------------------------------------------------------------------------
    public JSONObject toJSONObject() {
        JSONObject res = new JSONObject()
                .element( "ref", ref )
                .element( "conv_id", convId )
                .element( "proc", proc );
        JSONObject res_data = new JSONObject();
        for ( String key : data.keySet() ) {
            res_data.element( key, data.get( key ) );
        }
        res.element( "res_data", res_data );
        return res;
    }
//----------------------------------------------------------------------------------------------------------------------
    private final String convId;
    private final String ref;
    private final ResultType proc;
    private final Map<String, Object> data;

    private enum ResultType {
        ok, fail
    }
}
