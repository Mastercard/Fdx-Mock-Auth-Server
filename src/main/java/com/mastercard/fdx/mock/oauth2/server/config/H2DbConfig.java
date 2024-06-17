package com.mastercard.fdx.mock.oauth2.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@Slf4j
@EnableTransactionManagement
public class H2DbConfig {	
	
	@Bean
	public DataSource dataSource(ApplicationProperties appProps) {
		log.info("creating h2 datasource");
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(appProps.getDriverClassName());
		dataSource.setUrl(appProps.getJdbcUrl());
		dataSource.setUsername(appProps.getUsernamePath());
		dataSource.setPassword(appProps.getPasswordPath());
		
		// schema initialization
		executeScript(dataSource,"org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql");
		executeScript(dataSource,"org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql");
		executeScript(dataSource,"org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql");
		executeScript(dataSource,"scripts/sql/add-dh-adr-client-registrar.sql");
		executeScript(dataSource,"scripts/sql/push-authorization-request-schema.sql");
		executeScript(dataSource,"scripts/sql/customer-consent.sql");
		return dataSource;
	}

	@Bean(name = "jdbcTemplateAurora")
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	private void executeScript(DriverManagerDataSource dataSource, String scriptPath) {
		DatabasePopulatorUtils.execute(new ResourceDatabasePopulator(new ClassPathResource(scriptPath)), dataSource);
	}
}
