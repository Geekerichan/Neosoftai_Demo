package com.eric.neusoftai.util;

import cn.hutool.crypto.digest.DigestUtil;

public class Md5Util {

    /**
     * MD5加密
     * @param str 明文
     * @return 密文
     */
    public static String encrypt(String str) {
        return DigestUtil.md5Hex(str);
    }
}
