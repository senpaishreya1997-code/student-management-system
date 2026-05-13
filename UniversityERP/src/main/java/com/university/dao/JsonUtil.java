package com.university.dao;

import java.util.*;

public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escape((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map) {
            Map<?,?> map = (Map<?,?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?,?> e : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escape(e.getKey().toString())).append("\":").append(toJson(e.getValue()));
            }
            return sb.append("}").toString();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            return sb.append("]").toString();
        }
        return "\"" + escape(obj.toString()) + "\"";
    }

    public static String ok(String message) {
        return "{\"success\":true,\"message\":\"" + escape(message) + "\"}";
    }

    public static String error(String message) {
        return "{\"success\":false,\"error\":\"" + escape(message) + "\"}";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    public static Map<String,Object> parseBody(jakarta.servlet.http.HttpServletRequest req) throws Exception {
        StringBuilder sb = new StringBuilder();
        String line;
        try (java.io.BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return parseJson(sb.toString());
    }

    private static Map<String,Object> parseJson(String json) {
        Map<String,Object> map = new LinkedHashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1, json.length() - 1).trim();
        while (!json.isEmpty()) {
            int keyStart = json.indexOf('"') + 1;
            int keyEnd = json.indexOf('"', keyStart);
            String key = json.substring(keyStart, keyEnd);
            json = json.substring(keyEnd + 1).trim();
            if (json.startsWith(":")) json = json.substring(1).trim();
            String value;
            if (json.startsWith("\"")) {
                int valEnd = json.indexOf('"', 1);
                while (valEnd > 0 && json.charAt(valEnd - 1) == '\\') valEnd = json.indexOf('"', valEnd + 1);
                value = json.substring(1, valEnd);
                json = json.substring(valEnd + 1).trim();
            } else {
                int end = json.indexOf(',');
                int end2 = json.indexOf('}');
                int valEnd = (end < 0) ? end2 : (end2 < 0 ? end : Math.min(end, end2));
                value = json.substring(0, valEnd < 0 ? json.length() : valEnd).trim();
                json = valEnd < 0 ? "" : json.substring(valEnd).trim();
            }
            map.put(key, value);
            if (json.startsWith(",")) json = json.substring(1).trim();
        }
        return map;
    }
}
