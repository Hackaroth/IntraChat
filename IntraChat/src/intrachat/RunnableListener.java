/*
* IntraChat 
* 
* Class Runnable Listener
* 
* This file is part of the IntraChat project
* This class listen for connections from the the clients and show all incoming 
* messages.
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
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RunnableListener  implements Runnable {
    private ServerSocket server;
    private final int MaxClient = 10;
    
    public RunnableListener(int portNumber) throws IOException {
        server = new ServerSocket(portNumber, MaxClient);
    }
    
    @Override
    public void run() {
        try {
            while(!Thread.interrupted() && server != null) {
                Socket socket = server.accept();
                
                RunnableRequestDispacher rd = new RunnableRequestDispacher(socket);
                
                rd.setOnMessageReceivedHandler(new OnMessageReceivedHandler() {

                    @Override
                    public void onMessageReceived(String message) {
                        //Ok...the displacher tell us that there is a message for us,
                        //Diplay it.
                        Message msg = null;
                        try {
                            msg = new Gson().fromJson(message, Message.class);
                        }
                        catch(Exception ex) {
                            //Whooops... there's something bad during the decode of
                            //the message from the Json format.
                            //I think that is usefull to display the message anyway.                            
                            msg = new Message(null, null, message);
                            
                            if (Settings.IsInDebugMode())
                                System.out.println("Not a valid message: " + msg);
                        }

                        
                        if (msg != null) {
                            new Thread(new RunnableShowMessage(msg),"THREAD SHOW RECEIVED MESSAGE").start();
                        }
                    }
                });
                
                new Thread(rd, "THREAD REQUEST DISPACHER").start();
            }
        } catch (IOException ex) {
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(RunnableListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    
}
