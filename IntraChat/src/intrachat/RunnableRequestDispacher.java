/*
* IntraChat 
* 
* Class Runnable Request Dispacher
* 
* This file is part of the IntraChat project
* This class provide to perform the communication with the clients
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
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableRequestDispacher implements Runnable {
    private Socket socket;
    private BufferedReader bufferReader; 
    private BufferedWriter bufferWriter;
    
    private final long Timeout = 5000;
    private OnMessageReceivedHandler onMessageReceived;
    
    public RunnableRequestDispacher (Socket socket) {
        this.socket = socket;
        onMessageReceived = null;
    }
    
    public void setOnMessageReceivedHandler(OnMessageReceivedHandler onMessageReceivedHandler) {
        this.onMessageReceived = onMessageReceivedHandler;
    }
    
    @Override
    public void run() {
        try {
            bufferReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            long _startTime = new Date().getTime();

            String _msg = "";

            while (new Date().getTime() - _startTime < Timeout) {
                if (bufferReader.ready()) {
                    _msg += (String) bufferReader.readLine() +"\r\n"; 
                }
                
                if (_msg.endsWith("{[END]}\r\n")) {
                    _msg = _msg.substring(0, _msg.indexOf("{[END]}"));
                    break;
                }
            }
           
            if (_msg.equals("+HELO")) {
                //In this case the client ask us our state.
                
                String _state = new Gson().toJson(UserList.getMySelf().getInfo());
                
                bufferWriter.write(_state+"{[END]}");
                bufferWriter.flush();
            }

            if (_msg.equals("+WHOIS")) {
                //In this case the client ask who we are.
                //And we make our presentation. :-P
                
                String _myself = new Gson().toJson(UserList.getMySelf());

                bufferWriter.write(_myself+"{[END]}");
                bufferWriter.flush();
            }
            
            bufferReader.close();
            bufferWriter.close();

            socket.close();
            
            if (onMessageReceived != null && !_msg.trim().isEmpty() && !_msg.equals("+HELO") && !_msg.equals("+WHOIS")) {
                //In this case the client has sent us a message. Raise the OnMessageReceived event
                // to display it.
                onMessageReceived.onMessageReceived(_msg); 
            }
            
        } catch (IOException ex) {
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(RunnableListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
