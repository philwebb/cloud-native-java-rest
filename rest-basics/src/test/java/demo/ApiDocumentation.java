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

import javax.servlet.RequestDispatcher;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@formatter:off
// <1>
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ApiDocumentation {
//@formatter:off

 // <1>
 // @Rule public final RestDocumentation restDocumentation =
 //   new RestDocumentation(
 //  "target/generated-snippets");

 @Autowired
 private MockMvc mockMvc;

 @Test
 public void errorExample() throws Exception {
  this.mockMvc
   .perform(
    get("/error")
     .contentType(MediaType.APPLICATION_JSON)
     .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400)
     .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/customers")
     .requestAttr(RequestDispatcher.ERROR_MESSAGE,
      "The customer 'http://localhost:8443/v1/customers/123' does not exist"))
   .andDo(print()).andExpect(status().isBadRequest())
   .andExpect(jsonPath("error", is("Bad Request")))
   .andExpect(jsonPath("timestamp", is(notNullValue())))
   .andExpect(jsonPath("status", is(400)))
   .andExpect(jsonPath("path", is(notNullValue())))
   .andDo(document("error-example")); // <3>
 }

 @Test
 public void indexExample() throws Exception {
  this.mockMvc.perform(get("/v1/customers")).andExpect(status().isOk())
   .andDo(document("index-example"));
 }
}
