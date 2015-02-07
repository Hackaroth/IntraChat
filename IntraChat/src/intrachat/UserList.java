/*
* IntraChat 
* 
* Class UserList
* 
* This file is part of the IntraChat project
* This class is the container of all the users, here are also performed all the operation of 
* INSERT, UPDATE and DELETE of an user in the the sqlite database.
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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserList extends Observable{
    
    private static User MySelf;
    private ArrayList<User> Users;
    
    public UserList (User myself) {
        MySelf = myself;
        Users = new ArrayList<User>();
    }
    
    public int IndexOf(User user) {
        int _retval = -1;
        for (int i = 0; i < Users.size(); i++) {
            if (((User)Users.get(i)).getIpAddress().equals(user.getIpAddress())) {
                _retval = i;
                break;
            }
        }
        
        return _retval;
    }
    
    private int AddUserToDB(User user) {
        int _retval = -1;
        try {
            
            Database DB = new Database();

            PreparedStatement _statement = DB.getPreparedStatement("INSERT INTO Users(NAME, IPADDRESS, ICON) VALUES(?,?,?)");
            
            if (_statement != null) {
                _statement.setString(1, user.getName());
                _statement.setString(2, user.getIpAddress());
                _statement.setString(3, user.getIcon());

                if (_statement.executeUpdate() > 0)
                    _retval = DB.getLastInsertedId();

                _statement.close();
            }
            DB.Close();

        } catch (ClassNotFoundException ex) {
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(UserList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(UserList.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return _retval;
    }

    private boolean DeleteUserFromDB(int idUser) {
        boolean _retval = false;
        try {
            
            Database DB = new Database();

            PreparedStatement _statement = DB.getPreparedStatement("DELETE FROM Users WHERE ID = ?");
            
            if (_statement != null) {
                _statement.setInt(1, idUser);
                _retval = (_statement.executeUpdate() > 0)?true:false;
                _statement.close();
            }
            DB.Close();
        } catch (ClassNotFoundException ex) {
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(UserList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(UserList.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return _retval;
    }
    
    public void UpdateUserToDB(User user) {
        boolean _retval = false;
        try {
            
            Database DB = new Database();

            PreparedStatement _statement = DB.getPreparedStatement("UPDATE Users SET NAME = ?, IPADDRESS = ?, ICON = ? WHERE ID = ?");
            
            if (_statement != null) {
                _statement.setString(1, user.getName());
                _statement.setString(2, user.getIpAddress());
                _statement.setString(3, user.getIcon());
                _statement.setInt(4, user.getId());
                
                _retval = (_statement.executeUpdate() > 0)?true:false;
                _statement.close();
            }
            DB.Close();
        } catch (ClassNotFoundException ex) {
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(UserList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(UserList.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (_retval && user.onUserChanged != null)
            user.onUserChanged.OnUserChanged(user);
    }    
    
    public boolean AddUser(User user) {
        boolean _retval = false;

        if (user != null && IndexOf(user) < 0) {
            if (user.getId() < 0) {
                int _idNewUser = AddUserToDB(user);
                
                if (_idNewUser > 0) {
                    user.setId(_idNewUser);
                        Users.add(user);
                }
            }
            else {
                Users.add(user);
            }
            
            _retval = true;
        }
        else {
            _retval = false;
        }
        
        return _retval;
    }
    
    public void DeleteUser(User user) {
        int _idx = IndexOf(user);
        
        if (_idx >= 0) {
            //This method return true if the user has been deleted from Database, false otherwise... but is really usefull this return value ????
            DeleteUserFromDB(user.getId()); 
            
            Users.remove(_idx);
        }
    }
    
    public User getUserAt(int index) {
        User _retval = null;
        
        if (Users.size() > 0 && index >= 0 && index < Users.size())
            _retval = Users.get(index);
        
        return _retval;        
    }
    
    public static User getMySelf(){
        return MySelf;
    }
    
    public int Count() {
        return Users.size();
    }
    
    public ArrayList<User> getUserList () {
        //To avoid Concurrent Modification Exception, return a clone of the Users ArrayList so different thread do not
        //access to the same array list at the same time.
        //I don't know if this is the best solution, i promise to improve it in the future.
        return (ArrayList<User>)Users.clone();
        
    }
}
