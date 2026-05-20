package com.appsc.salmo23.login;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Utils {
    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }
}

