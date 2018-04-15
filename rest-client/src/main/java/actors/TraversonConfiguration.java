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

package actors;

import java.net.URI;

import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TraversonConfiguration {

	private int port;

	private URI baseUri;

	//@formatter:off
 @EventListener
 public void embeddedPortAvailable(
    EmbeddedServletContainerInitializedEvent e) {
  this.port = e.getEmbeddedServletContainer().getPort();
  this.baseUri = URI.create("http://localhost:" + this.port + '/');
 }
 //@formatter:on

	// <1>
	@Bean
	@Lazy
	Traverson traverson(RestTemplate restTemplate) {
		Traverson traverson = new Traverson(this.baseUri, MediaTypes.HAL_JSON);
		traverson.setRestOperations(restTemplate);
		return traverson;
	}

}
