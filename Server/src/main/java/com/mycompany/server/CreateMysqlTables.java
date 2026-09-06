/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server;
import java.util.*;
import java.sql.*;
/**
 *
 * @author linas
 */
public class CreateMysqlTables
{
    private static final String url = "jdbc:mysql://localhost:3306/";
    private static final String user = "root";
    private static final String password = "12345";
    private static final String db_name = "chat";
    
   
    protected static void initDatabaseTable()
    {

        Scanner sc = new Scanner(System.in);
        String input;

        System.out.println("Create Database - \"DB\"");
        System.out.println("Create Tables - \"TB\"");
        System.out.println("Quit - \"q\"");

        while(true)
        {
            input = sc.nextLine();

            if("q".equalsIgnoreCase(input)) {
                System.out.println("Exiting MySQL...");
                sc.close();
                break;
            }

            if("DB".equalsIgnoreCase(input))
            {
                createDatabase();
            } 
            else if("TB".equalsIgnoreCase(input))
            {
                initTables();
            }
            else
            {
                System.out.println("Invalid input.");
            }
        }
    }

    private static void createDatabase()
    {
        try(Connection conn = DriverManager.getConnection(url,user,password);
             Statement st = conn.createStatement())
        {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + db_name);
            System.out.println("Database created.");

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private static void initTables()
    {
        try(Connection conn = DriverManager.getConnection(url+db_name,user,password);
             Statement st = conn.createStatement())
        {
            createUserTable(st);
            createFriendList(st);
            createMessageList(st);
            createFilesList(st);
            createKeysList(st);
         
            System.out.println("Tables created.");

        } catch (SQLException e) {
            System.out.println("Make sure database exists first (use DB command).");
            e.printStackTrace();
        }
    }
    private static void createUserTable(Statement st) throws SQLException
    {
        st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "is_active BOOLEAN DEFAULT FALSE,"+
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
    }
    
    private static void createFriendList(Statement st) throws SQLException
    {
        st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS friends (" +
                "sender_id INT," +
                "receiver_id INT," +
                "status ENUM('PENDING', 'ACCEPTED', 'DECLINE') DEFAULT 'PENDING'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "PRIMARY KEY(sender_id,receiver_id)," +
                "FOREIGN KEY(sender_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY(receiver_id) REFERENCES users(id) ON DELETE CASCADE," +
                "CONSTRAINT no_self_friend CHECK (sender_id <> receiver_id)" +
                ")"
            );
    }
    
    private static void createMessageList(Statement st) throws SQLException
    {
        st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS messages ("+
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "sender_id INT NOT NULL," +
                "receiver_id INT NOT NULL," +
                "message VARCHAR(255) NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "FOREIGN KEY(sender_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY(receiver_id) REFERENCES users(id) ON DELETE CASCADE," +
                "CONSTRAINT message_no_self_friend CHECK (sender_id <> receiver_id)" +
                ")"
            );
    }
    
    private static void createFilesList(Statement st) throws SQLException
    {
        st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS files ("+
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "sender_id INT NOT NULL," +
                "receiver_id INT NOT NULL," +
                "filename VARCHAR(255) NOT NULL," +
                "data LONGBLOB," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "FOREIGN KEY(sender_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY(receiver_id) REFERENCES users(id) ON DELETE CASCADE," +
                "CONSTRAINT file_no_self_friend CHECK (sender_id <> receiver_id)" +
                ")"
            );
    }
    
    private static void createKeysList(Statement st) throws SQLException
    {
        st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS security (" +
                "user_id INT PRIMARY KEY," +
                "enc LONGBLOB," +
                "created_at TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")"
            );
    }
}
