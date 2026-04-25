package cn.dong.nexus.core.util;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequestUtil {

    public static String  buildParams(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap == null || parameterMap.isEmpty()) {
            return "";
        }
        return parameterMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Arrays.toString(entry.getValue())
                ))
                .toString();
    }

    public static String  getRequestBody(HttpServletRequest request) {
        if (request instanceof org.springframework.web.util.ContentCachingRequestWrapper wrapper) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length == 0) {
                return "";
            }
            return new String(buf, StandardCharsets.UTF_8);
        }
        return "";
    }
}
