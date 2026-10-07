package com.example.firstspringbootapp.controller;

import java.util.List;

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

import com.example.firstspringbootapp.entity.ItemEntity;
import com.example.firstspringbootapp.service.ItemService;

@RestController
@RequestMapping("/item/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ItemController {

	private final ItemService itemService;

	public ItemController(ItemService itemService) {
		this.itemService = itemService;
	}

	@GetMapping("/printAuthor")
	public String printAuthor() {
		return "Leah V. Barbaso";
	}

	@PostMapping("/postNewItem")
	public ItemEntity postItem(@RequestBody ItemEntity item) {
		return itemService.postItem(item);
	}

	@GetMapping("/getAllItems")
	public List<ItemEntity> getAllItems() {
		return itemService.getAllItems();
	}

	@PutMapping("/putItem")
	public ItemEntity putItem(@RequestParam("id") int id, @RequestBody ItemEntity newItemDetails) {
		return itemService.putItem(id, newItemDetails);
	}

	@DeleteMapping("/deleteItem/{id}")
	public String deleteItem(@PathVariable("id") int id) {
		return itemService.deleteItem(id);
	}
}
