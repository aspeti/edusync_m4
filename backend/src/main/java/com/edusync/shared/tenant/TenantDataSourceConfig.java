package com.edusync.shared.tenant;

import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Envuelve el {@code DataSource} autoconfigurado por Spring Boot en un
 * {@link TenantAwareDataSource}, sin reemplazar la autoconfiguracion de
 * {@code DataSourceAutoConfiguration} (evita declarar un segundo bean de conexion que
 * pueda desincronizarse con las propiedades {@code spring.datasource.*}).
 */
@Configuration
public class TenantDataSourceConfig {

  @Bean
  static BeanPostProcessor tenantAwareDataSourcePostProcessor() {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource dataSource && !(bean instanceof TenantAwareDataSource)) {
          return new TenantAwareDataSource(dataSource);
        }
        return bean;
      }
    };
  }
}
