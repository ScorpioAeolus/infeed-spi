package com.infeed.spi.common;

import java.util.HashMap;
import java.util.Map;

/**
 * map操作工具类
 *
 * @author typhoon
 * @date 2024-08-15 19:48 Thursday
 * @since V1.0.0
 */
public class MapUtil {

    public static final <K,T extends Object> String toPlaintString(Map<K,T> map) {
        if (null == map || map.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Map.Entry<K, T> entry : map.entrySet()) {
            builder.append(",")
                    .append("\"")
                    .append(entry.getKey())
                    .append("\"")
                    .append(":")
                    .append("\"")
                    .append(entry.getValue())
                    .append("\"");
        }
        builder.append("}");
        return builder.toString().replaceFirst(",","");
    }

    public static final <K,T extends Object> Map<K,T> emptyMap() {
        return new HashMap<>();
    }

    public static final boolean isEmpty(Map map) {
        return null == map || map.isEmpty();
    }

    public static void main(String[] args) {

        Map<String,String> map = new HashMap<>();
        map.put("returnId","son123");
        map.put("returnLines",null);
        System.out.println(toPlaintString(map));

    }


}

