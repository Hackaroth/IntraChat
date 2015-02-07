/*
* IntraChat 
* 
* Class User
* 
* This file is part of the IntraChat project
* This class contains all the information of an user-
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

public class User {
    private int Id;
    private String Name;
    private String IpAddress;
    private UserState State;
    private String Icon;
    
    public OnUserChangedHandler onUserChanged;
   
    public User(int id, String nome, String ipAddress, String icon) {
        Id = id;
        Name = nome;
        IpAddress = ipAddress;
        State = State.Offline;
        Icon = icon;
        
        onUserChanged = null;
    }
    
    public String getIpAddress() {
        return IpAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        IpAddress = ipAddress;
    }
    
    public String getName() {
        return Name;
    } 
    
    public void setName(String name) {
        Name = name;
    }       
    
    public int getId() {
        return Id;
    }
    
    public void setId(int id) {
        Id = id;
    }
    
    public synchronized UserState getState() {
        //This method is synchronized beacuse more then one thread can read or write the user state
        return State;
    }
    
    public synchronized void setState(UserState state) {
        //This method is synchronized beacuse more then one thread can read or write the user state

        boolean _needToRefresh = false;
        if (State == state) 
            _needToRefresh = false;
        else
            _needToRefresh = true;
        
        State = state;
        
        if (onUserChanged != null && _needToRefresh) //Refreh the UI only if is necessary
            onUserChanged.OnUserChanged(this); 
    }
    
    public String toString() {
        return Name; 
    }
    
    public void setOnUserChangedHandler(OnUserChangedHandler onUserChangedHandler) {
        onUserChanged = onUserChangedHandler;
    }
    
    public String getIcon() {
        return Icon;
    }
    
    public void setIcon(String icon) {
        Icon = icon;
    }    
    
    public UserInfo getInfo() {
        return new UserInfo(getState(), getIcon());
    }
}
