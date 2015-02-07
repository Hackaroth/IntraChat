/*
* IntraChat 
* 
* Ping ICMP Library
* 
* This file is part of the IntraChat project
* This library is used to sent an ICMP ping to an host using JNI
* The reason of this library is that the method isReachable of the class InetAddress under windows do not work.
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

#include "intrachat_PingICMP.h"
#include <winsock2.h>
#include <iphlpapi.h>
#include <icmpapi.h>
#include <stdio.h>

#pragma comment(lib, "iphlpapi.lib")
#pragma comment(lib, "ws2_32.lib")


JNIEXPORT jlong JNICALL Java_intrachat_PingICMP_isReachable (JNIEnv *env, jobject obj, jstring host, jint timeout) {
    
    HANDLE hIcmpFile;
    unsigned long ipaddr = INADDR_NONE;
    DWORD dwRetVal = 0;
    char SendData[32] = "Data Buffer";
    LPVOID ReplyBuffer = NULL;
    DWORD ReplySize = 0;

    jlong _retval = -1;
	
	const jlong JINVALID_ADDRESS			= -2;
	const jlong JINVALID_HANDLE_VALUE		= -3;
	const jlong JERROR_ALLOCATE_MEMORY		= -4;

    const char *nativeHost = env->GetStringUTFChars(host, false);

    ipaddr = inet_addr(nativeHost);
    if (ipaddr == INADDR_NONE) {
        return JINVALID_ADDRESS;
    }
    
    hIcmpFile = IcmpCreateFile();
    if (hIcmpFile == INVALID_HANDLE_VALUE) {
        return JINVALID_HANDLE_VALUE;
    }    

    ReplySize = sizeof(ICMP_ECHO_REPLY) + sizeof(SendData);
    ReplyBuffer = (VOID*) malloc(ReplySize);
    if (ReplyBuffer == NULL) {
        return JERROR_ALLOCATE_MEMORY;
    }    
    
    
    dwRetVal = IcmpSendEcho(hIcmpFile, ipaddr, SendData, sizeof(SendData), 
        NULL, ReplyBuffer, ReplySize, timeout);
    if (dwRetVal != 0) {
		PICMP_ECHO_REPLY pEchoReply = (PICMP_ECHO_REPLY)ReplyBuffer;

		_retval = pEchoReply->Status;
	}
    else {
		_retval = GetLastError();
	}

	free(ReplyBuffer);
	IcmpCloseHandle(hIcmpFile);
    env->ReleaseStringUTFChars(host, nativeHost);

	return _retval;
}