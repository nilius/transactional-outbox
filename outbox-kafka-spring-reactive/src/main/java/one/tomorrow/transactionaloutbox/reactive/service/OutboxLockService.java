/**
 * Copyright 2022 Tomorrow GmbH @ https://tomorrow.one
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package one.tomorrow.transactionaloutbox.reactive.service;

import lombok.RequiredArgsConstructor;
import one.tomorrow.transactionaloutbox.reactive.repository.OutboxLockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OutboxLockService {

	private static final Logger logger = LoggerFactory.getLogger(OutboxLockService.class);

	private final OutboxLockRepository repository;
	private final TransactionalOperator rxtx;

	public Mono<Boolean> acquireOrRefreshLock(String ownerId, Duration lockTimeout, boolean refreshLock) {
		return repository.acquireOrRefreshLock(ownerId, lockTimeout, refreshLock);
	}

	public Mono<Void> releaseLock(String ownerId) {
		return repository.releaseLock(ownerId);
	}

	@SuppressWarnings("java:S5411")
	public Mono<Boolean> runWithLock(String ownerId, Mono<Void> action) {
		return repository.preventLockStealing(ownerId).flatMap(outboxLockIsPreventedFromLockStealing ->
				outboxLockIsPreventedFromLockStealing
						? action.thenReturn(true)
						: Mono.just(false)
		).as(rxtx::transactional);
	}

}
