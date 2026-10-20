package com.example.firstspringbootapp.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.firstspringbootapp.service.ItemService.LeadingSlashRedirectFilter;
import com.example.firstspringbootapp.repository.ItemRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:demo-test;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class DemoApplicationTests {

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private ItemRepository itemRepository;

	private MockMvc mockMvc;

	@BeforeEach
	void setUpMockMvc() {
		itemRepository.deleteAll();
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
				.addFilters(new LeadingSlashRedirectFilter())
				.build();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void printAuthorEndpointIsMapped() throws Exception {
		mockMvc.perform(get("/item/api/printAuthor"))
				.andExpect(status().isOk())
				.andExpect(content().string("Leah V. Barbaso"));
	}

	@Test
	void printAuthorAcceptsDoubleLeadingSlash() throws Exception {
		mockMvc.perform(get(URI.create("http://localhost:8080//item/api/printAuthor")))
				.andExpect(status().isOk())
				.andExpect(content().string("Leah V. Barbaso"));
	}

	@Test
	void crudEndpointsAcceptDoubleLeadingSlash() throws Exception {
		String itemUrl = "http://localhost:8080//item/api";

		String createdItem = mockMvc.perform(post(URI.create(itemUrl + "/postNewItem"))
						.contentType("application/json")
						.content("""
								{"name":"Coffee","unit":"bag","price":12.5}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Coffee"))
				.andReturn().getResponse().getContentAsString();
		Matcher itemId = Pattern.compile("\"itemid\"\\s*:\\s*(\\d+)").matcher(createdItem);
		if (!itemId.find()) {
			throw new AssertionError("Create response did not contain an item id: " + createdItem);
		}
		String id = itemId.group(1);

		mockMvc.perform(get(URI.create(itemUrl + "/getAllItems")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].itemid").value(Integer.parseInt(id)))
				.andExpect(jsonPath("$[0].name").value("Coffee"));

		mockMvc.perform(put(URI.create(itemUrl + "/putItem?id=" + id))
						.contentType("application/json")
						.content("""
								{"name":"Tea","unit":"box","price":8.75}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Tea"))
				.andExpect(jsonPath("$.unit").value("box"))
				.andExpect(jsonPath("$.price").value(8.75));

		mockMvc.perform(delete(URI.create(itemUrl + "/deleteItem/" + id)))
				.andExpect(status().isOk())
				.andExpect(content().string("Item " + id + " is successfully deleted!"));

		mockMvc.perform(get(URI.create(itemUrl + "/getAllItems")))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	void updateAcceptsItemIdInBodyWithoutQueryParameter() throws Exception {
		String createdItem = mockMvc.perform(post("/item/api/postNewItem")
						.contentType("application/json")
						.content("""
								{"name":"Coffee","unit":"bag","price":12.5}
								"""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		Matcher itemId = Pattern.compile("\"itemid\"\\s*:\\s*(\\d+)").matcher(createdItem);
		if (!itemId.find()) {
			throw new AssertionError("Create response did not contain an item id: " + createdItem);
		}

		mockMvc.perform(put(URI.create("http://localhost:8080//item/api/putItem"))
						.contentType("application/json")
						.content("""
								{"itemid":%s,"name":"Tea","unit":"box","price":8.75}
								""".formatted(itemId.group(1))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.itemid").value(Integer.parseInt(itemId.group(1))))
				.andExpect(jsonPath("$.name").value("Tea"));
	}

	@Test
	void itemOperationErrorsReturnJsonInsteadOfHtml() throws Exception {
		mockMvc.perform(put("/item/api/putItem")
						.contentType("application/json")
						.content("""
								{"name":"Tea","unit":"box","price":8.75}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.error").value("An item id is required to update a record."));

		mockMvc.perform(delete("/item/api/deleteItem/999"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.error").value("Item 999 does not exist!"));
	}

	@Test
	void getRequestsCanUpdateAndDeleteItems() throws Exception {
		String createdItem = mockMvc.perform(post("/item/api/postNewItem")
						.contentType("application/json")
						.content("""
								{"name":"Coffee","unit":"bag","price":12.5}
								"""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		Matcher itemId = Pattern.compile("\"itemid\"\\s*:\\s*(\\d+)").matcher(createdItem);
		if (!itemId.find()) {
			throw new AssertionError("Create response did not contain an item id: " + createdItem);
		}
		String id = itemId.group(1);

		mockMvc.perform(get("/item/api/putItem")
						.param("id", id)
						.param("name", "Tea")
						.param("unit", "box")
						.param("price", "8.75"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.itemid").value(Integer.parseInt(id)))
				.andExpect(jsonPath("$.name").value("Tea"))
				.andExpect(jsonPath("$.unit").value("box"))
				.andExpect(jsonPath("$.price").value(8.75));

		mockMvc.perform(get("/item/api/deleteItem/" + id))
				.andExpect(status().isOk())
				.andExpect(content().string("Item " + id + " is successfully deleted!"));
	}

	@Test
	void openingUpdateUrlShowsFormInsteadOfMissingParameterError() throws Exception {
		mockMvc.perform(get("/item/api/putItem"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("<h1>Update item</h1>")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"id\"")));
	}

	@Test
	void postNewItemUrlShowsFormAndAcceptsFormSubmission() throws Exception {
		mockMvc.perform(get("/item/api/postNewItem"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("<h1>Add item</h1>")));

		mockMvc.perform(post("/item/api/postNewItem")
						.contentType("application/x-www-form-urlencoded")
						.param("name", "Coffee")
						.param("unit", "bag")
						.param("price", "12.5"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Item created")));

		mockMvc.perform(get("/item/api/getAllItems"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("\"name\":\"Coffee\"")));
	}
}
