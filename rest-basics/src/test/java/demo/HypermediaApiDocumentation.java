/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
public class HypermediaApiDocumentation {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void errorExample() throws Exception {
		this.mockMvc.perform(get("/error")
				.requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400)
				.requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/customers")
				.requestAttr(RequestDispatcher.ERROR_MESSAGE,
						"The customer 'http://localhost:8443/v2/customers/123' does not exist"))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("error", is("Bad Request")))
				.andExpect(jsonPath("timestamp", is(notNullValue())))
				.andExpect(jsonPath("status", is(400)))
				.andExpect(jsonPath("path", is(notNullValue())))
				.andDo(document("error-example", responseFields(
						fieldWithPath("error").description(
								"The HTTP error that occurred, e.g. `Bad Request`"),
						fieldWithPath("message")
								.description("A description of the cause of the error"),
						fieldWithPath("path")
								.description("The path to which the request was made"),
						fieldWithPath("status")
								.description("The HTTP status code, e.g. `400`"),
						fieldWithPath("timestamp").description(
								"The time, in milliseconds, at which the error occurred"))));
	}

	@Test
	public void customersListExample() throws Exception {

		this.mockMvc.perform(get("/v2/customers")).andExpect(status().isOk())
				.andDo(document("customers-list-example", responseFields(
						fieldWithPath("_links.self.href")
								.description("A link to the Customers"),
						fieldWithPath("_embedded.customers").description(
								"An array of <<resources-customer, Customer resources>>"))));
	}

	@Test
	public void customersCreateExample() throws Exception {
		Map<String, String> customer = new HashMap<>();
		customer.put("firstName", "Chris");
		customer.put("lastName", "Richardson");

		this.mockMvc
				.perform(post("/v2/customers").contentType(MediaTypes.HAL_JSON)
						.content(this.objectMapper.writeValueAsString(customer)))
				.andExpect(status().isCreated())
				.andDo(document("customers-create-example",
						links(linkWithRel("self")
								.description("This <<resources-customer,customer>>"),
								linkWithRel("profile-photo").description(
										"The <<resources-profile-photo,profile-photo>>")),
						requestFields(
								fieldWithPath("firstName")
										.description("the first name of the customer"),
								fieldWithPath("lastName")
										.description("the last name of the customer")),
						responseFields(
								fieldWithPath("firstName")
										.description("The first name of the customer"),
								fieldWithPath("lastName")
										.description("The last name of the customer"),
								fieldWithPath("_links").description(
										"<<resources-customer-links,Links>> to other resources"))));
	}

	@Test
	public void customerGetExample() throws Exception {
		Map<String, String> customer = new HashMap<>();
		customer.put("firstName", "Jez");
		customer.put("lastName", "Humble");

		String customerLocation = this.mockMvc
				.perform(post("/v2/customers").contentType(MediaTypes.HAL_JSON)
						.content(this.objectMapper.writeValueAsString(customer)))
				.andExpect(status().isCreated()).andReturn().getResponse()
				.getHeader("Location");

		this.mockMvc.perform(get(customerLocation)).andExpect(status().isOk())
				.andExpect(jsonPath("firstName", is(customer.get("firstName"))))
				.andExpect(jsonPath("lastName", is(customer.get("lastName"))))
				.andExpect(jsonPath("_links.self.href", is(customerLocation)))
				.andDo(document("customer-get-example",
						links(linkWithRel("self").description(
								"This <<resources-customer,customer>>"),
								linkWithRel("profile-photo").description(
										"The <<resources-profile-photo,profile-photo>>")),
						responseFields(
								// fieldWithPath("id").description("The id of this
								// customer"),
								fieldWithPath("firstName")
										.description("The first name of the customer"),
								fieldWithPath("lastName")
										.description("The last name of the customer"),
								fieldWithPath("_links").description(
										"<<resources-customer-links,Links>> to other resources"))));

	}

	@Test
	public void customerUpdateExample() throws Exception {
		Map<String, String> customer = new HashMap<>();
		customer.put("firstName", "Martin");
		customer.put("lastName", "Fowler");

		String customerLocation = this.mockMvc
				.perform(post("/v2/customers").contentType(MediaTypes.HAL_JSON)
						.content(this.objectMapper.writeValueAsString(customer)))
				.andExpect(status().isCreated()).andReturn().getResponse()
				.getHeader("Location");

		this.mockMvc.perform(get(customerLocation)).andExpect(status().isOk())
				.andExpect(jsonPath("firstName", is(customer.get("firstName"))))
				.andExpect(jsonPath("lastName", is(customer.get("lastName"))))
				.andExpect(jsonPath("_links.self.href", is(customerLocation)));

		Map<String, String> customerUpdate = new HashMap<>();
		customerUpdate.put("firstName", "Martin");
		customerUpdate.put("lastName", "Fowler");

		this.mockMvc
				.perform(put(customerLocation).contentType(MediaTypes.HAL_JSON)
						.content(this.objectMapper.writeValueAsString(customerUpdate)))
				.andExpect(status().isCreated())
				.andDo(document("customer-update-example",
						requestFields(fieldWithPath("firstName")
								.description("The first name of the customer").optional(),
								fieldWithPath("lastName")
										.description("The last name of the customer")
										.optional())));
	}

}
