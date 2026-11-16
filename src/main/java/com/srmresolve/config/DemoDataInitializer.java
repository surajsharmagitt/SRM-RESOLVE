package com.srmresolve.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Initializes demonstration user accounts and sample tickets ONLY when demo mode
 * is explicitly enabled (app.demo-mode=true).
 * In production or cloud deployments (app.demo-mode=false), demonstration accounts
 * with known passwords are NOT seeded into the database.
 */
@Component
public class DemoDataInitializer implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final DataSource dataSource;

    @Value("${app.demo-mode:false}")
    private boolean demoMode;

    public DemoDataInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!demoMode) {
            log.info("Production mode active (app.demo-mode=false): Skipping classroom demo account seeding.");
            return;
        }

        log.info("Demonstration mode active (app.demo-mode=true): Seeding classroom demo accounts and sample data from data-demo.sql...");
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("data-demo.sql"));
            populator.setContinueOnError(true);
            populator.execute(dataSource);
            log.info("Classroom demonstration accounts and sample data initialized successfully.");
        } catch (Exception e) {
            log.warn("Notice: demo data initialization encountered: {}", e.getMessage());
        }
    }
}
