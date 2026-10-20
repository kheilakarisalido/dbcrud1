package com.example.firstspringbootapp.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Map<String, String>> handleMethodNotSupported(
			HttpRequestMethodNotSupportedException exception) {
		HttpHeaders headers = new HttpHeaders();
		Set<HttpMethod> supportedMethods = exception.getSupportedHttpMethods();
		if (supportedMethods != null) {
			headers.setAllow(supportedMethods);
		}

		String message = "Method " + exception.getMethod() + " is not supported for this endpoint.";
		return new ResponseEntity<>(Map.of("error", message), headers, HttpStatus.METHOD_NOT_ALLOWED);
	}
}
