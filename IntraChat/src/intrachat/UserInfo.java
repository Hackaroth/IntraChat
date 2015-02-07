/*
* IntraChat 
* 
* Class User Info
* 
* This file is part of the IntraChat project
* This class is the container of all the user information sent to the other clients
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

public class UserInfo {
    private UserState State;
    private String Icon;
    
    public UserInfo(UserState state, String icon) {
        State = state;
        Icon = icon;
    }
    
    public UserState getState() {
        return State;
    }
    
    public void setState(UserState state) {
        State = state;
    }
    
    public String getIcon() {
        return Icon;
    }
    
    public void setIcon (String icon) {
        Icon = icon;
    }
}
