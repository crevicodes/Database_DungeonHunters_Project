
package main;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.*;

/**
 * @author Zuhair
 */

public class DatabaseConnector
{

    String DBURL = "jdbc:oracle:thin:@coeoracle.aus.edu:1521:orcl";
    String DBUSER = "b00087962";
    String DBPASS = "b00087962";

    Connection con;
    Statement statement;
    PreparedStatement prepStatement;
    ResultSet rs;

    public DatabaseConnector()
    {
        try
        {
            // Load Oracle JDBC Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            // Connect to Oracle Database
            con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);

        } catch(ClassNotFoundException | SQLException e)
        {
            JLabel label = new JLabel("SQL Error - Retrieving username/password.");
            label.setFont(new Font("Arial", Font.BOLD, 18));
            JOptionPane.showMessageDialog(null, label, "SQL Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    // returns records selected
    public ResultSet executeStatement(String strSQL) throws SQLException
    {
        // make the result set scrolable forward/backward updatable
        statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        // populate valid mgr numbers
        rs = statement.executeQuery(strSQL);
        return rs;
    }

    // update, insert (return number of records affected
    public int executePrepared(String strSQL) throws SQLException
    {
        prepStatement = con.prepareStatement(strSQL);
        return prepStatement.executeUpdate();
    }
}
