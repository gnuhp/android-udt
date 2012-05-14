/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.barchart.udt.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

import android.app.Activity;
import android.util.Log;
import android.os.Bundle;


public class HelloJni extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main);
	}

	private SocketUDT sockdscp = null;
	private String Cameraip,Cameraport,  Session_key,Channel_id;
	
	private boolean getCameraInfoFromServer() throws IOException
	{
		/********* start connection with server *************/

		String Server_IP = "107.21.243.7";
		int UDt_Server_Port=7000;

		InetAddress UDT_server_IP=null;
		StringBuffer buffer = null;
		SocketUDT socket = null;
		InetSocketAddress UDT_Server_Addr = null;

		String  channelID = "123456789012";                      
		String  macAddress = "000EA3070A55";//"000EA3070AC9";                      //macaddress of camera  
		UDT_server_IP = InetAddress.getByName(Server_IP);

		socket = new SocketUDT(TypeUDT.STREAM);

		UDT_Server_Addr = new InetSocketAddress(UDT_server_IP, UDt_Server_Port);

		socket.connect(UDT_Server_Addr);

		String message = macAddress + ":" + channelID;
		Log.d("mbp","Sending the camera details to the server:" + message);

		socket.send(message.getBytes());

		byte[] recvData = new byte[1000];

		socket.receive(recvData);

		socket.close();


		String Output = null;
		Output = new String(recvData);

		String []str = null, temp;
		int Camera_status;
		str = Output.split("<br>");
		try{
			temp = str[0].trim().split("=");
			Cameraip = temp[1].trim();
			Camera_status = Cameraip.compareToIgnoreCase("null");
			if (Camera_status == 0) {
				Log.d("mbp","Camera not Ready");                         //IP is NULL
			}

			temp = null;
			temp = str[1].trim().split("=");
			Cameraport = temp[1].trim();
			Camera_status = Cameraport.compareToIgnoreCase("0");
			if (Camera_status == 0) {
				Log.d("mbp","Stun not Ready");                            //Port is 0 
			}
			temp = null;
			temp = str[2].trim().split("=");
			Session_key = temp[1].trim();

			temp = null;
			temp = str[3].trim().split("=");
			Channel_id = temp[1].trim();


			Cameraip = Cameraip.substring(1);

		}
		catch(ArrayIndexOutOfBoundsException ex)
		{
			Log.d("mbp","Output :"+Output);
			return false;
		}
		/********* end of connection with server *************/



		return true; 
	}
	
	
	public void onStart()
	{
		String  msg;
		byte[] data1 = new byte[1024];
		byte[] recv_hdr = new byte[100];
		byte[] recv_buffer = new byte[64*1024];
		
		super.onStart();
		//---
		
		Log.d("mbp", "Start testing UDT lib ");
		

		/* hardcode data to test camera view  
		Cameraip = "192.168.5.107";
		Cameraport ="41327";
		Session_key ="D83B666E1ED069B8B2A641E073BF120B44B7412690ED76E33B83A81C904D2573";
		Log.d("mbp","UDT tryConnect ");
		*/
		
		try {
			getCameraInfoFromServer();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
 
		/* Send the command request for camera ip */
		InetAddress cameraIPAddress = null;
		
		
		try {
			cameraIPAddress = InetAddress.getByName(Cameraip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		/* address stored in getAddress() array is 
		 * [0    1   2  3 ]
		 * 192 .168 .5 .107
		*/
		Log.d("mbp", "cameraIPAddress: " +cameraIPAddress.getAddress()[0] + "." +
				 cameraIPAddress.getAddress()[1] + "." +
				 cameraIPAddress.getAddress()[2] + "." +
				 cameraIPAddress.getAddress()[3] 
		);
		

		int port = Integer.parseInt(Cameraport);

		msg = "action=appletvastream&remote_session=" + Session_key; //+"&channelID="+channelID;

		InetSocketAddress CamInetAddress = new InetSocketAddress(cameraIPAddress.getHostName(), port);
		try {
			sockdscp = new SocketUDT(TypeUDT.STREAM);
			Log.d("mbp","UDT 01 ");
			sockdscp.setReceiveBufferSize(90000);
			Log.d("mbp","UDT 02 ");
			sockdscp.connect(CamInetAddress);
			Log.d("mbp","UDT 03 ");
			data1 = msg.getBytes();
			sockdscp.send(data1);
			
		} catch (ExceptionUDT e) {
			e.printStackTrace();
		}
		
		new Thread()
		{
			public void run()
			{
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				} 
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						try {
							sockdscp.close();
						} catch (ExceptionUDT e) {
							e.printStackTrace();
						}
					}
				});
			}
		}.start();
	}


}
