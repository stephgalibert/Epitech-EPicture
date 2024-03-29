package fr.epicture.epicture.api.imgur.utils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class ImgurUtils {

    public static Map<String, String> getQueryMap(String url) {
        url = url.replaceAll(".*#", "");
        final String[] params = url.split("&");
        final Map<String, String> map = new HashMap<>();
        for (String param : params) {
            final String[] stab = param.split("=");
            if (stab.length >= 2)
            {
                String name = stab[0];
                String value = stab[1];
                map.put(name, value);
            }
        }
        return map;
    }

    public static long getTime()
    {
        return new Timestamp(System.currentTimeMillis()).getTime();
    }
}
