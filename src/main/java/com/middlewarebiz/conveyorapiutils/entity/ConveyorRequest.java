
package com.middlewarebiz.conveyorapiutils.entity;

/**
 * @author dn300986zav
 */
public class ConveyorRequest {
//---------------------------------------------------------------------------------------------------------------------
    /**
     * Reaqest constructor
     * @param baseUrl - URL conveyor https://{CONV_SERVER}:{PORT}/api/{VERSION}/{FORMAT}
     * @param apiLogin - login for  API
     * @param mes - message
     * @return
     * @throws Exception
     */
    public static ConveyorRequest getRequest( String baseUrl, String apiLogin, ConveyorMessage mes ) throws Exception {
        String url = new StringBuilder()
                .append( baseUrl )
                .append( slash ).append( apiLogin )
                .append( slash ).append( mes.time )
                .append( slash ).append( mes.signCode )
                .toString();

        return new ConveyorRequest( url, mes.body );
    }
//----------------------------------------------------------------------------------------------------------------------

    private ConveyorRequest( String url, String content ) {
        this.url = url;
        this.content = content;
    }
//----------------------------------------------------------------------------------------------------------------------
    public final String url;
    public final String content;
    public static final String slash = "/";
//----------------------------------------------------------------------------------------------------------------------
}
