/*
* IntraChat 
* 
* Class Message Sender
* 
* This file is part of the IntraChat project
* This class provide to send a Message
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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MessageSender {
    private static BufferedWriter bufferWriter; 
    private static Socket socket;
    
    public MessageSender() {

    }
    
    public static void Send(Message message) throws UnknownHostException, IOException {
        
        String _jsonMessage = new Gson().toJson(message);  
         
        _jsonMessage+="{[END]}";
        
        for (User u : message.getRcpt()) {
         
            InetAddress _addr = InetAddress.getByName(u.getIpAddress());
        
            if (Settings.isHostReachable(_addr, 1000)) {
                socket = new Socket(_addr, Settings.getPort());
                bufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                bufferWriter.flush();            
                bufferWriter.write(_jsonMessage);
                bufferWriter.flush();
                bufferWriter.close();
                socket.close();
            }
        }
        
        message.Save(); //Save message to log 
    }
}
