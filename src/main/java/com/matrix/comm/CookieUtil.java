package com.matrix.comm;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {
    public static final String AUD7_ACCESS_TOKEN = "bimatrix_accessToken";

    public CookieUtil() {}

    public static Cookie createCookie(String cookieName, String value, int magAge) {
        Cookie token = new Cookie(cookieName, value);
        token.setMaxAge(magAge);
        token.setPath("/");
        return token;
    }

    public static Cookie getCookie(HttpServletRequest req, String cookieName) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        } else {
            Cookie[] var3 = cookies;
            int var4 = cookies.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Cookie cookie = var3[var5];
                if (cookie.getName().equals(cookieName) && !cookie.getValue().equals("null")
                        && !cookie.getValue().isEmpty()) {
                    return cookie;
                }
            }

            return null;
        }
    }

    public static String getAccessTokenByCookie(HttpServletRequest request, String cookieName) {
        Cookie cookie = getCookie(request, cookieName);
        String jwt = null;
        if (cookie != null) {
            jwt = cookie.getValue();
        }

        return jwt;
    }

    public static void deleteCookie(String[] cookiesToClear, String cookiePath,
            HttpServletResponse response) {
        String[] var3 = cookiesToClear;
        int var4 = cookiesToClear.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String cookieName = var3[var5];
            Cookie cookie = new Cookie(cookieName, (String) null);
            if (!Utils.hasLength(cookiePath)) {
                cookiePath = "/";
            }

            cookie.setPath(cookiePath);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }

    }
}
