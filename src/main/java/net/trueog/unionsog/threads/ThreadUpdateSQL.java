package net.trueog.unionsog.threads;

import net.trueog.unionsog.UnionsOG;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author NeT32
 */
public class ThreadUpdateSQL extends Thread {

    Connection connection;
    String query;
    String sqlType;

    public ThreadUpdateSQL(Connection connection, String query, String sqlType) {

        this.query = query;
        this.connection = connection;
        this.sqlType = sqlType;

    }

    @Override
    public void run() {

        try {

            if (!connection.isClosed()) {

                this.connection.createStatement().executeUpdate(this.query);

            }

        } catch (SQLException ex) {

            if (!ex.toString().contains("not return ResultSet")) {

                UnionsOG.getInstance().getLogger().severe("[Thread] Error at SQL " + this.sqlType + " Query: " + ex);
                UnionsOG.getInstance().getLogger().severe("[Thread] Query: " + this.query);

            }

        }

    }

}
