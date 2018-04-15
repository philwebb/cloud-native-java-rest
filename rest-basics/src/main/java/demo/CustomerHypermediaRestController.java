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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
//@formatter:off
import org.springframework.web.servlet.mvc.method
        .annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support
        .ServletUriComponentsBuilder;
//@formatter:on

// <1>
@RestController
@RequestMapping(value = "/v2", produces = "application/hal+json")
public class CustomerHypermediaRestController {

	private final CustomerResourceAssembler customerResourceAssembler; // <2>

	private final CustomerRepository customerRepository;

	@Autowired
	CustomerHypermediaRestController(CustomerResourceAssembler cra,
			CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
		this.customerResourceAssembler = cra;
	}

	// <3>
	@GetMapping
	ResponseEntity<Resources<Object>> root() {
		Resources<Object> objects = new Resources<>(Collections.emptyList());
		URI uri = MvcUriComponentsBuilder
				.fromMethodCall(MvcUriComponentsBuilder.on(getClass()).getCollection())
				.build().toUri();
		Link link = new Link(uri.toString(), "customers");
		objects.add(link);
		return ResponseEntity.ok(objects);
	}

	// <4>
	@GetMapping("/customers")
	ResponseEntity<Resources<Resource<Customer>>> getCollection() {
		List<Resource<Customer>> collect = this.customerRepository.findAll().stream()
				.map(customerResourceAssembler::toResource)
				.collect(Collectors.<Resource<Customer>>toList());
		Resources<Resource<Customer>> resources = new Resources<>(collect);
		URI self = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
		resources.add(new Link(self.toString(), "self"));
		return ResponseEntity.ok(resources);
	}

	@RequestMapping(value = "/customers", method = RequestMethod.OPTIONS)
	ResponseEntity<?> options() {
		return ResponseEntity.ok().allow(HttpMethod.GET, HttpMethod.POST, HttpMethod.HEAD,
				HttpMethod.OPTIONS, HttpMethod.PUT, HttpMethod.DELETE).build();
	}

	@GetMapping(value = "/customers/{id}")
	ResponseEntity<Resource<Customer>> get(@PathVariable Long id) {
		return this.customerRepository.findById(id)
				.map(c -> ResponseEntity.ok(this.customerResourceAssembler.toResource(c)))
				.orElseThrow(() -> new CustomerNotFoundException(id));
	}

	@PostMapping(value = "/customers")
	ResponseEntity<Resource<Customer>> post(@RequestBody Customer c) {
		Customer customer = this.customerRepository
				.save(new Customer(c.getFirstName(), c.getLastName()));
		URI uri = MvcUriComponentsBuilder.fromController(getClass())
				.path("/customers/{id}").buildAndExpand(customer.getId()).toUri();
		return ResponseEntity.created(uri)
				.body(this.customerResourceAssembler.toResource(customer));
	}

	@DeleteMapping(value = "/customers/{id}")
	ResponseEntity<?> delete(@PathVariable Long id) {
		return this.customerRepository.findById(id).map(c -> {
			customerRepository.delete(c);
			return ResponseEntity.noContent().build();
		}).orElseThrow(() -> new CustomerNotFoundException(id));
	}

	@RequestMapping(value = "/customers/{id}", method = RequestMethod.HEAD)
	ResponseEntity<?> head(@PathVariable Long id) {
		return this.customerRepository.findById(id)
				.map(exists -> ResponseEntity.noContent().build())
				.orElseThrow(() -> new CustomerNotFoundException(id));
	}

	@PutMapping("/customers/{id}")
	ResponseEntity<Resource<Customer>> put(@PathVariable Long id,
			@RequestBody Customer c) {
		Customer customer = this.customerRepository
				.save(new Customer(id, c.getFirstName(), c.getLastName()));
		Resource<Customer> customerResource = this.customerResourceAssembler
				.toResource(customer);
		URI selfLink = URI
				.create(ServletUriComponentsBuilder.fromCurrentRequest().toUriString());
		return ResponseEntity.created(selfLink).body(customerResource);
	}

}
