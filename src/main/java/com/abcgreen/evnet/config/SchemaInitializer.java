package com.abcgreen.evnet.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/** Creates the schema on first boot, picking the right dialect for the configured database. */
@Component
public class SchemaInitializer {
    private final DataSource ds;

    public SchemaInitializer(DataSource ds) { this.ds = ds; }

    @javax.annotation.PostConstruct
    public void init() throws Exception {
        try (Connection c = ds.getConnection()) {
            String product = System.getenv().getOrDefault("DB_PLATFORM",
                    c.getMetaData().getDatabaseProductName().toLowerCase());
            String script = product.contains("postgres") ? "schema-postgresql.sql" : "schema.sql";
            ScriptUtils.executeSqlScript(c, new ClassPathResource(script));
            System.out.println("=== EV ChargeNet: schema ready (" + product + " -> " + script + ")");
        }
    }
}
