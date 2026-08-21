/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server;

import com.zaxxer.hikari.*;
import java.sql.*;

/**
 *
 * @author linas
 */
public class PoolToDB
{
    private static final HikariDataSource ds;
    
    private static final String url = "jdbc:mysql://localhost:3306/";
    private static final String user = "root";
    private static final String password = "15964";
    private static final String db_name = "chat";
    
    static
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url + db_name);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);

        ds = new HikariDataSource(config);
    }

    protected static Connection getConnection() throws SQLException
    {
        return ds.getConnection();
    }
}
