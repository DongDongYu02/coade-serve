package cn.dong.nexus.common.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JavaToStringParser {
    public static List<Map<String, Object>> parseListMap(String text) {
        Object value = new Parser(text).parseValue();
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("根节点不是List");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("List元素不是Map");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> casted = (Map<String, Object>) map;
            result.add(casted);
        }
        return result;
    }

    private static class Parser {
        private final String s;
        private int i = 0;

        Parser(String s) {
            this.s = s;
        }

        Object parseValue() {
            skipBlank();
            if (i >= s.length()) {
                return null;
            }

            char ch = s.charAt(i);
            if (ch == '[') {
                return parseList();
            }
            if (ch == '{') {
                return parseMap();
            }
            if (startsWith("null")) {
                i += 4;
                return null;
            }
            return parseScalar();
        }

        private List<Object> parseList() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipBlank();

            if (peek() == ']') {
                i++;
                return list;
            }

            while (i < s.length()) {
                list.add(parseValue());
                skipBlank();

                char ch = peek();
                if (ch == ',') {
                    i++;
                    skipBlank();
                    continue;
                }
                if (ch == ']') {
                    i++;
                    break;
                }
                throw new IllegalArgumentException("List解析失败，位置: " + i);
            }
            return list;
        }

        private Map<String, Object> parseMap() {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipBlank();

            if (peek() == '}') {
                i++;
                return map;
            }

            while (i < s.length()) {
                String key = parseKey();
                expect('=');
                Object value = parseValue();
                map.put(key, value);

                skipBlank();
                char ch = peek();
                if (ch == ',') {
                    i++;
                    skipBlank();
                    continue;
                }
                if (ch == '}') {
                    i++;
                    break;
                }
                throw new IllegalArgumentException("Map解析失败，位置: " + i);
            }
            return map;
        }

        private String parseKey() {
            skipBlank();
            int start = i;
            while (i < s.length() && s.charAt(i) != '=') {
                i++;
            }
            if (i >= s.length()) {
                throw new IllegalArgumentException("Key解析失败，位置: " + i);
            }
            return s.substring(start, i).trim();
        }

        private String parseScalar() {
            int start = i;
            while (i < s.length()) {
                char ch = s.charAt(i);
                if (ch == ',' || ch == '}' || ch == ']') {
                    break;
                }
                i++;
            }
            return s.substring(start, i).trim();
        }

        private void skipBlank() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        private void expect(char ch) {
            skipBlank();
            if (i >= s.length() || s.charAt(i) != ch) {
                throw new IllegalArgumentException("期望字符 '" + ch + "'，位置: " + i);
            }
            i++;
        }

        private char peek() {
            skipBlank();
            if (i >= s.length()) {
                return '\0';
            }
            return s.charAt(i);
        }

        private boolean startsWith(String str) {
            return s.startsWith(str, i);
        }
    }
}
