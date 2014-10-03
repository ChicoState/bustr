package com.bustr.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class UserDatabase
{
	private static final String CONNECTION = "jdbc:mysql://127.0.0.1/bustr";
	private static final String dbClassName = "com.mysql.jdbc.Driver";
	private static Connection connection;
	private static Statement stmt;
	private ResultSet rs;

    /*	Adds a user to the database.
     *  @param String username, the name of the new user
     *  @param String password, the password for the new user.
     *  Returns true if successful, false otherwise
     * */
    public static boolean addUser(String username, String password) throws ClassNotFoundException, SQLException 
    {
    	Class.forName(dbClassName);
        Properties p = new Properties();
        p.put("user", "root");
        p.put("password", "root");
        connection  = DriverManager.getConnection(CONNECTION, p);
        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
    		    ResultSet.CONCUR_READ_ONLY);
        String sql = "INSERT INTO users VALUES "
    		    + username + " "
    		    + password;
        
        try { 
        	stmt.execute(sql);
        } catch (Exception e) {
        	System.out.println("[-] Failed to execute sql stmt " + sql);
        	e.printStackTrace();
        	connection.close();
        	return false;
        }
        connection.close();
        return true;
    }
}