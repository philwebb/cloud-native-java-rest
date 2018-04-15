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
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//@formatter:off
import org.springframework.web.servlet.mvc.method
        .annotation.MvcUriComponentsBuilder;

import static org.springframework.web.servlet.support
        .ServletUriComponentsBuilder.fromCurrentRequest;
//@formatter:on

@RestController
@RequestMapping(value = "/v1/protos/customers")
public class CustomerProtobufRestController {

	private final CustomerRepository customerRepository;

	@Autowired
	public CustomerProtobufRestController(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@GetMapping(value = "/{id}")
	ResponseEntity<CustomerProtos.Customer> get(@PathVariable Long id) {
		return this.customerRepository.findById(id).map(this::fromEntityToProtobuf)
				.map(ResponseEntity::ok)
				.orElseThrow(() -> new CustomerNotFoundException(id));
	}

	@GetMapping
	ResponseEntity<CustomerProtos.Customers> getCollection() {
		List<Customer> all = this.customerRepository.findAll();
		CustomerProtos.Customers customers = this.fromCollectionToProtobuf(all);
		return ResponseEntity.ok(customers);
	}

	@PostMapping
	ResponseEntity<CustomerProtos.Customer> post(@RequestBody CustomerProtos.Customer c) {

		Customer customer = this.customerRepository
				.save(new Customer(c.getFirstName(), c.getLastName()));

		URI uri = MvcUriComponentsBuilder.fromController(getClass()).path("/{id}")
				.buildAndExpand(customer.getId()).toUri();
		return ResponseEntity.created(uri).body(this.fromEntityToProtobuf(customer));
	}

	@PutMapping("/{id}")
	ResponseEntity<CustomerProtos.Customer> put(@PathVariable Long id,
			@RequestBody CustomerProtos.Customer c) {

		return this.customerRepository.findById(id).map(existing -> {

			Customer customer = this.customerRepository.save(
					new Customer(existing.getId(), c.getFirstName(), c.getLastName()));

			URI selfLink = URI.create(fromCurrentRequest().toUriString());

			return ResponseEntity.created(selfLink).body(fromEntityToProtobuf(customer));

		}).orElseThrow(() -> new CustomerNotFoundException(id));
	}

	private CustomerProtos.Customers fromCollectionToProtobuf(Collection<Customer> c) {
		return CustomerProtos.Customers.newBuilder().addAllCustomer(
				c.stream().map(this::fromEntityToProtobuf).collect(Collectors.toList()))
				.build();
	}

	private CustomerProtos.Customer fromEntityToProtobuf(Customer c) {
		return fromEntityToProtobuf(c.getId(), c.getFirstName(), c.getLastName());
	}

	private CustomerProtos.Customer fromEntityToProtobuf(Long id, String f, String l) {
		CustomerProtos.Customer.Builder builder = CustomerProtos.Customer.newBuilder();
		if (id != null && id > 0) {
			builder.setId(id);
		}
		return builder.setFirstName(f).setLastName(l).build();
	}

}
