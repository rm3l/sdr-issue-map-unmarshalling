package org.rm3l.sdr_issue_map_deserialization.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.Type;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

@Configuration
public class MyRepositoryRestConfigurer implements RepositoryRestConfigurer {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    // a body should be returned on creating or updating a new entity
    config.setReturnBodyForPutAndPost(true);

    // Expose IDs for all JPA entities registered in the Persistence Context
    config.exposeIdsFor(
        entityManager.getMetamodel().getManagedTypes().stream()
            .map(Type::getJavaType)
            .toArray(Class[]::new));

    config.useHalAsDefaultJsonMediaType(true);

    config.setDefaultMediaType(MediaTypes.HAL_JSON);
  }

  @Override
  public void configureConversionService(ConfigurableConversionService conversionService) {

  }

  @Override
  public void configureValidatingRepositoryEventListener(
      ValidatingRepositoryEventListener validatingListener) {

  }

  @Override
  public void configureExceptionHandlerExceptionResolver(
      ExceptionHandlerExceptionResolver exceptionResolver) {

  }

  @Override
  public void configureHttpMessageConverters(List<HttpMessageConverter<?>> messageConverters) {

  }

  @Override
  public void configureJacksonObjectMapper(ObjectMapper objectMapper) {

  }


}
