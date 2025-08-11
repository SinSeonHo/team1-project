package com.matrix.service;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.StringTokenizer;
import com.matrix.comm.CookieUtil;
import com.matrix.comm.RequestMap;
import jakarta.servlet.http.HttpServletRequest;

public class CommService {
    public CommService() {}

    public RequestMap execute(HttpServletRequest request, String[] checkParam) throws Exception {
        RequestMap requestMap = this.getRequestMap(request);
        String exceptionKey = null;

        for (int i = 0; i < checkParam.length; ++i) {
            exceptionKey = checkParam[i];
            if (requestMap.get(exceptionKey) == null) {
                throw new Exception("request attribute not found[key:" + exceptionKey + "]");
            }
        }

        return requestMap;
    }

    private RequestMap getRequestMap(HttpServletRequest request) throws Exception {
        RequestMap requestMap = null;

        try {
            request.setCharacterEncoding("UTF-8");
            String aud7Token = CookieUtil.getAccessTokenByCookie(request, "bimatrix_accessToken");
            if (request.getAttribute("token") != null) {
                aud7Token = String.valueOf(request.getAttribute("token"));
            }

            requestMap = new RequestMap(aud7Token);
            Enumeration<?> paramNames = request.getAttributeNames();

            while (paramNames.hasMoreElements()) {
                String key = (String) paramNames.nextElement();
                Object value = request.getAttribute(key);
                requestMap.put(key, value);
            }

            return requestMap;
        } catch (Exception var7) {
            System.out.println("[aud7-embedded] request attribute setting error - [msg:"
                    + var7.getMessage() + "]");
            throw new Exception(var7.getMessage());
        }
    }

    public String createParams(RequestMap requestMap) throws Exception {
        String param = null;
        String sAuthList = (String) requestMap.get("auth_list");
        StringTokenizer st = new StringTokenizer(sAuthList, ",");
        String[] authList = new String[st.countTokens()];

        for (int i = 0; st.hasMoreTokens(); ++i) {
            String key = "\"" + st.nextToken() + "\"";
            authList[i] = key;
        }

        param = Arrays.toString(authList);
        return param;
    }
}
