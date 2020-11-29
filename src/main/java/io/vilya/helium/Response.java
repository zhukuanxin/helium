/**
 * 
 */
package io.vilya.helium;

/**
 *
 * @author zhukuanxin <cafedada@vilya.io>
 * @created 2020-11-28 21:49:46
 */
public class Response {

    private int code;

    private String message;

    private Object data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    
    public static Response succeeded() {
        Response response = new Response();
        response.code = 0;
        return response;
    }
    
    public static Response succeeded(Object data) {
        Response response = new Response();
        response.code = 0;
        response.data = data;
        return response;
    }
    
    public static Response failed(String message) {
        Response response = new Response();
        response.code = 1;
        response.message = message;
        return response;
    }

}
