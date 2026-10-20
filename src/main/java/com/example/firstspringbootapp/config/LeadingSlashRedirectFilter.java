package com.example.firstspringbootapp.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LeadingSlashRedirectFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String requestUri = request.getRequestURI();
		if (requestUri.startsWith("//")) {
			HttpServletRequest normalizedRequest = new HttpServletRequestWrapper(request) {
				@Override
				public String getRequestURI() {
					return normalizeLeadingSlashes(super.getRequestURI());
				}

				@Override
				public String getServletPath() {
					return normalizeLeadingSlashes(super.getServletPath());
				}

				@Override
				public String getPathInfo() {
					String pathInfo = super.getPathInfo();
					return pathInfo == null ? null : normalizeLeadingSlashes(pathInfo);
				}
			};
			filterChain.doFilter(normalizedRequest, response);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private static String normalizeLeadingSlashes(String path) {
		if (path == null || !path.startsWith("//")) {
			return path;
		}
		return "/" + path.replaceFirst("^/+", "");
	}
}
