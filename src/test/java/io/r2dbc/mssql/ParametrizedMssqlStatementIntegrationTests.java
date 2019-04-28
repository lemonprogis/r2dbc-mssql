/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.r2dbc.mssql;

import io.r2dbc.mssql.util.IntegrationTestSupport;
import io.r2dbc.spi.Result;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Integration tests for {@link ParametrizedMssqlStatement}.
 *
 * @author Mark Paluch
 */
class ParametrizedMssqlStatementIntegrationTests extends IntegrationTestSupport {

    @Test
    void shouldExecuteBatch() {

        connection.createStatement("DROP TABLE r2dbc_example").execute()
            .flatMap(MssqlResult::getRowsUpdated)
            .onErrorResume(e -> Mono.empty())
            .thenMany(connection.createStatement("CREATE TABLE r2dbc_example (" +
                "id int PRIMARY KEY IDENTITY(1,1), " +
                "first_name varchar(255), " +
                "last_name varchar(255))")
                .execute().flatMap(MssqlResult::getRowsUpdated).then())
            .as(StepVerifier::create)
            .verifyComplete();

        Flux.from(connection.createStatement("INSERT INTO r2dbc_example (first_name, last_name) values (@fn, @ln)")
            .bind("fn", "Walter").bind("ln", "White").add()
            .bind("fn", "Hank").bind("@ln", "Schrader").add()
            .bind("fn", "Skyler").bind("@ln", "White")
            .execute())
            .flatMap(Result::getRowsUpdated)
            .as(StepVerifier::create)
            .expectNext(1, 1, 1)
            .verifyComplete();
    }
}
