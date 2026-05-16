package com.mumu.woodlin.system.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 支持重复读取和覆盖请求体的包装器。
 *
 * @author mumu
 * @since 2026-04-13
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private byte[] cachedBody;
    private final Map<String, List<String>> headerOverrides = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Set<String> removedHeaderNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * 构造函数。
     *
     * @param request 原始请求
     * @throws IOException 读取请求体异常
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = request.getInputStream().readAllBytes();
    }

    /**
     * 获取请求体。
     *
     * @return 请求体字节数组
     */
    public byte[] getCachedBody() {
        return cachedBody.clone();
    }

    /**
     * 覆盖请求体。
     *
     * @param body 新请求体
     */
    public void replaceBody(byte[] body) {
        this.cachedBody = body == null ? new byte[0] : body.clone();
    }

    /**
     * 删除指定前缀的请求头。
     *
     * @param prefix 请求头前缀
     */
    public void removeHeadersWithPrefix(String prefix) {
        Enumeration<String> names = super.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            if (startsWithIgnoreCase(name, prefix)) {
                removedHeaderNames.add(name);
            }
        }
        headerOverrides.keySet().removeIf(name -> startsWithIgnoreCase(name, prefix));
    }

    /**
     * 覆盖请求头。
     *
     * @param name  请求头名称
     * @param value 请求头值
     */
    public void setHeader(String name, String value) {
        if (name == null || name.isBlank()) {
            return;
        }
        removedHeaderNames.remove(name);
        headerOverrides.put(name, Collections.singletonList(value == null ? "" : value));
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 同步读取场景无需实现
            }

            @Override
            public int read() {
                return inputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public String getHeader(String name) {
        List<String> values = headerOverrides.get(name);
        if (values != null) {
            return values.isEmpty() ? null : values.get(0);
        }
        if (removedHeaderNames.contains(name)) {
            return null;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = headerOverrides.get(name);
        if (values != null) {
            return Collections.enumeration(values);
        }
        if (removedHeaderNames.contains(name)) {
            return Collections.emptyEnumeration();
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new LinkedHashSet<>();
        Enumeration<String> originalNames = super.getHeaderNames();
        while (originalNames != null && originalNames.hasMoreElements()) {
            String name = originalNames.nextElement();
            if (!removedHeaderNames.contains(name) && !headerOverrides.containsKey(name)) {
                names.add(name);
            }
        }
        names.addAll(new ArrayList<>(headerOverrides.keySet()));
        return Collections.enumeration(names);
    }

    @Override
    public int getContentLength() {
        return cachedBody.length;
    }

    @Override
    public long getContentLengthLong() {
        return cachedBody.length;
    }

    private boolean startsWithIgnoreCase(String value, String prefix) {
        if (value == null || prefix == null) {
            return false;
        }
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
