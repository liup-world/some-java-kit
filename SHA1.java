package com.bobsystem.exercise.commons;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Hex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.MessageDigest;

public class SHA1 {

	//region CONSTANT
	private static final Logger LOGGER = LoggerFactory.getLogger(SHA1.class);
	private static final MessageDigest SHA1;
	//endregion

	static {
		try {
			SHA1 = MessageDigest.getInstance("SHA-1");
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			throw new Error(ex);
		}
	}

	//region member methods
	public static String encrypt(String text) {
		return encrypt(text, Charset.forName("utf-8"));
	}

	public static String encrypt(String text, Charset charset) {
		if (StringUtils.isBlank(text)) return "";
		byte[] bytes = text.getBytes(charset);
		synchronized (SHA1) {
			SHA1.update(bytes);
			bytes = SHA1.digest();
		}
		return Hex.toHexString(bytes);
	}

	public static void prepare() {
		// 目的是执行静态块，静态块中如果异常，手动引发崩溃
	}
	//endregion

	//region constructors
	private SHA1() { }
	//endregion
}
