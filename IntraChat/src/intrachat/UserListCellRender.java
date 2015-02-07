/*
* IntraChat 
* 
* Class User List Cell Render
* 
* This file is part of the IntraChat project
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

import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


class UserListCellRender extends JLabel implements ListCellRenderer {
    public UserListCellRender() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
        String imageName = "";   
        User u = (User)o;
        
        setText(u.toString());
        
        if (u.getState() == UserState.Offline) {
            imageName = "Status-Offline.png";
        }
        else if (u.getState() == UserState.Busy) {
            imageName = "Status-Busy.png";
        }
        else if (u.getState() == UserState.Away) {
            imageName = "Status-Away.png";        
        }        
        else if (u.getState() == UserState.Online) {
            imageName = "Status-Online.png";
        }
        
        String path = "resources/images/" + imageName;
        java.net.URL imgURL = getClass().getResource(path);
        
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            setIcon(icon);
        }
        
        if (bln) {
            setBackground(Color.BLUE);
            setForeground(Color.WHITE);
        }
        else {
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
        }

        return this;
    }

}

