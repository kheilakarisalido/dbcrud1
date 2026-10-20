package com.example.firstspringbootapp.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import com.example.firstspringbootapp.entity.ItemEntity;
import com.example.firstspringbootapp.repository.ItemRepository;

@Service
public class ItemService {

	private final ItemRepository itemRepository;

	public ItemService(ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	//C - Create
	public ItemEntity postItem(ItemEntity item) {
		return itemRepository.save(item);
	}

	//R - Read
	public List<ItemEntity> getAllItems() {
		return itemRepository.findAll();
	}

	//U - Update
	public ItemEntity putItem(int id, ItemEntity newItemDetails) {
		ItemEntity existingItem = itemRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item " + id + " does not exist!"));

		existingItem.setName(newItemDetails.getName());
		existingItem.setUnit(newItemDetails.getUnit());
		existingItem.setPrice(newItemDetails.getPrice());

		return itemRepository.save(existingItem);
	}

	//D - Delete
	public String deleteItem(int id) {
		if (!itemRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item " + id + " does not exist!");
		}

		itemRepository.deleteById(id);
		return "Item " + id + " is successfully deleted!";
	}

	@RestControllerAdvice
	public static class ApiExceptionHandler {

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

	@Component
	public static class LeadingSlashRedirectFilter extends OncePerRequestFilter {

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
}
