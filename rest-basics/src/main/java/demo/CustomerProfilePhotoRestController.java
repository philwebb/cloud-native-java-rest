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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

//@formatter:off
import static org.springframework.web.servlet
        .support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping(value = "/customers/{id}/photo")
public class CustomerProfilePhotoRestController {

	private File root;

	private final CustomerRepository customerRepository;

	private final Log log = LogFactory.getLog(getClass());

	@Autowired
	CustomerProfilePhotoRestController(
			@Value("${upload.dir:${user.home}/images}") String uploadDir,
			CustomerRepository customerRepository) {
		this.root = new File(uploadDir);
		this.customerRepository = customerRepository;
		Assert.isTrue(this.root.exists() || this.root.mkdirs(),
				String.format("The path '%s' must exist.", this.root.getAbsolutePath()));
	}

	// <1>
	@GetMapping
	ResponseEntity<Resource> read(@PathVariable Long id) {
		return this.customerRepository.findById(id).map(customer -> {
			File file = fileFor(customer);

			Assert.isTrue(file.exists(),
					String.format("file-not-found %s", file.getAbsolutePath()));

			Resource fileSystemResource = new FileSystemResource(file);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG)
					.body(fileSystemResource);
		}).orElseThrow(() -> new CustomerNotFoundException(id));
	}

	// <2>
	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT })
	Callable<ResponseEntity<?>> write(@PathVariable Long id,
			@RequestParam MultipartFile file) // <3>
			throws Exception {
		log.info(String.format("upload-start /customers/%s/photo (%s bytes)", id,
				file.getSize()));
		return () -> this.customerRepository.findById(id).map(customer -> {
			File fileForCustomer = fileFor(customer);
			try (InputStream in = file.getInputStream();
					OutputStream out = new FileOutputStream(fileForCustomer)) {
				FileCopyUtils.copy(in, out);
			}
			catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			URI location = fromCurrentRequest().buildAndExpand(id).toUri(); // <4>
			log.info(String.format("upload-finish /customers/%s/photo (%s)", id,
					location));
			return ResponseEntity.created(location).build();
		}).orElseThrow(() -> new CustomerNotFoundException(id));
	}

	private File fileFor(Customer person) {
		return new File(this.root, Long.toString(person.getId()));
	}

}
