/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server;
import java.sql.*;
import java.util.*;
/**
 *
 * @author linas
 */
public class Mysql 
{   
    private final int sqlDublicateErrorCode = 1062;
    
    protected boolean createUser(String username,String hashedPassword)
    {
        String sql = "INSERT INTO users (username,password) VALUES (?,?)";

        try (Connection conn = PoolToDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1,username);
            ps.setString(2,hashedPassword);
            ps.executeUpdate();
            return true;
            
        } catch(SQLException e){
            if (e.getErrorCode() == sqlDublicateErrorCode)
            {
                System.out.println("Username already exists.");
            }
            else
            {
                e.printStackTrace();
            }
            return false;
        }
    }

    protected String getPasswordByUsername(String username)
    {
        String sql = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = PoolToDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1,username);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return rs.getString("password");
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }
    
    protected void setActive(String username,boolean isActive)
    {
        String sql = "UPDATE users "
                   + "SET is_active = ? "
                   + "WHERE username = ?";
        
        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {  
            ps.setBoolean(1, isActive);
            ps.setString(2, username);

            ps.executeUpdate();
            
        }catch(SQLException e){
            e.printStackTrace();
        }
    
    }
    
    protected boolean isOnline(String username)
    {
        String sql = "SELECT is_active FROM users WHERE username = ?";

        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1,username);

            try(ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return rs.getBoolean("is_active");
                }
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }
    
    protected String findUser(String username)
    {
        String sql = "SELECT username FROM users WHERE username = ?";

        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1,username);

            try(ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return rs.getString("username");
                }
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }
    
    protected boolean addFriend(String username,String friend)
    {
        int un = getUserIdByUsername(username);
        int fn = getUserIdByUsername(friend);

        String sql = "INSERT INTO friends (sender_id,receiver_id,status) VALUES (?,?,'PENDING');";

        try (Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            if (un == -1 || fn == -1 || un == fn)
            {
                return false;
            }
            ps.setInt(1, un);
            ps.setInt(2, fn);

            ps.executeUpdate();
            return true;

        }catch(SQLException e){
            
        if(e.getErrorCode() == sqlDublicateErrorCode)
        {
            System.out.println("Friend is already in the current list.");
        }
        else
        {
            e.printStackTrace();
        }
        return false;
        }
    }
    
    protected List<String> getFriendListActive(int userId)
    {
        List<String> friends = new ArrayList<>();

        String sql = "SELECT u.username " +
             "FROM friends f " +
             "JOIN users u ON " +
             "    (u.id = f.receiver_id OR u.id = f.sender_id) " +
             "WHERE (f.sender_id = ? OR f.receiver_id = ?) " +
             "AND u.id != ? " +
             "AND f.status = 'ACCEPTED'";
        
        try(Connection conn = PoolToDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1,userId);
            ps.setInt(2,userId);
            ps.setInt(3,userId);

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                   friends.add(rs.getString("username"));
                }
            }
            
        }catch(SQLException e){
            e.printStackTrace();
        }
        return friends;
    }
    
    protected List<String> getFriendList(int userId)
    {
        List<String> friends = new ArrayList<>();

        String sql =
            "SELECT u.username, u.is_active " +
            "FROM friends f " +
            "JOIN users u ON ( " +
            "    (f.sender_id = ? AND u.id = f.receiver_id) " +
            " OR (f.receiver_id = ? AND u.id = f.sender_id) " +
            ") " +
            "WHERE f.status = 'ACCEPTED'";

        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    friends.add(rs.getString("username") + "::" +rs.getBoolean("is_active"));
                }
            }

        }catch(SQLException e) {
        e.printStackTrace();
        }

        return friends;
    }
    
    protected List<String> getPendingFriendRequests(String username)
    {
        List<String> list = new ArrayList<>();

        String sql =
            "SELECT u.username " +
            "FROM friends f " +
            "JOIN users u ON f.sender_id = u.id " +
            "JOIN users r ON f.receiver_id = r.id " +
            "WHERE r.username = ? AND f.status = 'PENDING'";

        try(Connection conn = PoolToDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1,username);

            try(ResultSet rs = ps.executeQuery())
            {
                while(rs.next())
                {
                   list.add(rs.getString("username"));
                }
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
        
        return list;
    }
    
    protected boolean deletePendingFriendRequest(String username, String senderUsername)
    {
        String sql =
            "DELETE FROM friends " +
            "WHERE sender_id = (SELECT id FROM users WHERE username = ?) " +
            "AND receiver_id = (SELECT id FROM users WHERE username = ?) " +
            "AND status = 'PENDING'";

        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, senderUsername);
            ps.setString(2, username);

            int rowsDeleted = ps.executeUpdate();

            return rowsDeleted > 0;

        }catch(SQLException e) {
        e.printStackTrace();
        return false;
        }
    }
    
    protected void acceptFriendship(String senderUsername,String receiverUsername)
    {
        String sql =
            "UPDATE friends f " +
            "JOIN users s ON f.sender_id = s.id " +
            "JOIN users r ON f.receiver_id = r.id " +
            "SET f.status = 'ACCEPTED' " +
            "WHERE ((s.username = ? AND r.username = ?) " +
            "OR (s.username = ? AND r.username = ?)) " +
            "AND f.status = 'PENDING'";

        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1,senderUsername);
            ps.setString(2,receiverUsername);
            ps.setString(3,receiverUsername);
            ps.setString(4,senderUsername);

            ps.executeUpdate();
        
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    protected int getUserIdByUsername(String username)
    {
        String sql = "SELECT id FROM users WHERE username = ?";

        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1,username);

            try(ResultSet rs = ps.executeQuery())
            {
                if(rs.next())
                {
                    return rs.getInt("id");
                }
            }

        }catch(SQLException e){
            e.printStackTrace();
        }

        return -1;
    }
    
    protected void storeMessage(String sender,String receiver,String message)
    {
        int temp_sender = getUserIdByUsername(sender);
        int temp_receiver = getUserIdByUsername(receiver);
        
        String sql = "INSERT INTO messages (sender_id,receiver_id,message) VALUES (?,?,?)";
        
        try (Connection conn = PoolToDB.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1,temp_sender);
            ps.setInt(2,temp_receiver);
            ps.setString(3,message);
            
            ps.executeUpdate();

        }catch(SQLException e){
        e.printStackTrace();
        }
    }
    
    protected void storeFiles(String sender,String receiver,String filename,byte[] fileBytes)
    {
        int temp_sender = getUserIdByUsername(sender);
        int temp_receiver = getUserIdByUsername(receiver);
        
        String sql = "INSERT INTO files (sender_id,receiver_id,filename,data) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = PoolToDB.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql))
        {
           ps.setInt(1, temp_sender);
           ps.setInt(2, temp_receiver);
           ps.setString(3, filename);
           ps.setBytes(4, fileBytes);

           ps.executeUpdate();

        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    protected List<String> receiveMessages(String sender,String receiver)
    {
        List<String> messages = new ArrayList<>();

        String sql =
            "SELECT u.username AS sender, m.message, m.created_at "+
            "FROM messages m " +
            "JOIN users u ON m.sender_id = u.id " +
            "JOIN users f ON m.receiver_id = f.id " +
            "WHERE (u.username = ? AND f.username = ?) " +
                "OR (u.username = ? AND f.username = ?) " +
            "ORDER BY m.created_at ASC;";

        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1,sender);
            ps.setString(2,receiver);
            ps.setString(3,receiver);
            ps.setString(4,sender);

            try(ResultSet rs = ps.executeQuery())
            {
                while(rs.next())
                {
                    messages.add(rs.getString("sender") + "::" + rs.getString("message")+ "::" + rs.getTimestamp("created_at").getTime());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }
    
    protected List<String> receiveFiles(String sender, String receiver)
    {
        List<String> files = new ArrayList<>();

        String sql =
            "SELECT d.id AS file_id, " + 
            "u.username AS sender, " +
            "d.filename, " +
            "d.created_at " +
            "FROM files d " +
            "JOIN users u ON d.sender_id = u.id " +
            "JOIN users f ON d.receiver_id = f.id " +
            "WHERE (u.username = ? AND f.username = ?) " +
            "OR (u.username = ? AND f.username = ?) " +
            "ORDER BY d.created_at ASC;";

        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, sender);
            ps.setString(2, receiver);
            ps.setString(3, receiver);
            ps.setString(4, sender);

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    files.add(rs.getString("sender") + "::" +rs.getString("file_id") + "::" +
                          rs.getString("filename") + "::" + rs.getTimestamp("created_at").getTime());
                }
            }

        }catch(SQLException e){
        e.printStackTrace();
        }

        return files;
    }
    protected byte[] downloadFile(String file_id)
    {
        byte[] fileData = null;
        String sql =
            "SELECT data " + 
            "FROM files " +
            "WHERE id = ?";

        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, Integer.parseInt(file_id));
        

            try (ResultSet rs = ps.executeQuery())
            {
                if(rs.next())
                {
                    fileData = rs.getBytes("data");
                }
            }

        }catch(SQLException e){
        e.printStackTrace();
        }

        return fileData;
    }
    
    protected boolean createKey(String username,byte[] key)
    {
        int user_id = getUserIdByUsername(username);
        
        String sql = "INSERT INTO security (user_id,enc) VALUES (?,?)";

        try (Connection conn = PoolToDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1,user_id);
            ps.setBytes(2,key);
            ps.executeUpdate();
            return true;
            
        }catch(SQLException e){
            e.printStackTrace();         
            return false;
        }
    }
    
    protected void resetAllUsersOffline()
    {
        String sql = "UPDATE users SET is_active = false";

        try (Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.executeUpdate();
        }catch (SQLException e){
        e.printStackTrace();
        }
    }
    
    public void insertKey(int userId, byte[] encrypted)
    {
        String sql = "INSERT INTO security(user_id, enc, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";

        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ps.setBytes(2, encrypted);

            ps.executeUpdate();
        }catch (SQLException e){
        e.printStackTrace();
        }
    }
    public byte[] getKeyByUsername(String username)
    {
        String sql =
            "SELECT s.enc " +
            "FROM security s " +
            "JOIN users u ON s.user_id = u.id " +
            "WHERE u.username = ?";

        try(Connection conn = PoolToDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery())
            {
                if(rs.next())
                {
                    return rs.getBytes("enc");
                }
            }
        }catch (SQLException e){
        e.printStackTrace();
        }

        return null;
    }
      
}
