package com.spotonresponse.adapter.config;

import com.spotonresponse.adapter.repo.DynamoDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@PropertySource("classpath:dynamodb.properties")
@Configuration
@ComponentScan("com.spotonresponse.adapter")
public class JsonAdapterConfiguration {

    private final static String packageName = "com.spotonresponse.adapter";

    private final static String S_AWS_ACCESS_KEY = "aws.access.key.id";
    private final static String S_AWS_SECRET_KEY = "aws.secret.access.key";

    @Autowired
    Environment environment;

    @Value("${amazon.endpoint}")
    private String amazon_endpoint;
    @Value("${amazon.region}")
    private String amazon_region;
    @Value("${nosql.table.name}")
    private String dynamoDBTableName;

    private int pollerCount = 10;

    /*
    @Bean
    public ConfigurationDirectoryWatcher watcher() {
        ConfigurationDirectoryWatcher watcher = new ConfigurationDirectoryWatcher();
        watcher.setScheduler(threadPoolTaskScheduler());
        return watcher;
    }

    @Bean
    public JSONPollerTask jsonPollerTask() {

        JSONPollerTask jsonPollerTask = new JSONPollerTask();
        jsonPollerTask.setRepo(dynamoDBRepository());
        return jsonPollerTask;
    }
    */

    @Bean
    public DynamoDBRepository dynamoDBRepository() {

        DynamoDBRepository repo = new DynamoDBRepository();

        repo.init(environment.getProperty(S_AWS_ACCESS_KEY), environment.getProperty(S_AWS_SECRET_KEY), amazon_endpoint,
                  amazon_region, dynamoDBTableName);
        return repo;
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {

        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(pollerCount);
        return threadPoolTaskScheduler;
    }

    @Bean
    public DataSource dataSource() {

        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {

        HibernateJpaVendorAdapter bean = new HibernateJpaVendorAdapter();
        bean.setDatabase(Database.H2);
        bean.setGenerateDdl(true);
        bean.setShowSql(false);
        return bean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
        JpaVendorAdapter jpaVendorAdapter) {

        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource);
        bean.setJpaVendorAdapter(jpaVendorAdapter);
        bean.setPackagesToScan(packageName);
        return bean;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {

        return new JpaTransactionManager(emf);
    }
}
