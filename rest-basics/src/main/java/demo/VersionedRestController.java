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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@formatter:off
import static org.springframework.http
        .MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api")
public class VersionedRestController {

 public static final String V1_MEDIA_TYPE_VALUE
         = "application/vnd.bootiful.demo-v1+json";

 public static final String V2_MEDIA_TYPE_VALUE
         = "application/vnd.bootiful.demo-v2+json";

	private enum ApiVersion {

		v1, v2

	}

	public static class Greeting {

		private String how;

		private String version;

		public Greeting(String how, ApiVersion version) {
			this.how = how;
			this.version = version.toString();
		}

		public String getHow() {
			return how;
		}

		public String getVersion() {
			return version;
		}

	}

	// <1>
	@GetMapping(value = "/{version}/hi", produces = APPLICATION_JSON_VALUE)
	Greeting greetWithPathVariable(@PathVariable ApiVersion version) {
		return greet(version, "path-variable");
	}

	// <2>
	@GetMapping(value = "/hi", produces = APPLICATION_JSON_VALUE)
	Greeting greetWithHeader(@RequestHeader("X-API-Version") ApiVersion version) {
		return this.greet(version, "header");
	}

	// <3>
	@GetMapping(value = "/hi", produces = V1_MEDIA_TYPE_VALUE)
	Greeting greetWithContentNegotiationV1() {
		return this.greet(ApiVersion.v1, "content-negotiation");
	}

	// <4>
	@GetMapping(value = "/hi", produces = V2_MEDIA_TYPE_VALUE)
	Greeting greetWithContentNegotiationV2() {
		return this.greet(ApiVersion.v2, "content-negotiation");
	}

	private Greeting greet(ApiVersion version, String how) {
		return new Greeting(how, version);
	}

}
