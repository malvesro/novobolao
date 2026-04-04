package com.opendev.bolao.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuração Java para os repositórios Spring Data JPA.
 * Substitui o <jpa:repositories> do XML para evitar bugs de inicialização do EntityPathResolver.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.opendev.bolao.repository",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
public class JpaRepositoriesConfig {
}
