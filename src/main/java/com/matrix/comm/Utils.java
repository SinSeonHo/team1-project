package com.matrix.comm;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class Utils {
    public Utils() {
    }

    public static String initStr(String stOrigin, String initData) {
        String stReturn = "";
        if (stOrigin != null && stOrigin.length() > 0 && !"".equals(stOrigin)) {
            stReturn = stOrigin;
        } else if (initData != null && initData.length() > 0) {
            stReturn = initData;
        } else {
            stReturn = "";
        }

        return stReturn;
    }

    public static String getTokenValue(String cookieName, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        } else {
            Cookie[] var3 = cookies;
            int var4 = cookies.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Cookie cookie = var3[var5];
                if (cookie.getName().equals(cookieName) && !cookie.getValue().equals("null")
                        && !cookie.getValue().isEmpty()) {
                    return cookie.getValue();
                }
            }

            return null;
        }
    }

    public static boolean isNull(String str) {
        if (str == null) {
            return true;
        } else {
            String strRet = str.replaceAll(" ", "");
            return strRet.length() <= 0;
        }
    }

    public static Map<String, String> getJsonStringParser(String aud7Session) {
        String json2 = aud7Session.substring(1, aud7Session.length() - 1).trim();
        char[] array = json2.toCharArray();
        LinkedList<String> key = new LinkedList<>();
        StringBuilder output = new StringBuilder();
        boolean sep = false;

        for (int i = 0; i < array.length; ++i) {
            if (i == array.length - 1) {
                key.add(output.substring(1, output.toString().length()));
            } else {
                if (sep) {
                    sep = false;
                    key.add(output.substring(1, output.toString().length()));
                    output.setLength(0);
                } else if (array[i] == '"' && array[i + 1] == ','
                        || array[i] == '"' && array[i + 1] == ':') {
                    sep = true;
                    ++i;
                }

                if (!sep) {
                    output.append(array[i]);
                }
            }
        }

        Map<String, String> returnMap = new HashMap<>();

        for (int j = 0; j < key.size(); ++j) {
            returnMap.put(((String) key.get(j)).trim(), ((String) key.get(j + 1)).trim());
            ++j;
        }

        return returnMap;
    }

    public static JsonObject getJsonObjectByString(String jsonString) throws Exception {
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(jsonString).getAsJsonObject();
            return jsonObject;
        } catch (Exception var3) {
            throw var3;
        }
    }

    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    public static String conversionBase64EncodingValue(String strText) {
        String conversionStr = strText;

        try {
            Pattern pattern = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
            Matcher matcher = pattern.matcher(strText);
            if (matcher.find()) {
                byte[] param_decode = Base64.toByte(strText);
                conversionStr = (new String(param_decode)).trim();
                conversionStr = URLDecoder.decode(conversionStr, "utf-8");
            }

            return conversionStr;
        } catch (IllegalArgumentException var5) {
            System.out.println(
                    "[aud7-embedded] AUD7 Framework.jspf checkBase64Encoding IllegalArgumentException : "
                            + var5.getMessage());
            return strText;
        } catch (Exception var6) {
            System.out
                    .println("[aud7-embedded] AUD7 Framework.jspf checkBase64Encoding Exception : "
                            + var6.getMessage());
            return strText;
        }
    }
}
