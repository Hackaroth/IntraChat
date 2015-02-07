/*
* IntraChat 
* 
* Class Runnable User State Update
* 
* This file is part of the IntraChat project
* This class provide to check if an user is online
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

import com.google.gson.Gson;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RunnableUserStateUpdate implements Runnable {
    UserList Users;
    private Socket socket;
    private BufferedWriter bufferWriter;
    private BufferedReader bufferReader;
    
    private final String message = "+HELO{[END]}\r\n";

    private final int Timeout = 1000;
    private final int TimeoutSleep = 10000;
    
    public RunnableUserStateUpdate(UserList users) {
        Users = users;    
    }
    
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            for (User u : Users.getUserList()) {
                try {
                    if (UserList.getMySelf().getIpAddress().isEmpty())
                        UserList.getMySelf().setIpAddress(Settings.getLocalIpAddress());

                    String _ipAddress = u.getIpAddress();

                    InetAddress sockAddr = InetAddress.getByName(_ipAddress);
                    
                    if (_ipAddress.isEmpty() || !Settings.isHostReachable(sockAddr, 1000)) {
                        u.setState(UserState.Offline);
                        continue;
                    }

                    socket = new Socket(sockAddr, Settings.getPort());

                    bufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    bufferReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    bufferWriter.flush();

                    bufferWriter.write(message);
                    bufferWriter.flush();

                    long _startTime = new Date().getTime();

                    String _msg = "";

                    while (new Date().getTime() - _startTime < Timeout) {
                        if (bufferReader.ready()) {
                            _msg = (String) bufferReader.readLine() + "\r\n"; 
                        }

                        if (_msg.endsWith("{[END]}\r\n")) {
                            _msg = _msg.substring(0, _msg.indexOf("{[END]}"));
                            break;
                        }
                    }

                    try  {
                        UserInfo _ui = new Gson().fromJson(_msg, UserInfo.class);

                        u.setState(_ui.getState());

                        if (!u.getIcon().equals(_ui.getIcon())) {
                            u.setIcon(_ui.getIcon());

                            Users.UpdateUserToDB(u);
                        }

                    }
                    catch (Exception ex) {
                        u.setState(UserState.Offline);
                    }

                    bufferWriter.close();
                    bufferReader.close();
                    socket.close();

                } catch (UnknownHostException ex) {
                    if (Settings.IsInDebugMode()) 
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    if (Settings.IsInDebugMode()) 
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                Thread.sleep(TimeoutSleep);
            } catch (InterruptedException ex) {
                if (Settings.IsInDebugMode()) 
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
