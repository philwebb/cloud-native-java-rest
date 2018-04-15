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

import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Ignore
public class HiddenHttpMethodFilterTest {

	private RestTemplate restTemplate = new RestTemplate();

	@Test
	public void testHiddenHttpMethodInvocation() throws Throwable {
		ConfigurableApplicationContext applicationContext = SpringApplication
				.run(Application.class);
		int port = AnnotationConfigServletWebServerApplicationContext.class
				.cast(applicationContext).getWebServer().getPort();

		String s = "/v1/customers/1";
		String url = String.format("http://localhost:%d/%s", port, s);

		ResponseEntity<Map> forEntity;

		forEntity = restTemplate.getForEntity(url, Map.class);
		Assert.assertTrue(forEntity.getStatusCode().is2xxSuccessful());

		restTemplate.postForEntity(url + "?_method=DELETE", null, java.util.Map.class);

		try {
			forEntity = null;
			forEntity = restTemplate.getForEntity(url, Map.class);
		}
		catch (HttpClientErrorException ex) {
			// works as expected
			return;
		}
		Assert.fail();
	}

}
