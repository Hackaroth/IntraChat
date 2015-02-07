/*
* IntraChat 
* 
* Class Sounds
* 
* This file is part of the IntraChat project
* This class simply play a sound.
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

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class Sounds {
    
    private Sounds() {}
    
    public static synchronized void play(final String url) {
    new Thread(new Runnable() {
      public void run() {
        try {
            
            BufferedInputStream buffer = new BufferedInputStream(getClass().getResourceAsStream("resources/sounds/" + url));
            
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(buffer);
            AudioFormat format = inputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            Clip clip = (Clip)AudioSystem.getLine(info);
            
            clip.open(inputStream);
            clip.start(); 
        } catch (Exception ex) {
            if (Settings.IsInDebugMode()) 
                Logger.getLogger(Sounds.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }).start();
  }    
}
