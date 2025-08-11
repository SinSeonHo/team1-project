package com.matrix.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.matrix.comm.CookieUtil;
import com.matrix.comm.EmbeddedException;
import com.matrix.comm.RequestMap;
import com.matrix.comm.UrlConnection;
import com.matrix.comm.Utils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TokenService extends CommService {
    private final String tokenUrl = "/api/auth/sign/token/rest";

    public TokenService() {}

    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String result = null;
        boolean var21 = false;

        String[] cookies;
        try {
            var21 = true;
            cookies = new String[] {"aud7_server_url", "is_ssl", "user_id"};
            RequestMap requestMap = super.execute(request, cookies);
            boolean isSSL = (Boolean) requestMap.get("is_ssl");
            String serverUrl = (String) requestMap.get("aud7_server_url");
            String apiUrl = serverUrl + "/api/auth/sign/token/rest";
            String jsonParams = this.createJsonParams(requestMap);
            String aud7Token = requestMap.getAccessToken();
            String responseMsg =
                    (new UrlConnection()).execute(apiUrl, jsonParams, isSSL, aud7Token);
            System.out.println("====responseMsg:" + responseMsg);
            JsonObject jsonObject = Utils.getJsonObjectByString(responseMsg);
            if (!jsonObject.get("success").getAsBoolean()) {
                String rtnCode = String.valueOf(jsonObject.get("bizRtnCode"));
                String rtnMsg = String.valueOf(jsonObject.get("message"));
                if (rtnMsg.replace("\"", "").equals("SSO-002")) {
                    rtnCode = rtnMsg;
                    rtnMsg = " user not found [userID : " + requestMap.get("user_id") + "]";
                }

                throw new EmbeddedException(rtnCode, rtnMsg);
            }

            JsonArray jsonArray = jsonObject.getAsJsonArray("result");
            JsonObject resultObj = (JsonObject) jsonArray.get(0);
            result = String.valueOf(resultObj.getAsJsonObject("tokenDto").get("accessToken"))
                    .replaceAll("\\\"", "");
            var21 = false;
        } catch (EmbeddedException var22) {
            System.out.println("[EmbeddedException] " + var22.getMessage());
            throw var22;
        } catch (Exception var23) {
            var23.printStackTrace();
            throw var23;
        } finally {
            if (var21) {
                if (result == null) {
                    result = CookieUtil.getAccessTokenByCookie(request, "bimatrix_accessToken");
                }

                if (result != null) {
                    CookieUtil.deleteCookie(new String[] {"bimatrix_accessToken"}, "", response);
                    Cookie createCookie =
                            CookieUtil.createCookie("bimatrix_accessToken", result, -1);
                    response.addCookie(createCookie);
                    response.setCharacterEncoding("utf-8");
                }

            }
        }

        if (result == null) {
            result = CookieUtil.getAccessTokenByCookie(request, "bimatrix_accessToken");
        }

        if (result != null) {
            cookies = new String[] {"bimatrix_accessToken"};
            CookieUtil.deleteCookie(cookies, "", response);
            Cookie createCookie = CookieUtil.createCookie("bimatrix_accessToken", result, -1);
            response.addCookie(createCookie);
            response.setCharacterEncoding("utf-8");
        }

        return result;
    }

    private String createJsonParams(RequestMap requestMap) throws Exception {
        String jsonParam = null;
        JsonObject jsonObject = new JsonObject();
        String userId = (String) requestMap.get("user_id");
        if (Utils.isNull(userId)) {
            throw new Exception("required attribute fail [key:" + userId + "]");
        } else {
            jsonObject.addProperty("duplication_login", "N");
            jsonObject.addProperty("flag", "sso");
            jsonObject.addProperty("userid", userId);
            jsonObject.addProperty("rsakey", "");
            jsonObject.addProperty("lang_code", "");
            jsonParam = jsonObject.toString();
            return jsonParam;
        }
    }
}
