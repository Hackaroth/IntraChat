/*
* IntraChat 
* 
* Class Settings
* 
* This file is part of the IntraChat project
* This class contains all the settings of the program.
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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.omg.SendingContext.RunTime;

/**
 *
 * @author hackaroth
 */
public class Settings {
    public static final int DefaultPort = 6300;
    public static final String DefaultUserName = getLocalHostName();
    
    public static final int DefaultStartScanRange = 1;
    public static final int DefaultEndScanRange = 254;
    
    public static final String DefaultUserIcon = "Male;User1.png";
    
    private static int Port;
    private static String myName;     
    private static String NotifySoundFileName = "";
    
    private static int StartScanRange;
    private static int EndScanRange;
    
    private static String UserIcon;
    
    private static PingICMP pingICMP;
    private static boolean debugMode;
    
    
    private Settings () {}

    public static String getLocalIpAddress() {
        
        //mmmm....not sure if this is the right way to get the ip address.
        //i think that more try are necessary, expecially in winzzoz.
        
        //But...anyway...for now it work fine...and i'll use it.
        String _reval = "";
        try {
            Enumeration nets = NetworkInterface.getNetworkInterfaces();
            
            while  (nets.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nets.nextElement();

                Enumeration inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {    
                    InetAddress inetAddress = (InetAddress) inetAddresses.nextElement();

                    if (inetAddress.isSiteLocalAddress())
                        _reval = inetAddress.getHostAddress();
                    }
            }
            
        } catch (SocketException ex) {
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return _reval;
    }
    
    private static String getLocalHostName() {
        String _retval = "";
        try {
            _retval = java.net.InetAddress.getLocalHost().getHostName();
        }
        catch (Exception ex) {
            _retval = "New User";
        }
        
        return _retval;
    
    }
    
    public static boolean IsInDebugMode() {
        return debugMode;
    }
    
    public static void setDebugMode(boolean value) {
        debugMode = value; 
    }

    public static boolean Save() {
        boolean _retval = false;
        try {
            
            Database DB = new Database();

            PreparedStatement _statement = DB.getPreparedStatement("UPDATE Settings SET VALUE = ? WHERE NAME = ?");
            
            if (_statement != null) {
                _statement.setString(1, String.valueOf(Port));
                _statement.setString(2, "Port");
                
                _statement.executeUpdate();
                
                _statement.setString(1, myName);
                _statement.setString(2, "MyName");
                
                _statement.executeUpdate();
                
                _statement.setString(1, NotifySoundFileName);
                _statement.setString(2, "NotifySound");
                
                _statement.executeUpdate();
                
                _statement.setString(1, String.valueOf(StartScanRange));
                _statement.setString(2, "StartScanRange");
                
                _statement.executeUpdate();

                _statement.setString(1, String.valueOf(EndScanRange));
                _statement.setString(2, "EndScanRange");
                
                _statement.executeUpdate();
                
                _statement.setString(1, UserIcon);
                _statement.setString(2, "UserIcon");
                
                _statement.executeUpdate();                 
                
                _retval = (_statement.executeUpdate() > 0)?true:false;
                _statement.close();
            }
            DB.Close();
        } catch (ClassNotFoundException ex) {
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {           
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return _retval;
    } 
    
    public static int getPort() {
        return Port;
    }
    
    public static void setPort(int port) {
        Port = port;
    }  

    
    public static int getStartScanRange() {
        return StartScanRange;
    }
    
    public static void setStartScanRange(int _startRange) {
        StartScanRange = _startRange;
    }  
    
    public static int getEndScanRange() {
        return EndScanRange;
    }
    
    public static void setEndScanRange(int _endRange) {
        EndScanRange = _endRange;
    }  
    
    public static String getMyName() {
        return myName;
    }
    
    public static void setMyName(String myname) {
        myName = myname;
    } 
    
    public static String getUserIcon() {
        return UserIcon;
    }
    
    public static void setUserIcon(String userIcon) {
        UserIcon = userIcon;
    }
    
    public static boolean PlayNotifySound() {
        return !NotifySoundFileName.isEmpty();
    }
    
    public static void setNotifySoundFileName(String fileName) {
        NotifySoundFileName = fileName;
    }

    public static String getNotifySoundFileName() {
        return NotifySoundFileName;
    }

    public static UserState getUserStateByName(String name) {
        UserState _retval = UserState.Offline;
        
        if (name.equals("ONLINE")) {
            _retval = UserState.Online;
        }
        else if (name.equals("OFFLINE")) {
            _retval = UserState.Offline;
        }
        else if (name.equals("BUSY")) {
            _retval = UserState.Busy;
        }
        else if (name.equals("AWAY")) {
            _retval = UserState.Away;
        }        
        
        return _retval;
    }
    
    public synchronized static boolean isHostReachable(InetAddress addr, int timeout) {
        //This method is the result of many long night work. 
        //The problem is the method isReacheble of the class InetAddress, because
        //in linux work fine and fast....in winzozz don't work at all. :((
        //So to solve this problem, if i'm on a windows OS, i send native ICMP Echo Request through JNI
        //For more information see the msdn help fro windows "IcmpSendEcho" function.
        //It work fine but...it is slow.  
        
        //The timeout parameter is used only in windows, because the windows function IcmpSendEcho fail
        //if timeout is under 1000, but this time for autoscan is an eternity.
        
        //In linux 100ms the function isReachable with 100ms of timeout work fine.
        
        boolean _retval = false;
        
        if (System.getProperty("os.name").startsWith("Windows")) {
            if (pingICMP == null)
                pingICMP = new PingICMP();

            long res = pingICMP.isReachable(addr.getHostAddress(), timeout);            
            
            if (res == pingICMP.IP_SUCCESS)
                _retval = true;
            else
                _retval = false;
            
            if (IsInDebugMode()) {
                System.out.println("Ping result: " + String.valueOf(res)); 
            }
        }
        else {
            try {
                _retval = addr.isReachable(100);
            } catch (IOException ex) {
                if (Settings.IsInDebugMode()) 
                    Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
                
                _retval = false;
            }
        }
        
        return _retval;
    }
}
