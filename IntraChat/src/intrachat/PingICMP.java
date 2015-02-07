/*
* IntraChat 
* 
* Class PIng ICMP
* 
* This file is part of the IntraChat project
* This class send native ICMP echo request to an host. 
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

public class PingICMP {
        public final long JERROR_UNKNOWN		= -1;
	public final long JINVALID_HANDLE_VALUE		= -3;
	public final long JERROR_ALLOCATE_MEMORY	= -4;        

        public final long IP_SUCCESS 			= 0;
        public final long IP_BUF_TOO_SMALL 		= 11001;
        public final long IP_DEST_NET_UNREACHABLE 	= 11002;
        public final long IP_DEST_HOST_UNREACHABLE 	= 11003;
        public final long IP_DEST_PROT_UNREACHABLE 	= 11004;
        public final long IP_DEST_PORT_UNREACHABLE 	= 11005;
        public final long IP_NO_RESOURCES 		= 11006;
        public final long IP_BAD_OPTION  		= 11007;
        public final long IP_HW_ERROR                   = 11008;
        public final long IP_PACKET_TOO_BIG 		= 11009;
        public final long IP_REQ_TIMED_OUT 		= 11010;
        public final long IP_BAD_REQ 			= 11011;
        public final long IP_BAD_ROUTE                  = 11012;
        public final long IP_TTL_EXPIRED_TRANSIT 	= 11013;
        public final long IP_TTL_EXPIRED_REASSEM 	= 11014;
        public final long IP_PARAM_PROBLEM 		= 11015;
        public final long IP_SOURCE_QUENCH 		= 11016;
        public final long IP_OPTION_TOO_BIG 		= 11017;
        public final long IP_BAD_DESTINATION 		= 11018;
        public final long IP_GENERAL_FAILURE 		= 11050;    
    
	static {
                String _model = System.getProperty("sun.arch.data.model");
                if (_model.equals("64"))
                    System.loadLibrary("PingICMPLibrary64");
                else
                    System.loadLibrary("PingICMPLibrary");
	}
	
	public native long isReachable(String host, int timeout);
}
