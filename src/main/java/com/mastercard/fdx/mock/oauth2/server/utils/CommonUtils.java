package com.mastercard.fdx.mock.oauth2.server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Date;

@Slf4j
public class CommonUtils {

	private CommonUtils() {}

	private static final ObjectMapper mapper = new ObjectMapper();

	public static Timestamp now() {
		return new Timestamp(new Date().getTime());
	}

	public static Timestamp fromNow(int millisec) {
		return new Timestamp(new Date().getTime() + millisec);
	}

	public static RemoteJWKSet<SecurityContext> getRemoteJWKSet(String url) {
		try {
			return new RemoteJWKSet<>(new URL(url));
		} catch (MalformedURLException e) {
			log.error("Failed to obtain RemoteJWKSet.", e);
			return null;
		}
	}

	public static String getStringOfStackTrace(StackTraceElement[] stackTrace) {
		var sb = new StringBuilder();
		for (StackTraceElement e : stackTrace) {
			sb.append(e.toString()).append("/=/");
		}
		return sb.toString();
	}

	public static final <T> T convertObject(String data, Class<T> type) throws JsonProcessingException {
		return mapper.readValue(data, type);
	}

	public static final <T> T convertObjectWithErrHandling(String data, Class<T> type) {
		try {
			return convertObject(data, type);
		} catch (JsonProcessingException e) {
			log.error("Convert error while trying to convert string(JSON) to " + type.getSimpleName());
			return null;
		}
	}

	public static final <T> String toString(T t) throws JsonProcessingException {
		return mapper.writeValueAsString(t);
	}

	public static final <T> String toStringWithErrHandling(T t) {
		if (t == null) return null;
		String response = null;
		try {
			response = CommonUtils.toString(t);
		} catch (JsonProcessingException e) {
			log.error("Could not convert object to json string." + e.getMessage());
			return null;
		}
		return response.equals("{}") ? null : response;
	}
	
	public static String getFileContent(String resourceFolderPath, String fileName) {
		String requestFileData = null;
		try {

			ClassPathResource cpr = new ClassPathResource(resourceFolderPath + fileName);
			byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
			requestFileData = new String(bdata, StandardCharsets.UTF_8);
			return requestFileData;
		} catch (Exception e1) {
			log.error("Error while getting file", e1);
		}
		return "";
	}
}
