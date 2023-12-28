package com.liblab.hook.model;

import java.util.Map;

/**
 * Liblab's representation of a request
 */
public class Request {
    private String method;
    private String url;
    private String body;
    private Map<String, String> headers;

    public Request(String method, String url, String body, Map<String, String> headers) {
        this.method = method;
        this.url = url;
        this.body = body;
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
