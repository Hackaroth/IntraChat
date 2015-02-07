/*
* IntraChat 
* 
* Main class
* 
* This file is part of the IntraChat project
* This is the main class of the project.
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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class IntraChat {

    private static UserList Users;
    private static Database DB = null;
    
    public static void main(String[] args) {
        
        if (args.length > 0 && args[0].equals("debug"))
            Settings.setDebugMode(true);
        else
            Settings.setDebugMode(false);
            
        
        OpenDatabase();
        LoadSettings();
        LoadUsers();
        CloseDatabase();
        
        if (StartThreads()) {
            FormMain _mf = new FormMain(Users);
            _mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            _mf.setVisible(true);
        }
        else
            return;
    }
    
    private static void OpenDatabase() {
        try {
            DB = new Database();
        } catch (ClassNotFoundException ex) {
            if (Settings.IsInDebugMode())            
                Logger.getLogger(IntraChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void CloseDatabase() {
        try {
            DB.Close();
        } catch (SQLException ex) {
            if (Settings.IsInDebugMode())            
                Logger.getLogger(IntraChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void LoadSettings() {
        ResultSet rs = null;
        
        try {
            String _value = "";
            String _port = "";
            String _notifySound = "";
            String _myName = "";
            
            String _startScanRange = "";
            String _endScanRange = "";
            
            
            String _userIcon = "";
            
            rs = DB.getResultSet("SELECT NAME, VALUE FROM Settings");
            
            while (rs != null && rs.next()) {
                if (rs.getString(1).equals("Port")) {
                    _port = rs.getString(2);
                }
                else if (rs.getString(1).equals("MyName")) {
                    _myName = rs.getString(2);
                }
                else if (rs.getString(1).equals("NotifySound")) {
                    _notifySound = rs.getString(2);
                }
                else if (rs.getString(1).equals("StartScanRange")) {
                    _startScanRange = rs.getString(2);
                }
                else if (rs.getString(1).equals("EndScanRange")) {
                    _endScanRange = rs.getString(2);
                }    
                else if (rs.getString(1).equals("UserIcon")) {
                    _userIcon = rs.getString(2);
                }                  
            }
            
            if (_port.isEmpty()) {
                Settings.setPort(Settings.DefaultPort);
            }
            else {
                Settings.setPort(Integer.valueOf(_port));
            }
            
            if (_myName.isEmpty()) {
                Settings.setMyName(Settings.DefaultUserName);
            }
            else {
                Settings.setMyName(_myName);
            }            
            
            if (_notifySound.isEmpty()) {
                Settings.setNotifySoundFileName("");
            }
            else {
                Settings.setNotifySoundFileName(_notifySound);
            }
            
            if (_startScanRange.isEmpty()) {
                Settings.setStartScanRange(Settings.DefaultStartScanRange);
            }
            else {
                Settings.setStartScanRange(Integer.valueOf(_startScanRange));
            }

            if (_endScanRange.isEmpty()) {
                Settings.setEndScanRange(Settings.DefaultEndScanRange);
            }
            else {
                Settings.setEndScanRange(Integer.valueOf(_endScanRange));
            }
            
            if (_userIcon.isEmpty()) {
                Settings.setUserIcon(Settings.DefaultUserIcon);
            }
            else {
                Settings.setUserIcon(_userIcon);
            }            

        } catch (SQLException ex) {
            Settings.setPort(Settings.DefaultPort);
            Settings.setMyName(Settings.DefaultUserName);
            Settings.setNotifySoundFileName(""); 
            Settings.setStartScanRange(Settings.DefaultStartScanRange);
            Settings.setEndScanRange(Settings.DefaultEndScanRange);
            Settings.setUserIcon(Settings.DefaultUserIcon);
        }
        finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {
                    if (Settings.IsInDebugMode())                    
                        Logger.getLogger(IntraChat.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }
    
    private static void LoadUsers() {
        ResultSet rs = null;
        User mySelf = new User(-1, Settings.getMyName(), Settings.getLocalIpAddress(), Settings.getUserIcon());
        mySelf.setState(UserState.Online);
        
        Users = new UserList(mySelf);
        
        try {
            rs = DB.getResultSet("SELECT ID, NAME, IPADDRESS, ICON FROM Users");

            while (rs != null && rs.next()) {
                Users.AddUser(new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            }    
        }
        catch (Exception ex) {
            if (Settings.IsInDebugMode())                    
                Logger.getLogger(IntraChat.class.getName()).log(Level.SEVERE, null, ex);            
        }
        finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {
                    if (Settings.IsInDebugMode()) 
                        Logger.getLogger(IntraChat.class.getName()).log(Level.SEVERE, null, ex);
                }
        }        
    }
    
    private static boolean StartThreads() {
        boolean _retval = false;
        try {
            Thread listerer = new Thread(new RunnableListener(Settings.getPort()),"THREAD LISTENER");
            listerer.start();
            
            Thread userStateUpdate = new Thread(new RunnableUserStateUpdate(Users),"THREAD CHECK USER STATE");
            userStateUpdate.start();
            
            _retval = true;
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "IntraChat", JOptionPane.OK_OPTION);
            _retval = true;
        }
        
        return _retval;
    }
}
