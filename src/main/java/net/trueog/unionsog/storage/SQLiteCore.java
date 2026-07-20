package net.trueog.unionsog.storage;

import net.trueog.unionsog.UnionsOG;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * @author cc_madelg
 */
public class SQLiteCore implements DBCore {

    private final Logger log;
    private Connection connection;
    private final String dbLocation;
    private final String dbName;
    private File file;

    /**
     * @param dbLocation the dbLocation to set
     */
    public SQLiteCore(String dbLocation) {

        this.dbName = "UnionsOG";
        this.dbLocation = dbLocation;
        this.log = UnionsOG.getInstance().getLogger();
        initialize();

    }

    private void initialize() {

        if (file == null) {

            File dbFolder = new File(dbLocation);

            if (!dbFolder.exists() && !dbFolder.mkdir()) {

                log.severe("Failed to create database folder!");
                return;

            }

            file = new File(dbFolder.getAbsolutePath() + File.separator + dbName + ".db");

        }

        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());

        } catch (SQLException ex) {

            log.severe("SQLite exception on initialize " + ex);

        } catch (ClassNotFoundException ex) {

            log.severe("You need the SQLite library " + ex);

        }

    }

    @Override
    public Connection getConnection() {

        if (connection == null) {

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
