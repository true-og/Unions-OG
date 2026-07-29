package net.trueog.unionsog.storage;

import net.trueog.unionsog.UnionsOG;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author cc_madelg
 */
public class MySQLCore implements DBCore {

    private final Logger log;
    private Connection connection;
    private final String host;
    private final String username;
    private final String password;
    private final String database;
    private final int port;

    /**
     * @param host     The host
     * @param database The database
     * @param username The username
     * @param password The password
     */
    public MySQLCore(String host, String database, int port, String username, String password) {

        this.database = database;
        this.port = port;
        this.host = host;
        this.username = username;
        this.password = password;
        this.log = UnionsOG.getInstance().getLogger();
        initialize();

    }

    private void initialize() {

        loadDriver();

        try {

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database
                            + "?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&useSSL=false",
                    username, password);

        } catch (SQLException e) {

            log.severe("SQLException! " + e.getMessage());

        }

    }

    /**
     * Loads whichever driver class the server provides. Connector/J 8 dropped the
     * legacy class name, so a missing class must not stop the connection attempt:
     * JDBC service discovery still finds a driver that is on the classpath.
     */
    private void loadDriver() {

        for (String driver : List.of("com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver", "org.mariadb.jdbc.Driver")) {

            try {

                Class.forName(driver);
                return;

            } catch (ClassNotFoundException ignored) {

                // Try the next known driver class name.

            }

        }

        log.warning("No known MySQL driver class was found, falling back to JDBC service discovery.");

    }

    @Override
    public Connection getConnection() {

        try {

            if (connection == null || connection.isClosed() || !connection.isValid(0)) {

                initialize();

            }

        } catch (SQLException e) {

            initialize();

        }

        return connection;

    }

    @Override
    public void close() {

        try {

            if (connection != null) {

                connection.close();

            }

        } catch (Exception e) {

            log.severe("Failed to close database connection! " + e.getMessage());

        }

    }

}
