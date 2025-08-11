package com.matrix.service;

import com.google.gson.JsonObject;
import com.matrix.comm.RequestMap;
import com.matrix.comm.UrlConnection;
import com.matrix.comm.Utils;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AudSessionService extends CommService {
    private final String tokenUrl = "/api/user/user/aud7/session/attribute";

    public AudSessionService() {
    }

    public Map<String, String> execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Map<String, String> audSessionInfos = new HashMap<>();

        try {
            String[] checkParam = new String[] { "aud7_server_url", "is_ssl" };
            RequestMap requestMap = super.execute(request, checkParam);
            boolean isSSL = (Boolean) requestMap.get("is_ssl");
            String serverUrl = (String) requestMap.get("aud7_server_url");
            String apiUrl = serverUrl + "/api/user/user/aud7/session/attribute";
            String sAuthList = (String) requestMap.get("auth_list");
            String sParam;
            if (sAuthList == null) {
                sParam = "IS_SOURCE_DEBUG_MODE,PRIVATE_VERSION,THEME_PRIVATE_VERSION,LANG_CODE,LANG_CODE,p@STUDIO_ACTIVE_VER,p@AUD_ACTIVE_VER,p@STUDIO_SKIN_VER,PORTAL_THEME_CSS_PATH,PORTAL_THEME_IMG_PATH";
                requestMap.setString("auth_list", sParam);
            }

            sParam = super.createParams(requestMap);
            String responseMsg = (new UrlConnection()).execute(apiUrl, sParam, isSSL,
                    requestMap.getAccessToken());
            JsonObject jsonObject = Utils.getJsonObjectByString(responseMsg);
            String audGlobalParam = this.createAUDGlobalVariable(request);
            audSessionInfos.put("CUSTOM_PARAM", audGlobalParam);
            this.audFrameworkPathSetting(jsonObject, audSessionInfos, serverUrl);
            return audSessionInfos;
        } catch (Exception var14) {
            throw var14;
        }
    }

    private String createAUDGlobalVariable(HttpServletRequest request) {
        List<Object> _params = new ArrayList<>();
        String jsonVParams = null;

        try {
            Enumeration<?> paramNames = request.getParameterNames();

            while (true) {
                String key;
                String val;
                do {
                    if (!paramNames.hasMoreElements()) {
                        Enumeration<?> paramNames1 = request.getAttributeNames();

                        while (true) {
                            do {
                                if (!paramNames1.hasMoreElements()) {
                                    StringBuffer customParams = new StringBuffer();
                                    customParams.append("[");
                                    String doubleQu = "\"";

                                    for (int i = 0; i < _params.size(); ++i) {
                                        Map<String, Object> subMap = (Map) _params.get(i);
                                        customParams.append("{");
                                        int index = 0;
                                        boolean isNum = false;

                                        for (Iterator var14 = subMap.keySet().iterator(); var14
                                                .hasNext(); ++index) {
                                            String paramKey = (String) var14.next();
                                            customParams.append(doubleQu);
                                            customParams.append(paramKey);
                                            customParams.append(doubleQu);
                                            customParams.append(":");
                                            if (paramKey.indexOf("KEY") == 0
                                                    && String.valueOf(subMap.get(paramKey))
                                                            .indexOf("VN") == 0) {
                                                isNum = true;
                                            }

                                            if (isNum && paramKey.indexOf("VALUE") == 0) {
                                                customParams.append(subMap.get(paramKey));
                                                isNum = false;
                                            } else {
                                                customParams.append(doubleQu);
                                                customParams.append(subMap.get(paramKey));
                                                customParams.append(doubleQu);
                                            }

                                            if (index < subMap.size() - 1) {
                                                customParams.append(",");
                                            }
                                        }

                                        customParams.append("}");
                                        if (_params.size() - 1 > i) {
                                            customParams.append(",");
                                        }
                                    }

                                    customParams.append("]");
                                    jsonVParams = customParams.toString();
                                    return jsonVParams;
                                }

                                key = (String) paramNames1.nextElement();
                            } while (key.indexOf("VS") != 0 && key.indexOf("VN") != 0
                                    && key.indexOf("V_") != 0);

                            val = (String) request.getAttribute(key);
                            val = Utils.conversionBase64EncodingValue(val);
                            Map<String, Object> p = new LinkedHashMap();
                            p.put("KEY", key);
                            p.put("VALUE", val);
                            _params.add(p);
                        }
                    }

                    key = (String) paramNames.nextElement();
                } while (key.indexOf("VS") != 0 && key.indexOf("VN") != 0
                        && key.indexOf("V_") != 0);

                val = request.getParameter(key);
                val = Utils.conversionBase64EncodingValue(val);
                Map<String, Object> p = new LinkedHashMap();
                p.put("KEY", key);
                p.put("VALUE", val);
                _params.add(p);
            }
        } catch (Exception var16) {
            System.out.println(
                    "[aud7-embedded] AudSessionService :: createAUDGlobalVariable() error - "
                            + var16.getMessage());
            return jsonVParams;
        }
    }

    private void audFrameworkPathSetting(JsonObject aud7SessionMap,
            Map<String, String> audSessionInfos, String aud7ServerUrl) {
        String CSS_PATH = "/css";
        String CSS_DEBUG_PATH = "/css_debug";
        String IMAGE_PATH = "/image";
        String THEME_PATH = "/theme";
        Boolean RELEASE_NUMBER = true;
        String version = aud7SessionMap.get("AUD_VERSION") != null
                ? aud7SessionMap.get("AUD_VERSION").getAsString()
                : null;
        String PRIVATE_VERSION;
        String THEME_PRIVATE_VERSION;
        if (version != null && !version.isEmpty()) {
            System.out.println("[AUD7] i-AUD version:" + version);
            String[] splitVersion = version.split("\\.");
            if (splitVersion.length > 3) {
                PRIVATE_VERSION = splitVersion[2];
                if (Integer.parseInt(PRIVATE_VERSION) >= 400) {
                    THEME_PRIVATE_VERSION = splitVersion[3];
                    THEME_PRIVATE_VERSION = THEME_PRIVATE_VERSION.split("-")[1];
                    long buildNo = Long.parseLong(THEME_PRIVATE_VERSION);
                    if (buildNo <= 202306231349L) {
                        RELEASE_NUMBER = false;
                    }
                }
            }
        } else {
            version = aud7SessionMap.get("RELEASE_NO") != null
                    ? aud7SessionMap.get("RELEASE_NO").getAsString()
                    : null;
            version = version.replace("REL", "");
            System.out.println("[AUD7] Release version:" + version);
            if (version != null && !version.isEmpty()) {
                long releaseNo = Long.parseLong(version);
                if (releaseNo < 704002023071300L) {
                    RELEASE_NUMBER = false;
                }
            }
        }

        Boolean IS_SOURCE_DEBUG_MODE = aud7SessionMap.get("IS_SOURCE_DEBUG_MODE") != null
                ? aud7SessionMap.get("IS_SOURCE_DEBUG_MODE").getAsBoolean()
                : false;
        PRIVATE_VERSION = aud7SessionMap.get("PRIVATE_VERSION").getAsString();
        THEME_PRIVATE_VERSION = aud7SessionMap.get("THEME_PRIVATE_VERSION").getAsString();
        String VERSION_PATH = "/" + aud7SessionMap.get("STUDIO_ACTIVE_VER");
        String AUD_VERSION_PATH = "/" + aud7SessionMap.get("AUD_ACTIVE_VER");
        if (PRIVATE_VERSION != null && PRIVATE_VERSION.length() > 0) {
            VERSION_PATH = "/" + PRIVATE_VERSION;
            AUD_VERSION_PATH = "/" + PRIVATE_VERSION;
        }

        (new StringBuilder()).append(aud7ServerUrl).append("/iStudio").append(VERSION_PATH)
                .toString();
        String AUD7_FULL_PATH = aud7ServerUrl + "/AUD" + AUD_VERSION_PATH;
        String STUDIO_SKIN_VER = aud7SessionMap.get("STUDIO_SKIN_VER").getAsString();
        String SKIN_PATH = "/" + STUDIO_SKIN_VER;
        String DEFAULT_LANG_CODE = "ko";
        String LANG_CODE = aud7SessionMap.get("LANG_CODE").getAsString();
        String langSeperator;
        if (LANG_CODE != null) {
            String[] skinSplit = STUDIO_SKIN_VER.split("-");
            if (!"en".equals(LANG_CODE) && !"ko".equals(LANG_CODE) && !"jp".equals(LANG_CODE)) {
                LANG_CODE = DEFAULT_LANG_CODE;
            }

            if (skinSplit.length > 2) {
                String SKIN_NAME = skinSplit[0] + "-" + skinSplit[1] + "-" + LANG_CODE;
                SKIN_PATH = "/" + SKIN_NAME;
            } else {
                langSeperator = "-";
                if (RELEASE_NUMBER) {
                    langSeperator = "/";
                }

                SKIN_PATH = "/" + STUDIO_SKIN_VER + langSeperator + LANG_CODE;
            }
        }

        if (THEME_PRIVATE_VERSION != null && THEME_PRIVATE_VERSION.length() > 0) {
            SKIN_PATH = "/" + THEME_PRIVATE_VERSION;
        }

        String AUD7_SKIN_PATH = AUD7_FULL_PATH + THEME_PATH + SKIN_PATH;
        langSeperator = AUD7_SKIN_PATH + CSS_PATH;
        if (IS_SOURCE_DEBUG_MODE) {
            langSeperator = AUD7_SKIN_PATH + CSS_DEBUG_PATH;
        }

        (new StringBuilder()).append(AUD7_SKIN_PATH).append(IMAGE_PATH).toString();
        (new StringBuilder()).append(AUD7_FULL_PATH).append(CSS_PATH).toString();
        (new StringBuilder()).append(AUD7_FULL_PATH).append(IMAGE_PATH).toString();
        String PORTAL_THEME_IMG_PATH = aud7SessionMap.get("PORTAL_THEME_IMG_PATH").getAsString();
        String PORTAL_THEME_CSS_PATH = aud7SessionMap.get("PORTAL_THEME_CSS_PATH").getAsString();
        audSessionInfos.put("AUD7_FULL_PATH", AUD7_FULL_PATH.replaceAll("\\\"", ""));
        audSessionInfos.put("AUD7_SKIN_CSS_PATH", langSeperator.replaceAll("\\\"", ""));
        audSessionInfos.put("IS_SOURCE_DEBUG_MODE", "N");
        if (IS_SOURCE_DEBUG_MODE) {
            audSessionInfos.put("IS_SOURCE_DEBUG_MODE", "Y");
        }

        audSessionInfos.put("PORTAL_THEME_CSS_PATH", PORTAL_THEME_CSS_PATH.replaceAll("\\\"", ""));
    }
}
