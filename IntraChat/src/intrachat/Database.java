/*
* IntraChat 
* 
* Class Database
* 
* This file is part of the IntraChat project
* This class provide to communicate with the SQLite Database
* 
* Copyright (C) 2012  Stefano BARILETTI <hackaroth@gmail.com>

* This program is free software: you can redistribute it and/or modify it under the 
* terms of the GNU General Public License as published by the Free Software 
* Foundation, either version 3 of the License, or (at your option) any later version.

* This program is distributed in the hope that it will be useful, but WITHOUT ANY 
* WARRANTY; without even the implied warranty of MERCHANTABILITY or 
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
* for more details.

* You should have received a copy of the GNU General Public License along with
* this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package intrachat;

import java.sql.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private static final String DBName = "IntraChat.db";
    private static final String ConnectionString = "jdbc:sqlite:"+DBName;
    private Connection conn;
    public Database() throws ClassNotFoundException {
        try {
            conn = OpenDB();
                    
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet res = meta.getTables(null, null, null, new String[] {"TABLE"});
            
            if (!res.next()) {
                Statement stat = conn.createStatement(); 
                
                stat.executeUpdate("CREATE TABLE Settings(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, NAME TEXT, VALUE TEXT)"); 
                stat.executeUpdate("CREATE TABLE Users(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, NAME TEXT, IPADDRESS TEXT, ICON TEXT)"); 
                stat.executeUpdate("CREATE TABLE Logs(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, MESSAGEDATE TEXT, SENDER TEXT, RECEIVER TEXT, MESSAGE TEXT)"); 
                
                PreparedStatement query = conn.prepareStatement("INSERT INTO Settings (NAME, VALUE) VALUES (?, ?)");
                query.setString(1, "Port");
                query.setString(2, String.valueOf(Settings.DefaultPort));
                
                query.executeUpdate();
                
                query.setString(1, "MyName");
                query.setString(2, Settings.DefaultUserName);
                
                query.executeUpdate();
                
                query.setString(1, "NotifySound");
                query.setString(2, "");
                
                query.executeUpdate();
                
                query.setString(1, "StartScanRange");
                query.setString(2, String.valueOf(Settings.DefaultStartScanRange));
                
                query.executeUpdate();

                query.setString(1, "EndScanRange");
                query.setString(2, String.valueOf(Settings.DefaultEndScanRange));
                
                query.executeUpdate();
                
                query.setString(1, "UserIcon");
                query.setString(2, Settings.DefaultUserIcon);
                
                query.executeUpdate();                

                query.close();
            }
            res.close(); 
        } catch (SQLException ex) {
            if (Settings.IsInDebugMode())
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Connection OpenDB() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(ConnectionString);      
    }
    
    public void Close() throws SQLException {
        if (conn != null && ! conn.isClosed())
            conn.close();
    }
    
    public void ExecuteQuery(String query) {
        if (conn != null) {
            try {
                if (conn.isClosed())
                    OpenDB();
            
                Statement _statement  =  conn.createStatement();
                _statement.execute(query);
            } catch (SQLException ex) {
                if (Settings.IsInDebugMode())
                    Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                if (Settings.IsInDebugMode())
                    Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }       
    }
    
    public PreparedStatement getPreparedStatement(String query) throws SQLException {
        PreparedStatement _retval = null;

        if (conn != null) {
            try {
                if (conn.isClosed())
                    OpenDB();
            
                _retval =  conn.prepareStatement(query);
            } catch (ClassNotFoundException ex) {
                if (Settings.IsInDebugMode())
                    Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }

        return _retval;
    }
    
    public ResultSet getResultSet(String _query) throws SQLException {
        ResultSet _retval = null;

        if (conn != null) {
            try {
                
                if (conn.isClosed())
                    conn = OpenDB();
            
                if (!conn.isClosed()) {
                    Statement s = conn.createStatement();
                    _retval =  s.executeQuery(_query);
                }
            } catch (ClassNotFoundException ex) {
                if (Settings.IsInDebugMode())
                    Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return _retval;
    }
    
    public int getLastInsertedId() throws SQLException {
        int _retval = -1;
        ResultSet _rs = getResultSet("select last_insert_rowid();");
        
        if (_rs != null && _rs.next())
            _retval = _rs.getInt(1);
        
        if (_rs != null)
            _rs.close();
        
        return _retval; 
    }
}
