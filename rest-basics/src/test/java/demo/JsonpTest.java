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

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static demo.TestUtils.lambaMatcher;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CustomerRestController.class)
public class JsonpTest {

	@MockBean
	private CustomerRepository customerRepository;

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testJsonpCallbacks() throws Throwable {

		given(this.customerRepository.findAll()).willReturn(Arrays
				.asList(new Customer(1L, "A1", "B1"), new Customer(2L, "A2", "B2")));

		String callbackName = "callMeMaybe";
		this.mockMvc.perform(get("/v1/customers?callback=" + callbackName))
				.andExpect(status().isOk())
				.andExpect(content().string(lambaMatcher(
						"the result should contain the JavaScript"
								+ " invocation syntax if it's a JSONP request",
						(String result) -> result
								.startsWith("/**/" + callbackName + "("))));
	}

}
