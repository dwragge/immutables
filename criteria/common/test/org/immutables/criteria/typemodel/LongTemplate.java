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

package org.immutables.criteria.typemodel;

import org.immutables.check.IterableChecker;
import org.immutables.criteria.backend.Backend;
import org.immutables.criteria.personmodel.CriteriaChecker;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

/**
 * Testing various string operations prefix/suffix/length etc.
 */
public abstract class LongTemplate {

  private final LongHolderRepository repository;
  private final LongHolderCriteria criteria = LongHolderCriteria.longHolder;
  private final Supplier<ImmutableLongHolder> generator;

  protected LongTemplate(Backend backend) {
    this.repository = new LongHolderRepository(backend);
    this.generator = TypeHolder.LongHolder.generator();
  }

  @Test
  void empty() {
    final long zero = 0L;
    ids(criteria.value.is(zero)).isEmpty();
    ids(criteria.value.isNot(zero)).isEmpty();
    ids(criteria.value.atLeast(zero)).isEmpty();
    ids(criteria.value.atMost(zero)).isEmpty();
    ids(criteria.value.greaterThan(zero)).isEmpty();
    ids(criteria.value.lessThan(zero)).isEmpty();
    ids(criteria.value.between(zero, zero)).isEmpty();
  }

  @Test
  void equality() {
    repository.insert(generator.get().withId("id0").withValue(0L));
    repository.insert(generator.get().withId("id1").withValue(1L));
    repository.insert(generator.get().withId("id2").withValue(2L));

    ids(criteria.value.is(0L)).hasContentInAnyOrder("id0");
    ids(criteria.value.is(1L)).hasContentInAnyOrder("id1");
    ids(criteria.value.is(-1L)).isEmpty();
    ids(criteria.value.isNot(0L)).hasContentInAnyOrder("id1", "id2");
    ids(criteria.value.isNot(1L)).hasContentInAnyOrder("id0", "id2");
    ids(criteria.value.isNot(3L)).hasContentInAnyOrder("id0", "id1", "id2");
    ids(criteria.value.in(0L, 1L)).hasContentInAnyOrder("id1", "id0");
    ids(criteria.value.notIn(0L, 1L)).hasContentInAnyOrder("id2");
    ids(criteria.value.notIn(0L, 1L, 2L)).isEmpty();
    ids(criteria.value.notIn(-1L, -2L)).hasContentInAnyOrder("id0", "id1", "id2");
  }

  @Test
  void comparison() {
    repository.insert(generator.get().withId("id0").withValue(0L));
    repository.insert(generator.get().withId("id1").withValue(1L));
    repository.insert(generator.get().withId("id2").withValue(2L));

    ids(criteria.value.atMost(0L)).hasContentInAnyOrder("id0");
    ids(criteria.value.atLeast(0L)).hasContentInAnyOrder("id0", "id1", "id2");
    ids(criteria.value.atMost(2L)).hasContentInAnyOrder("id0", "id1", "id2");
    ids(criteria.value.between(0L, 2L)).hasContentInAnyOrder("id0", "id1", "id2");
    ids(criteria.value.between(10L, 20L)).isEmpty();
    ids(criteria.value.atMost(1L)).hasContentInAnyOrder("id0", "id1");
    ids(criteria.value.atLeast(1L)).hasContentInAnyOrder("id1", "id2");
    ids(criteria.value.atLeast(2L)).hasContentInAnyOrder("id2");
    ids(criteria.value.atLeast(33L)).isEmpty();
    ids(criteria.value.greaterThan(2L)).isEmpty();
    ids(criteria.value.lessThan(0L)).isEmpty();
  }

  private IterableChecker<List<String>, String> ids(LongHolderCriteria criteria) {
    return CriteriaChecker.<TypeHolder.LongHolder>of(repository.find(criteria)).toList(TypeHolder.LongHolder::id);
  }
}
