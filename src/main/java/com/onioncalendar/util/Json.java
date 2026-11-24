package com.onioncalendar.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 외부 라이브러리 없이 쓰는 최소 JSON 파서/작성기.
 * 객체 → Map&lt;String,Object&gt;, 배열 → List&lt;Object&gt;, 숫자 → Double,
 * 문자열 → String, true/false → Boolean, null → null.
 */
public final class Json {
    private final String s;
    private int i;

    private Json(String s) { this.s = s; }

    /** 문자열을 파싱한다. 형식이 틀리면 IllegalArgumentException. */
    public static Object parse(String text) {
        Json p = new Json(text);
        p.ws();
        Object v = p.value();
        p.ws();
        if (p.i != p.s.length()) throw p.error("끝에 남는 문자가 있음");
        return v;
    }

    private Object value() {
        if (i >= s.length()) throw error("값이 없음");
        char c = s.charAt(i);
        if (c == '{') return object();
        if (c == '[') return array();
        if (c == '"') return string();
        if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
        if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
        if (s.startsWith("null", i)) { i += 4; return null; }
        return number();
    }

    private Map<String, Object> object() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++; ws();
        if (peek() == '}') { i++; return m; }
        while (true) {
            ws();
            if (peek() != '"') throw error("키는 문자열이어야 함");
            String key = string();
            ws(); expect(':'); ws();
            m.put(key, value());
            ws();
            char c = next();
            if (c == '}') return m;
            if (c != ',') throw error("',' 또는 '}' 필요");
        }
    }

    private List<Object> array() {
        List<Object> list = new ArrayList<>();
        i++; ws();
        if (peek() == ']') { i++; return list; }
        while (true) {
            ws();
            list.add(value());
            ws();
            char c = next();
            if (c == ']') return list;
            if (c != ',') throw error("',' 또는 ']' 필요");
        }
    }

    private String string() {
        expect('"');
        StringBuilder b = new StringBuilder();
        while (true) {
            char c = next();
            if (c == '"') return b.toString();
            if (c == '\\') {
                char e = next();
                switch (e) {
                    case 'n': b.append('\n'); break;
                    case 't': b.append('\t'); break;
                    case 'r': b.append('\r'); break;
                    case 'b': b.append('\b'); break;
                    case 'f': b.append('\f'); break;
                    case 'u': b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                    default: b.append(e);
                }
            } else {
                b.append(c);
            }
        }
    }

    private Double number() {
        int st = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        if (st == i) throw error("알 수 없는 값");
        return Double.parseDouble(s.substring(st, i));
    }

    private void ws() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
    private char peek() { if (i >= s.length()) throw error("갑자기 끝남"); return s.charAt(i); }
    private char next() { char c = peek(); i++; return c; }
    private void expect(char c) { if (next() != c) throw error("'" + c + "' 필요"); }
    private IllegalArgumentException error(String msg) {
        return new IllegalArgumentException("JSON 오류(" + i + "번째 문자): " + msg);
    }

    /** 값을 들여쓴 JSON 문자열로 만든다. */
    public static String write(Object v) {
        StringBuilder b = new StringBuilder();
        write(v, b, 0);
        return b.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(Object v, StringBuilder b, int depth) {
        if (v == null) { b.append("null"); return; }
        if (v instanceof String) { quote((String) v, b); return; }
        if (v instanceof Boolean) { b.append(v); return; }
        if (v instanceof Number) {
            double d = ((Number) v).doubleValue();
            if (d == Math.rint(d) && !Double.isInfinite(d)) b.append((long) d); else b.append(d);
            return;
        }
        String pad = "  ".repeat(depth + 1), end = "  ".repeat(depth);
        if (v instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) v;
            if (m.isEmpty()) { b.append("{}"); return; }
            b.append("{\n");
            int k = 0;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                b.append(pad); quote(e.getKey(), b); b.append(": ");
                write(e.getValue(), b, depth + 1);
                b.append(++k < m.size() ? ",\n" : "\n");
            }
            b.append(end).append('}');
            return;
        }
        if (v instanceof List) {
            List<Object> list = (List<Object>) v;
            if (list.isEmpty()) { b.append("[]"); return; }
            b.append("[\n");
            for (int k = 0; k < list.size(); k++) {
                b.append(pad);
                write(list.get(k), b, depth + 1);
                b.append(k + 1 < list.size() ? ",\n" : "\n");
            }
            b.append(end).append(']');
            return;
        }
        throw new IllegalArgumentException("JSON으로 쓸 수 없는 타입: " + v.getClass());
    }

    private static void quote(String str, StringBuilder b) {
        b.append('"');
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c)); else b.append(c);
            }
        }
        b.append('"');
    }
}
