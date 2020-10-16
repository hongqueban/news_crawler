package com.ustcinfo.hftnews.utils;

import java.util.UUID;

/**
 * 数据库插入时的使用的id
 */
public class UUIDUtil {

	/**
	 * 防止1毫秒内产生多次运算所加的0-999之间的一个数
	 */
    private static int gene = 0;

    /**
     * 生成UUID字符串
     *
     * @return
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static synchronized String getID() {
        String tmp = gene++ % 1000 + "";

        for (int i = tmp.length(); i < 3; i++) {
            tmp = "0" + tmp;
        }
        return "" + System.currentTimeMillis() + tmp;
    }
}
