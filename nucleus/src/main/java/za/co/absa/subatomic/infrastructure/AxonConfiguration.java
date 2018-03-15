package za.co.absa.subatomic.infrastructure;

import javax.sql.DataSource;

import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.PostgresEventTableFactory;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.spring.jdbc.SpringDataSourceConnectionProvider;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AxonConfiguration {

    @Bean
    public EventStorageEngine eventStorageEngine(DataSource dataSource,
            PlatformTransactionManager transactionManager) {
        JdbcEventStorageEngine eventStorageEngine = new JdbcEventStorageEngine(
                new SpringDataSourceConnectionProvider(dataSource),
                new SpringTransactionManager(transactionManager)) {
        };
        eventStorageEngine.createSchema(PostgresEventTableFactory.INSTANCE);
        return eventStorageEngine;
    }

}
