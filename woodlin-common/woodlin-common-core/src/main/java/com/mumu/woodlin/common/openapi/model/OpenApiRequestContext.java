package com.mumu.woodlin.common.openapi.model;

import com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm;
import com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI 请求上下文。
 *
 * @author mumu
 * @since 2026-04-21
 */
public class OpenApiRequestContext {

    private String method;
    private String path;
    private Map<String, List<String>> queryParameters = new LinkedHashMap<String, List<String>>();
    private byte[] body;
    private String timestamp;
    private String nonce;
    private String tenantId;
    private String accessKey;
    private String requestId;
    private ApiSignatureAlgorithm signatureAlgorithm;
    private ApiEncryptionAlgorithm encryptionAlgorithm;

    public String getMethod() {
        return method;
    }

    public OpenApiRequestContext setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getPath() {
        return path;
    }

    public OpenApiRequestContext setPath(String path) {
        this.path = path;
        return this;
    }

    public Map<String, List<String>> getQueryParameters() {
        return queryParameters;
    }

    public OpenApiRequestContext setQueryParameters(Map<String, List<String>> queryParameters) {
        this.queryParameters = new LinkedHashMap<String, List<String>>();
        if (queryParameters == null || queryParameters.isEmpty()) {
            return this;
        }
        for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
            this.queryParameters.put(
                entry.getKey(),
                entry.getValue() == null ? new ArrayList<String>() : new ArrayList<String>(entry.getValue())
            );
        }
        return this;
    }

    public OpenApiRequestContext addQueryParameter(String key, String value) {
        if (!queryParameters.containsKey(key)) {
            queryParameters.put(key, new ArrayList<String>());
        }
        queryParameters.get(key).add(value);
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public OpenApiRequestContext setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public OpenApiRequestContext setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getNonce() {
        return nonce;
    }

    public OpenApiRequestContext setNonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public OpenApiRequestContext setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public OpenApiRequestContext setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public OpenApiRequestContext setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public ApiSignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public OpenApiRequestContext setSignatureAlgorithm(ApiSignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
        return this;
    }

    public ApiEncryptionAlgorithm getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public OpenApiRequestContext setEncryptionAlgorithm(ApiEncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        return this;
    }
}
