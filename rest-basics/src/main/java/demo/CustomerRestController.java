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
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/customers")
public class CustomerRestController {

	@Autowired
	private CustomerRepository customerRepository;

	// <1>
	@RequestMapping(method = RequestMethod.OPTIONS)
	ResponseEntity<?> options() {

	//@formatter:off
  return ResponseEntity
   .ok()
   .allow(HttpMethod.GET, HttpMethod.POST,
          HttpMethod.HEAD, HttpMethod.OPTIONS,
          HttpMethod.PUT, HttpMethod.DELETE)
          .build();
   //@formatter:on
	}

	@GetMapping
	ResponseEntity<Collection<Customer>> getCollection() {
		return ResponseEntity.ok(this.customerRepository.findAll());
	}

	// <2>
	@GetMapping(value = "/{id}")
	ResponseEntity<Customer> get(@PathVariable Long id) {
		return this.customerRepository.findById(id).map(ResponseEntity::ok)
				.orElseThrow(() -> new CustomerNotFoundException(id));
	}

	@PostMapping
	ResponseEntity<Customer> post(@RequestBody Customer c) { // <3>

		Customer customer = this.customerRepository
				.save(new Customer(c.getFirstName(), c.getLastName()));

		URI uri = MvcUriComponentsBuilder.fromController(getClass()).path("/{id}")
				.buildAndExpand(customer.getId()).toUri();
		return ResponseEntity.created(uri).body(customer);
	}

	// <4>
	@DeleteMapping(value = "/{id}")
	ResponseEntity<?> delete(@PathVariable Long id) {
		return this.customerRepository.findById(id).map(c -> {
			customerRepository.delete(c);
			return ResponseEntity.noContent().build();
		}).orElseThrow(() -> new CustomerNotFoundException(id));
	}

	// <5>
	@RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
	ResponseEntity<?> head(@PathVariable Long id) {
		return this.customerRepository.findById(id)
				.map(exists -> ResponseEntity.noContent().build())
				.orElseThrow(() -> new CustomerNotFoundException(id));
	}

	// <6>
	@PutMapping(value = "/{id}")
	ResponseEntity<Customer> put(@PathVariable Long id, @RequestBody Customer c) {
		return this.customerRepository.findById(id).map(existing -> {
			Customer customer = this.customerRepository.save(
					new Customer(existing.getId(), c.getFirstName(), c.getLastName()));
			URI selfLink = URI.create(
					ServletUriComponentsBuilder.fromCurrentRequest().toUriString());
			return ResponseEntity.created(selfLink).body(customer);
		}).orElseThrow(() -> new CustomerNotFoundException(id));

	}

}
