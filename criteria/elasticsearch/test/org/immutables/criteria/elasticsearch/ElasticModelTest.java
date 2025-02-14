/*
 * Copyright 2019 Immutables Authors and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.immutables.criteria.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Arrays;

import static org.immutables.check.Checkers.check;

/**
 * Start embedded ES instance. Insert document(s) then find it.
 */
@ExtendWith(ElasticExtension.class)
public class ElasticModelTest {

  private static final ObjectMapper MAPPER = ElasticPersonTest.MAPPER;

  private static final String INDEX_NAME = "mymodel";

  private final RestClient restClient;
  private ElasticModelRepository repository;

  ElasticModelTest(RestClient restClient) throws IOException {
    this.restClient = restClient;
    setupElastic();
    ElasticsearchBackend backend = new ElasticsearchBackend(ElasticsearchSetup.builder(restClient).objectMapper(MAPPER).resolver(ignore -> INDEX_NAME).build());
    this.repository = new ElasticModelRepository(backend);
  }

  private void setupElastic()  {
    new IndexOps(restClient, MAPPER, INDEX_NAME).create(Mappings.of(ElasticModel.class)).blockingGet();

    final ElasticsearchOps ops = new ElasticsearchOps(restClient, INDEX_NAME, MAPPER, 1024);

    ObjectNode doc1 = MAPPER.createObjectNode()
                .put("string", "foo")
                .put("optionalString", "optFoo")
                .put("bool", true)
                .put("intNumber", 42);

    ObjectNode doc2 = MAPPER.createObjectNode()
                .put("string", "bar")
                .put("optionalString", "optBar")
                .put("bool", false)
                .put("intNumber", 44);

    ops.insertBulk(Arrays.asList(doc1, doc2)).blockingGet();
  }

  @Test
  void criteria() {
    ElasticModelCriteria crit = ElasticModelCriteria.elasticModel;

    assertCount(crit, 2);
    assertCount(crit.intNumber.is(1), 0);
    assertCount(crit.string.is("foo"), 1);
    assertCount(crit.string.in("foo", "bar"), 2);
    assertCount(crit.string.notIn("foo", "bar"), 0);
    assertCount(crit.string.in("foo", "foo2"), 1);
    assertCount(crit.string.in("not", "not"), 0);
    assertCount(crit.string.is("bar"), 1);
    assertCount(crit.string.is("hello"), 0);
    assertCount(crit.optionalString.is("optFoo"), 1);
    assertCount(crit.optionalString.is("missing"), 0);
    assertCount(crit.intNumber.atMost(42).string.is("foo"), 1);
    assertCount(crit.intNumber.atMost(11), 0);

  }

  private void assertCount(ElasticModelCriteria crit, int count) {
    check(repository.find(crit).fetch()).hasSize(count);
  }

}
