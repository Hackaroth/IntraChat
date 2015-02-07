/*
* IntraChat 
* 
* Class Message
* 
* This file is part of the IntraChat project
* This class contains all the infomation that will be send when a client send a message
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Message {
    private User Sender;
    private ArrayList<User> Rcpt;
    private String Body;
    
    public Message() {
        Sender = UserList.getMySelf();
        Rcpt = new ArrayList<User>();
        Body = "";
    }
    
    public Message(User sender, ArrayList<User> rcpt, String body) {
        Sender = sender;
        Rcpt = rcpt;
        Body = body;
    }
    
    public User getSender() {
        return Sender;
    }
    
    public String getBody() {
        return Body;
    }

    public void setBody(String message) {
        Body = message;
    }
    
    public ArrayList<User> getRcpt() {
        return Rcpt;
    }
    
    public void setRcpt(ArrayList<User> rcpt) {
        if (rcpt != null)
            Rcpt = rcpt;
    }
    
    public boolean Save() {
        boolean _retval = false;
        try {
            
            Database DB = new Database();
            PreparedStatement _statement = DB.getPreparedStatement("INSERT INTO Logs(MESSAGEDATE, SENDER, RECEIVER, MESSAGE) VALUES (?, ?, ?, ?)");
            
            if (_statement != null) {
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                String _now = dateFormat.format(date);
                
                String _rcpt = "";
                
                for (User u : getRcpt())
                    _rcpt += u.getName() + " <" + u.getIpAddress() +">;";
                
                _statement.setString(1, _now);
                _statement.setString(2, getSender().getName()+ " <" + getSender().getIpAddress() +">");
                _statement.setString(3, _rcpt);
                _statement.setString(4, Body);
                
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
    
}
