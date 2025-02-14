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
import org.elasticsearch.client.RestClient;
import org.immutables.criteria.backend.Backend;
import org.immutables.criteria.backend.WithSessionCallback;
import org.immutables.criteria.typemodel.BooleanTemplate;
import org.immutables.criteria.typemodel.LocalDateTemplate;
import org.immutables.criteria.typemodel.LongTemplate;
import org.immutables.criteria.typemodel.StringTemplate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Consumer;

@ExtendWith(ElasticExtension.class)
class ElasticIntegrationTests {

  private final Backend backend;
  private final ObjectMapper mapper;

  ElasticIntegrationTests(RestClient restClient) {
    this.mapper = ElasticPersonTest.MAPPER;

    ElasticsearchBackend backend = new ElasticsearchBackend(ElasticsearchSetup.builder(restClient)
            .objectMapper(mapper)
            .build());

    // auto-create ES mapping on session open
    Consumer<Class<?>> onOpen = entity -> {
      String name = IndexResolver.defaultResolver().resolve(entity);
      new IndexOps(restClient, mapper, name).create(Mappings.of(entity)).blockingAwait();
    };
    this.backend = WithSessionCallback.wrap(backend, onOpen);
  }

  @Nested
  class StringTest extends StringTemplate {
    private StringTest() {
      super(backend);
    }
  }

  @Nested
  class BooleanTest extends BooleanTemplate {
    private BooleanTest() {
      super(backend);
    }
  }

  @Nested
  class LocalDateTest extends LocalDateTemplate {
    private LocalDateTest() {
      super(backend);
    }
  }

  @Nested
  class LongTest extends LongTemplate {
    private LongTest() {
      super(backend);
    }
  }

}
