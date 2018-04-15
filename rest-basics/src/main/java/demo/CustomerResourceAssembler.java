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

import java.net.URI;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@Component
class CustomerResourceAssembler
		implements ResourceAssembler<Customer, Resource<Customer>> {

	@Override
	public Resource<Customer> toResource(Customer customer) {

		Resource<Customer> customerResource = new Resource<>(customer);// <1>
		URI photoUri = MvcUriComponentsBuilder.fromMethodCall(MvcUriComponentsBuilder
				.on(CustomerProfilePhotoRestController.class).read(customer.getId()))
				.buildAndExpand().toUri();

		URI selfUri = MvcUriComponentsBuilder
				.fromMethodCall(MvcUriComponentsBuilder
						.on(CustomerHypermediaRestController.class).get(customer.getId()))
				.buildAndExpand().toUri();

		customerResource.add(new Link(selfUri.toString(), "self"));
		customerResource.add(new Link(photoUri.toString(), "profile-photo"));
		return customerResource;
	}

}
