package uk.gov.hmcts.rse;

import org.flywaydb.core.Flyway;
import org.postgresql.jdbc.PgConnection;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class Migrator {
    public static void init(Connection connection) throws SQLException {
        String path;
        if (new File("nfdiv").exists()) {
            path = "filesystem:nfdiv/src/main/resources/db/migration";
        } else {
            path = "filesystem:src/main/resources/db/migration";
        }

        PgConnection c = (PgConnection) connection;
        Flyway.configure().dataSource(c.getURL(), "test", "test")
                .detectEncoding(true)
                .locations(path)
                          .load()
                          .migrate();
    }
}
