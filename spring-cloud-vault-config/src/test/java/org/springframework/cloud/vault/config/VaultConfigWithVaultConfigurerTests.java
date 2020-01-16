/*
 * Copyright 2017-2020 the original author or authors.
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

package org.springframework.cloud.vault.config;

import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.vault.util.VaultRule;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.vault.core.VaultOperations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test using config infrastructure with token authentication.
 *
 * <p>
 * In case this test should fail because of SSL make sure you run the test within the
 * spring-cloud-vault-config/spring-cloud-vault-config directory as the keystore is
 * referenced with {@code ../work/keystore.jks}.
 *
 * @author Mark Paluch
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VaultConfigWithVaultConfigurerTests.TestApplication.class,
		properties = "VaultConfigWithVaultConfigurerTests.custom.config=true")
public class VaultConfigWithVaultConfigurerTests {

	@Value("${vault.value}")
	String configValue;

	@BeforeClass
	public static void beforeClass() {

		VaultRule vaultRule = new VaultRule();
		vaultRule.before();

		VaultOperations vaultOperations = vaultRule.prepare().getVaultOperations();

		vaultOperations.write("secret/VaultConfigWithVaultConfigurerTests",
				Collections.singletonMap("vault.value", "hello"));

		vaultOperations.write("secret/testVaultApp",
				Collections.singletonMap("vault.value", "world"));
	}

	@Test
	public void contextLoads() {
		assertThat(this.configValue).isEqualTo("hello");
	}

	@SpringBootApplication
	public static class TestApplication {

		public static void main(String[] args) {
			SpringApplication.run(TestApplication.class, args);
		}

	}

	public static class ConfigurerBootstrapApplication {

		@ConditionalOnProperty("VaultConfigWithVaultConfigurerTests.custom.config")
		@Bean
		VaultConfigurer vaultConfigurer() {
			return configurer -> configurer
					.add("secret/VaultConfigWithVaultConfigurerTests");
		}

	}

}
