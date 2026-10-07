package com.example.firstspringbootapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.firstspringbootapp.entity.ItemEntity;
import com.example.firstspringbootapp.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceTests {

	@Mock
	private ItemRepository itemRepository;

	private ItemService itemService;

	@BeforeEach
	void setUp() {
		itemService = new ItemService(itemRepository);
	}

	@Test
	void postItemSavesAndReturnsItem() {
		ItemEntity item = new ItemEntity("Coffee", "bag", 12.5f);
		when(itemRepository.save(item)).thenReturn(item);

		assertSame(item, itemService.postItem(item));

		verify(itemRepository).save(item);
	}

	@Test
	void getAllItemsReturnsRepositoryResults() {
		List<ItemEntity> items = List.of(new ItemEntity("Coffee", "bag", 12.5f));
		when(itemRepository.findAll()).thenReturn(items);

		assertSame(items, itemService.getAllItems());
	}

	@Test
	void putItemUpdatesExistingItem() {
		ItemEntity existing = new ItemEntity("Old name", "box", 2.0f);
		existing.setItemid(4);
		ItemEntity changes = new ItemEntity("Coffee", "bag", 12.5f);
		when(itemRepository.findById(4)).thenReturn(Optional.of(existing));
		when(itemRepository.save(existing)).thenReturn(existing);

		ItemEntity result = itemService.putItem(4, changes);

		assertSame(existing, result);
		assertEquals(4, result.getItemid());
		assertEquals("Coffee", result.getName());
		assertEquals("bag", result.getUnit());
		assertEquals(12.5f, result.getPrice());
		verify(itemRepository).save(existing);
	}

	@Test
	void putItemReturnsNotFoundWhenItemDoesNotExist() {
		when(itemRepository.findById(4)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> itemService.putItem(4, new ItemEntity("Coffee", "bag", 12.5f)));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		verify(itemRepository).findById(4);
		verifyNoMoreInteractions(itemRepository);
	}

	@Test
	void deleteItemDeletesExistingItem() {
		when(itemRepository.existsById(4)).thenReturn(true);

		assertEquals("Item 4 is successfully deleted!", itemService.deleteItem(4));

		verify(itemRepository).deleteById(4);
	}

	@Test
	void deleteItemReturnsNotFoundWhenItemDoesNotExist() {
		when(itemRepository.existsById(4)).thenReturn(false);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> itemService.deleteItem(4));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		verify(itemRepository).existsById(4);
		verifyNoMoreInteractions(itemRepository);
	}
}
