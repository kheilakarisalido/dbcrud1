package com.example.firstspringbootapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.firstspringbootapp.entity.ItemEntity;
import com.example.firstspringbootapp.service.ItemService;

@RestController
@RequestMapping("/item/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ItemController {

	private final ItemService iserv;

	public ItemController(ItemService itemService) {
		this.iserv = itemService;
	}

	@GetMapping("/printAuthor")
	public String printAuthor() {
		return "Leah V. Barbaso";
	}

	@GetMapping(value = "/postNewItem", produces = MediaType.TEXT_HTML_VALUE)
	public String newItemForm() {
		return """
				<!doctype html>
				<html lang="en">
				<head>
					<meta charset="utf-8">
					<meta name="viewport" content="width=device-width, initial-scale=1">
					<title>Add item</title>
					<style>
						body { font: 16px system-ui, sans-serif; max-width: 32rem; margin: 3rem auto; padding: 0 1rem; }
						label { display: block; margin: 1rem 0; }
						input { box-sizing: border-box; display: block; margin-top: .35rem; padding: .5rem; width: 100%; }
						button { padding: .6rem 1rem; }
					</style>
				</head>
				<body>
					<h1>Add item</h1>
					<form action="/item/api/postNewItem" method="post">
						<label>Name<input name="name" required></label>
						<label>Unit<input name="unit" required></label>
						<label>Price<input name="price" type="number" min="0" step="any" required></label>
						<button type="submit">Create item</button>
					</form>
				</body>
				</html>
				""";
	}

	//C - Create
	@PostMapping("/postNewItem")
	public ItemEntity postItem(@RequestBody ItemEntity item) {
		return iserv.postItem(item);
	}

	@PostMapping(value = "/postNewItem", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
	public String postItemFromForm(
			@RequestParam String name,
			@RequestParam String unit,
			@RequestParam float price) {
		iserv.postItem(new ItemEntity(name, unit, price));
		return """
				<!doctype html>
				<html lang="en">
				<head><meta charset="utf-8"><title>Item created</title></head>
				<body>
					<h1>Item created</h1>
					<p>The item was saved successfully.</p>
					<a href="/item/api/postNewItem">Add another item</a>
				</body>
				</html>
				""";
	}

	//R - Read
	@GetMapping("/getAllItems")
	public List<ItemEntity> getAllItems() {
		return iserv.getAllItems();
	}

	//U - Update
	@PutMapping("/putItem")
	public ItemEntity putItem(
			@RequestParam(required = false) Integer id,
			@RequestBody ItemEntity newItemDetails) {
		if (id == null) {
			id = newItemDetails.getItemid();
		}
		if (id <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An item id is required to update a record.");
		}
		return iserv.putItem(id, newItemDetails);
	}

	@GetMapping("/putItem")
	public ResponseEntity<?> putItemFromBrowser(
			@RequestParam(required = false) Integer id,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String unit,
			@RequestParam(required = false) Float price) {
		if (id == null || name == null || unit == null || price == null) {
			return ResponseEntity.ok()
					.contentType(MediaType.TEXT_HTML)
					.body("""
							<!doctype html>
							<html lang="en">
							<head>
								<meta charset="utf-8">
								<meta name="viewport" content="width=device-width, initial-scale=1">
								<title>Update item</title>
								<style>
									body { font: 16px system-ui, sans-serif; max-width: 32rem; margin: 3rem auto; padding: 0 1rem; }
									label { display: block; margin: 1rem 0; }
									input { box-sizing: border-box; display: block; margin-top: .35rem; padding: .5rem; width: 100%; }
									button { padding: .6rem 1rem; }
								</style>
							</head>
							<body>
								<h1>Update item</h1>
								<form action="/item/api/putItem" method="get">
									<label>Item ID<input name="id" type="number" min="1" required></label>
									<label>Name<input name="name" required></label>
									<label>Unit<input name="unit" required></label>
									<label>Price<input name="price" type="number" min="0" step="any" required></label>
									<button type="submit">Update item</button>
								</form>
							</body>
							</html>
							""");
		}
		return ResponseEntity.ok(iserv.putItem(id, new ItemEntity(name, unit, price)));
	}

	//D - Delete
	@DeleteMapping("/deleteItem/{id}")
	public String deleteItem(@PathVariable int id) {
		return iserv.deleteItem(id);
	}

	@GetMapping("/deleteItem/{id}")
	public String deleteItemFromBrowser(@PathVariable int id) {
		return iserv.deleteItem(id);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Map<String, String>> handleItemOperationError(ResponseStatusException exception) {
		return ResponseEntity.status(exception.getStatusCode())
				.body(Map.of("error", exception.getReason() == null ? "Request failed." : exception.getReason()));
	}
}
