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

import java.util.Optional;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice(annotations = RestController.class)
public class CustomerControllerAdvice {

	// <1>
	private final MediaType vndErrorMediaType = MediaType
			.parseMediaType("application/vnd.error");

	// <2>
	@ExceptionHandler(CustomerNotFoundException.class)
	ResponseEntity<VndErrors> notFoundException(CustomerNotFoundException e) {
		return this.error(e, HttpStatus.NOT_FOUND, e.getCustomerId() + "");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<VndErrors> assertionException(IllegalArgumentException ex) {
		return this.error(ex, HttpStatus.NOT_FOUND, ex.getLocalizedMessage());
	}

	// <3>
	private <E extends Exception> ResponseEntity<VndErrors> error(E error,
			HttpStatus httpStatus, String logref) {
		String msg = Optional.of(error.getMessage())
				.orElse(error.getClass().getSimpleName());
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(this.vndErrorMediaType);
		return new ResponseEntity<>(new VndErrors(logref, msg), httpHeaders, httpStatus);
	}

}