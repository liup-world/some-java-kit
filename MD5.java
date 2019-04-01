package com.bobsystem.exercise.commons;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Hex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

public class MD5 {

    //region CONSTANT
    private static final Logger LOGGER = LoggerFactory.getLogger(MD5.class);
    private static final MessageDigest MD5;
    //endregion

    static {
        try {
            MD5 = MessageDigest.getInstance("MD5");
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new Error(ex);
        }
    }

    //region static methods
    public static String encrypt(String text) {
        return encrypt(text, "");
    }

    public static String encrypt(String text, String charset) {
        if (StringUtils.isBlank(text)) return "";
        try {
            byte[] bytes;
            if (StringUtils.isNotBlank(charset)) {
                bytes = text.getBytes(charset);
            }
            else {
                bytes = text.getBytes();
            }
            synchronized (MD5) {
                bytes = MD5.digest(bytes);
            }
            return Hex.toHexString(bytes);
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return "";
    }

    public static void prepare() {
        // 目的是执行静态块，静态块中如果异常，手动引发崩溃
    }
    //endregion

    //region constructors
    private MD5() { }
    //endregion
}
