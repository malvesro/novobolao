package com.opendev.bolao.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Daniel Rochetti
 */
public final class SegurancaUtils {

	private static final String ENCODING = "UTF-8";
	private static final String DEFAULT_ALGORITHM = "SHA-1";

	public static String codificar(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
			digest.update(password.getBytes(ENCODING));
			byte[] raw = digest.digest();
			return new String(Base64.encodeBase64(raw));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	
}
