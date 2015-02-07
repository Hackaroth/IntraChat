/*
* IntraChat 
* 
* Class Runnable Autoscan
* 
* This file is part of the IntraChat project
* This class scan the network to find other users.
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableAutoscan implements Runnable {
    private Socket socket;
    private BufferedWriter bufferWriter;
    private BufferedReader bufferReader;
    
    private final String whois = "+WHOIS{[END]}\r\n";
    private final String RetMsg = "+OK";
    
    private final int Timeout = 1000;  
    private final int AutoScanTimeout = 300000; //Five minutes
        
    private ArrayList<User> Users;
    
    private OnAutoScanCompleteHandler onAutoScanComplete;
    private OnAutoScanErrorHandler onAutoScanError;
    
    private final FormAutoScan formAutoScan;
    
    private static int StartScanRange = Settings.DefaultStartScanRange;
    private static int EndScanRange = Settings.DefaultEndScanRange;
    
    public RunnableAutoscan (FormAutoScan formAutoScan) {
        onAutoScanComplete = null;
        onAutoScanError = null;
        Users = new ArrayList<User>();
        
        this.formAutoScan = formAutoScan;
    }
    
    public void setOnAutoScanCompleteHandler(OnAutoScanCompleteHandler onAutoScanComplete) {
        this.onAutoScanComplete = onAutoScanComplete;
    }
    
    public void setOnAutoScanErrorHandler(OnAutoScanErrorHandler onAutoScanError) {
        this.onAutoScanError = onAutoScanError;
    }   
    
    @Override
    public void run() {
        
        //long _startTimeRunning   = new Date().getTime();
        int _idTemp         = -1;
        int _port           = Settings.getPort();
        String _ipClient    = "";
        String _myIpAddress = Settings.getLocalIpAddress();
        String[] _partOfIp  = _myIpAddress.split("\\.");
        StartScanRange      = Settings.getStartScanRange();
        EndScanRange        = Settings.getEndScanRange();
        
        if (StartScanRange < 1 || StartScanRange > 254) {
            StartScanRange = Settings.DefaultStartScanRange;
        }
        
        if (EndScanRange < 1 || EndScanRange > 254) {
            EndScanRange = Settings.DefaultEndScanRange;
        }
        
        formAutoScan.setMinimun(StartScanRange);        
        formAutoScan.setMaximun(EndScanRange);

        Users.clear();

        if (_partOfIp.length != 4) {
            if (onAutoScanError != null) 
                onAutoScanError.OnAutoScanError("Invalid Ip Address");
            
            return;
        }

        for (int i = StartScanRange; i <= EndScanRange; i++) {
            
            if (formAutoScan.Stopped())
                break;
                
            
            /* 
            if (new Date().getTime() - _startTimeRunning > AutoScanTimeout) {
                if (onAutoScanError != null) 
                    onAutoScanError.OnAutoScanError("Timeout");

                return;                
            }
            */

            _ipClient = _partOfIp[0] + "." + _partOfIp[1] + "." + _partOfIp[2] + "." + String.valueOf(i);
            formAutoScan.setProgessValue(i);
            if (!_ipClient.equals(_myIpAddress)) {
                try {
                    InetAddress sockAddr = InetAddress.getByName(_ipClient);
                    
                    if (!Settings.isHostReachable(sockAddr, 100)) {
                        continue;
                    }
                    
                    if (Settings.IsInDebugMode()) {
                       System.out.println("Processing Ip: " + _ipClient);
                    }

                    socket = new Socket(sockAddr, _port);

                    bufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    bufferReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    bufferWriter.flush();

                    bufferWriter.write(whois);
                    bufferWriter.flush();

                    long _startTime = new Date().getTime();

                    String _msg = "";

                    while (new Date().getTime() - _startTime < Timeout) {
                        if (bufferReader.ready()) {
                            _msg = (String) bufferReader.readLine() +"\r\n"; ; 
                        }

                        if (_msg.endsWith("{[END]}\r\n")) {
                            _msg = _msg.substring(0, _msg.indexOf("{[END]}"));
                            break;
                        }
                    }

                    if (_msg != null && !_msg.isEmpty()) {
                        //Whoaaa....there is someone....i'm not alone :)
                        try {
                            User u = new Gson().fromJson(_msg, User.class);
                            u .setId(_idTemp);
                            
                            Users.add(u);
                            _idTemp--;
                        }
                        catch (Exception ex) {
                            if (Settings.IsInDebugMode()) {
                               System.out.println("Not a valid user: " + _msg);
                            }
                        }
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
        }
        //For debug
        //System.out.println("Execution Time = " + String.valueOf(new Date().getTime() - _startTimeRunning));
        
        //Ok our work here is done, raise an event to comunicate our results
        if (onAutoScanComplete != null) {
            onAutoScanComplete.OnAutoScanComplete(Users);
        }
    }
}
