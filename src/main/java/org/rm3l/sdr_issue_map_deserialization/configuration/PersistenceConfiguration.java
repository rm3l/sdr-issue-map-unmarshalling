package org.rm3l.sdr_issue_map_deserialization.configuration;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;
import org.datanucleus.api.jpa.JPAEntityTransaction;
import org.datanucleus.api.jpa.PersistenceProviderImpl;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.exceptions.NucleusOptimisticException;
import org.datanucleus.store.query.NoQueryResultsException;
import org.datanucleus.store.query.QueryNotUniqueException;
import org.rm3l.sdr_issue_map_deserialization.SdrIssueMapDeserializationApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.support.MergingPersistenceUnitManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.lookup.SingleDataSourceLookup;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.jpa.DefaultJpaDialect;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "org.rm3l.sdr_issue_map_deserialization")
@EnableTransactionManagement
public class PersistenceConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(PersistenceConfiguration.class);

  private String persistenceUnitName;

  public PersistenceConfiguration(
      @Value("${datasource.persistence-unit}")
          String persistenceUnitName) {
    this.persistenceUnitName = persistenceUnitName;
  }

  @Bean
  @ConditionalOnMissingBean(name = "jdbcTemplate")
  public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
       final DataSource dataSource,
       final PersistenceUnitManager persistenceUnitManager) {
    final LocalContainerEntityManagerFactoryBean factoryBean =
        new LocalContainerEntityManagerFactoryBean();
    factoryBean.setPersistenceUnitManager(persistenceUnitManager);
    final DataNucleusJpaDialect jpaDialect = new DataNucleusJpaDialect();
    factoryBean.setJpaDialect(jpaDialect);
    factoryBean.setJpaVendorAdapter(new DataNucleusJpaVendorAdapter(jpaDialect));
    factoryBean.setDataSource(dataSource);
    return factoryBean;
  }

  @Bean
  public PersistenceUnitManager persistenceUnitManager( final DataSource dataSource) {
    final MergingPersistenceUnitManager persistenceUnitManager =
        new MergingPersistenceUnitManager();
    persistenceUnitManager.setDefaultPersistenceUnitName(persistenceUnitName);
    persistenceUnitManager.setPackagesToScan(SdrIssueMapDeserializationApplication.class.getPackage().getName());
    persistenceUnitManager.setDataSourceLookup(new SingleDataSourceLookup(dataSource));
    persistenceUnitManager.setDefaultDataSource(dataSource);
    return persistenceUnitManager;
  }

  @Bean
  public PlatformTransactionManager transactionManager(
       final LocalContainerEntityManagerFactoryBean entityManagerFactory) {
    final JpaTransactionManager txManager = new JpaTransactionManager();
    txManager.setEntityManagerFactory(entityManagerFactory.getObject());
    return txManager;
  }

  private static class DataNucleusJpaVendorAdapter extends AbstractJpaVendorAdapter {

    private final PersistenceProvider persistenceProvider;
    private final JpaDialect jpaDialect;

    @SuppressWarnings("unused")
    DataNucleusJpaVendorAdapter() {
      this(new DataNucleusJpaDialect());
    }

    DataNucleusJpaVendorAdapter(DataNucleusJpaDialect dataNucleusJpaDialect) {
      this.jpaDialect = dataNucleusJpaDialect;
      this.persistenceProvider = new PersistenceProviderImpl();
    }

    @Override
    public PersistenceProvider getPersistenceProvider() {
      return this.persistenceProvider;
    }

    @Override
    public JpaDialect getJpaDialect() {
      return this.jpaDialect;
    }
  }

  /** SPI class that represents JPA Dialect implementation for DataNucleus (DN) */
  private static class DataNucleusJpaDialect extends DefaultJpaDialect {

    private static class DataNucleusJpaTransactionData implements SavepointManager {

       private final JPAEntityTransaction datanucleusTransaction;

       private final AtomicLong savepointsCounter = new AtomicLong(0L);

      private DataNucleusJpaTransactionData( JPAEntityTransaction datanucleusTransaction) {
        this.datanucleusTransaction = datanucleusTransaction;
      }

      @Override
      public Object createSavepoint() throws TransactionException {
        final String savepointName =
            (ConnectionHolder.SAVEPOINT_NAME_PREFIX + this.savepointsCounter.incrementAndGet());
        this.datanucleusTransaction.setSavepoint(savepointName);
        return savepointName;
      }

      @Override
      public void rollbackToSavepoint(Object savepoint) throws TransactionException {
        this.datanucleusTransaction.rollbackToSavepoint((String) savepoint);
      }

      @Override
      public void releaseSavepoint(Object savepoint) throws TransactionException {
        try {
          this.datanucleusTransaction.releaseSavepoint((String) savepoint);
        } catch (Throwable ex) {
          if (logger.isDebugEnabled()) {
            logger.debug(ex.getMessage(), ex);
          }
        }
      }
    }

    @Override
    public Object beginTransaction(EntityManager entityManager, TransactionDefinition definition)
        throws PersistenceException, TransactionException {
      // From super#beginTransaction()
      if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
        throw new InvalidIsolationLevelException(
            getClass().getSimpleName()
                + " does not support custom isolation levels due to limitations in standard JPA. "
                + "Specific arrangements may be implemented in custom JpaDialect variants.");
      }
      final EntityTransaction transaction = entityManager.getTransaction();
      transaction.begin();

      // Return a custom implementation for supporting Savepoints,
      // thus allowing us to benefit from Spring NESTED Propagation levels
      if (transaction instanceof JPAEntityTransaction) {
        return new DataNucleusJpaTransactionData((JPAEntityTransaction) transaction);
      }
      return null;
    }

    /**
     * Translate the given runtime exception thrown by DN to a corresponding exception from Spring's
     * generic {@link DataAccessException} hierarchy, if possible.
     *
     * @param ex the runtime exception to translate
     * @return the corresponding DataAccessException (or {@code null} if the exception could not be
     *     translated, as in this case it may result from user code rather than from an actual
     *     persistence problem)
     */
    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
      if (ex instanceof NucleusException) {
        return translateDataNucleusException((NucleusException) ex);
      }
      if (ex instanceof PersistenceException) {
        if (ex.getCause() instanceof NucleusException) {
          // DN exceptions may be encapsulated into standard {@link PersistenceException}
          // through the use of {@link org.datanucleus.api.jpa.NucleusJPAHelper}
          return translateDataNucleusException((NucleusException) ex.getCause());
        }
        if (ex.getCause() instanceof SQLException) {
          // DN may encapsulate the datastore exception right here
          return translateDataNucleusException(
              new NucleusDataStoreException(ex.getCause().getMessage(), ex));
        }
      }
      return super.translateExceptionIfPossible(ex);
    }

    /**
     * Convert the given {@link NucleusException} to an appropriate exception from the {@link
     * DataAccessException} hierarchy.
     *
     * @param ex NucleusException that occurred
     * @return the corresponding DataAccessException instance
     */
    private DataAccessException translateDataNucleusException(NucleusException ex) {
      // TODO Translate more exceptions as needed

      if (ex instanceof org.datanucleus.store.query.QueryTimeoutException) {
        return new QueryTimeoutException(ex.getMessage(), ex);
      }

      if (ex instanceof NucleusDataStoreException) {
        final String message = ex.getMessage();
        if (message != null
            && (message.toLowerCase().contains("duplicate key")
            || message.toLowerCase().contains("unique constraint or index violation"))) {
          return new DuplicateKeyException(ex.getMessage(), ex);
        }
        return new DataAccessResourceFailureException(ex.getMessage(), ex);
      }

      if (ex instanceof NoQueryResultsException) {
        return new EmptyResultDataAccessException(ex.getMessage(), -1, ex);
      }

      if (ex instanceof QueryNotUniqueException) {
        return new IncorrectResultSizeDataAccessException(ex.getMessage(), 1, ex);
      }

      if (ex instanceof NucleusObjectNotFoundException) {
        final NucleusObjectNotFoundException nucleusObjectNotFoundException =
            (NucleusObjectNotFoundException) ex;
        final Object failedObject = nucleusObjectNotFoundException.getFailedObject();
        if (failedObject != null) {
          return new ObjectRetrievalFailureException(
              failedObject.getClass(), failedObject, ex.getMessage(), ex);
        }
        return new ObjectRetrievalFailureException(ex.getMessage(), ex);
      }
      if (ex instanceof NucleusOptimisticException) {
        return new JpaOptimisticLockingFailureException(
            new OptimisticLockException(ex.getMessage(), ex));
      }

      // Fallback
      return new JpaSystemException(ex);
    }
  }

}
