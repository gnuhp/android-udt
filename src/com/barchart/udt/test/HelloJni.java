package com.barchart.udt.test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Bundle;


public class HelloJni extends Activity
{

	private SocketUDT sockdscp = null;
	private String Cameraip,Cameraport,  Session_key,Channel_id;
	private int udtLocalPort;
	private String macAddr ;
	private boolean inProgress;

	private EditText displayView; 
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		displayView = (EditText ) findViewById(R.id.editText1);
		Button connect = (Button ) findViewById(R.id.button1);
		synchronized (this) {
			inProgress = false; 
		}

		connect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				synchronized (this) {
					if (inProgress == true)
					{
						Log.d("mbp", "Some test is run, return ");
						return; 
					}

					inProgress = true; 
				}

				//clear text
				displayView.setText("");


				EditText mac = (EditText) findViewById(R.id.editText2);
				String mac_str =  mac.getText().toString(); 
				if (mac_str.length() != 12 )
				{
					return; //dont do anything
				}

				macAddr = mac_str.toUpperCase();  

				new Thread() {
					public void run()
					{
						//testConnection(displayView);
						
						testInvalidSskey(displayView);
					}
				}.start(); 

			}
		});


		Button connect1 = (Button ) findViewById(R.id.button2);
		connect1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			
				new Thread() {
					public void run()
					{
						
						testConnectToSelf();
					}

					
				}.start(); 
				
			}
		});

	}




	public void onStart()
	{
		super.onStart();
	}
	
	
	private static final int testPort = 2345; 
	
	
	private void testConnectToSelf() 
	{
		SocketUDT sock = null; 
		try {
			sock = new SocketUDT(TypeUDT.STREAM);
		} catch (ExceptionUDT e) {
			e.printStackTrace();
		}
		
		try {
			InetSocketAddress myOwnAddr = new InetSocketAddress(
					InetAddress.getByName("127.0.0.1"), testPort);
			
			
			sock.connect(myOwnAddr); 

			DataInputStream dis =  new DataInputStream(sock.getUDTInputStream());
			
			
			String msg1  = dis.readLine();
			
			
			Log.d("mbp", " READ from self : " + msg1); 
			
			
			sock.close();
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ExceptionUDT e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	public void testInvalidSskey(final TextView display)
	{
		SocketUDT serversock = null; 
		try {
			serversock = new SocketUDT(TypeUDT.STREAM);
		} catch (ExceptionUDT e) {
			e.printStackTrace();
		}
		
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				display.append( "Start testing UDT Invalid Response \n");

			}
		});
		
		SocketUDT clientSock = null;
		OutputStream os = null;
		String message  ="601"; 
		if (serversock != null)
		{
			
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					display.append( "creating udt server sock on port 2345 \n");

				}
			});
			
			InetSocketAddress myOwnAddr = new InetSocketAddress(testPort);
			
			try {
				serversock.bind(myOwnAddr);
				
				serversock.listen(1000);

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						display.append( "accepting connection...  \n");

					}
				});
				
				while (true)
				{
					clientSock = serversock.accept();


					if (clientSock != null)
					{
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								display.append( "Client connected.. \n");

							}
						});
						
						
						break;
					}
					else
					{
						//failed to accept..continue
						
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				
				if (clientSock != null)
				{

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							display.append( "Send status & close\n");

						}
					});

					clientSock.send(message.getBytes());
					
					clientSock.close();
				}
				
				
				
			} catch (ExceptionUDT e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			
			
			
		}
		
		
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				display.append( "Test end.\n");

			}
		});

	}
	
	
	public void testConnection(final TextView display)
	{
		String  msg;
		byte[] data1 = new byte[1024];
		byte[] recv_hdr = new byte[100];
		byte[] recv_buffer = new byte[64*1024];

		super.onStart();
		//---

		Log.d("mbp", "Start testing UDT lib ");
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				display.append( "Start testing UDT lib\n");

			}
		});

		
		
		
		
		
		
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				display.append( "Try going thru relay server\n");
			}
		});

		SocketUDT relaySock ; 
		relaySock = useRelaySever(this.macAddr,"123456789012");
		if (relaySock == null)
		{
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					display.append( "Relay Failed\n");
				}
			});
		}
		else
		{
			//read from relaySock instead
			readSomeDataFromSock(relaySock, display);
		}
		
		
		
		
		


		boolean gotAddress = false; 
		try {
			gotAddress = getCameraInfoFromServer(this.macAddr);
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		if (gotAddress == false)
		{


			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					display.append( "Fail to get camera info from UDT server \n");
				}
			});



			synchronized (this) {
				inProgress = false; 
			}
			return; 
		}


		/* Send the command request for camera ip */
		InetAddress cameraIPAddress = null;



		try {
			cameraIPAddress = InetAddress.getByName(Cameraip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		short[] ipaddress = new short[4] ; 
		ipaddress[0] = (short)(cameraIPAddress.getAddress()[0] & 0xFF);
		ipaddress[1] = (short)(cameraIPAddress.getAddress()[1] & 0xFF);
		ipaddress[2] = (short) (cameraIPAddress.getAddress()[2] & 0xFF);
		ipaddress[3] = (short)(cameraIPAddress.getAddress()[3] & 0xFF);

		final String logmsg = "cameraIPAddress: " + 
				ipaddress[0] + "." +
				ipaddress[1] + "." +
				ipaddress[2]+ "." +
				ipaddress[3] ;
		/* address stored in getAddress() array is 
		 * [0    1   2  3 ]
		 * 192 .168 .5 .107
		 */
		Log.d("mbp", logmsg);

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				display.append( logmsg+"\n");

			}
		});



		int port = Integer.parseInt(Cameraport);

		msg = "action=appletvastream&remote_session=" + Session_key; //+"&channelID="+channelID;

		InetSocketAddress CamInetAddress = new InetSocketAddress(cameraIPAddress.getHostName(), port);
		InetSocketAddress myOwnAddr = new InetSocketAddress(udtLocalPort);

		Log.d("mbp","local port" +udtLocalPort );
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				display.append( "set localPort: " + udtLocalPort +"\n");
			}
		});

		try {
			sockdscp = new SocketUDT(TypeUDT.STREAM);
			sockdscp.bind(myOwnAddr);

			udtLocalPort = sockdscp.getLocalInetPort();
			Log.d("mbp","UDT Connecting to camera ..  from localPort: " + udtLocalPort);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					display.append( "UDT Connecting to camera ..fr localPort: " + udtLocalPort+" .... ");
				}
			});
			sockdscp.connect(CamInetAddress);

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					display.append( "connected\n");
				}
			});


			data1 = msg.getBytes();
			sockdscp.send(data1);

			Log.d("mbp","sending done"); 

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					display.append( "sending done\n");
				}
			});

		} catch (ExceptionUDT e) {
			e.printStackTrace();
			final String exception = e.getMessage();
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					display.append( exception +"\n");
				}
			});
			if (sockdscp!= null)
				try {
					sockdscp.close();
				} catch (ExceptionUDT e1) {
				}

		}


		

		readSomeDataFromSock(sockdscp, display);
		
	
		
		
		
		
		
		Log.d("mbp","UDT test end ");

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				display.append( "UDT test end \n");
			}
		});



		synchronized (this) {
			inProgress = false; 
		}
	}

	
	private void readSomeDataFromSock(SocketUDT openedSock, final TextView display)
	{
		DataInputStream dis ;
		if(!openedSock.isClosed())
		{
			Log.d("mbp","Reading data from camera ---------------------->"); 

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					display.append( "Reading data from camera --------\n");
				}
			});

			dis = new DataInputStream(
					new BufferedInputStream(openedSock.getUDTInputStream()));


			byte [] data = new byte[16*1024]; 
			int rest = -1; 
			int numReadTime = 4 ; 

			while (numReadTime -- > 0)
			{
				try {
					//rest = dis.read(data);
					final String aa = dis.readLine();
					Log.d("mbp","read: " + aa);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							display.append( "read: " + aa + "\n");
						}
					});


				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}


			try {
				openedSock.close();
			} catch (ExceptionUDT e) {
			} 
		}
	}
	
	
	
	private boolean getCameraInfoFromServer(String mac) throws IOException
	{
		/********* start connection with server *************/

		String Server_IP = "107.21.243.7";
		int UDt_Server_Port=7000;


		int myLocalPort=5876;


		DataInputStream  dataIn = null; 
		String Output = null;

		InetAddress UDT_server_IP=null;
		StringBuffer buffer = null;
		SocketUDT socket = null;
		InetSocketAddress  UDT_Server_Addr= null, mylocalAdd = null;

		String  channelID = "123456789012";
		String  macAddress = mac; 
		if (mac == null)
		{
			macAddress = "000DA3121372";//"000EA3070AC9";  //macaddress of camera  
		}

		UDT_server_IP = InetAddress.getByName(Server_IP);


		UDT_Server_Addr = new InetSocketAddress(UDT_server_IP, UDt_Server_Port);

		socket = new SocketUDT(TypeUDT.STREAM);
		socket.connect(UDT_Server_Addr);
		udtLocalPort = socket.getLocalInetPort();

		String message = macAddress + ":" + channelID;
		Log.d("mbp","Sending the camera details to the server:" + message + " from local port:" +udtLocalPort );

		socket.send(message.getBytes());

		//socket.receive(recvData);
		//Output = new String(recvData);		



		/*TEST skip 
		byte[] recvData = new byte[1000];
		dataIn = new DataInputStream(socket.getUDTInputStream());
		dataIn.skipBytes(300);
		dataIn.read(recvData);
		Output = new String(recvData);	
		Log.d("mbp","read bytearr After skip 300: "+ Output);*/


		/*TEST READLINE */
		dataIn = new DataInputStream(socket.getUDTInputStream());
		Output = dataIn.readLine(); 
		Log.d("mbp","readline: Output: "+ Output);


		/*TEST read byte array/
		byte[] recvData = new byte[1000];
		dataIn = new DataInputStream(socket.getUDTInputStream());
		dataIn.read(recvData);
		Output = new String(recvData);	
		Log.d("mbp","read bytearr: Output: "+ Output);
		 */	

		socket.close();


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


	private SocketUDT useRelaySever(String macAddress, String channelID)
	{
		
		InetSocketAddress relay_conn_addr ; 
		SocketUDT relay_socket;
		
		
		relay_conn_addr = new InetSocketAddress("23.21.60.17", 44444);
		if(relay_conn_addr==null)
		{
			Log.d("mbp","Server addr null");
			return null;
		}
		//relay_conn_addr = new InetSocketAddress(InetAddress.getByName("23.21.60.17"),7000);

		try {
			relay_socket = new SocketUDT(TypeUDT.STREAM);

			try{
				relay_socket.connect(relay_conn_addr);
			}catch(Exception  ex){
				Log.d("mbp","Stun server not running or loaded");
				return null;
			}
			String message = macAddress + ":" + channelID;
			relay_socket.send(message.getBytes());
			byte[] recvdata = new  byte[50];
			Log.d("mbp","Port ::"+relay_socket.getLocalInetPort());
			relay_socket.receive(recvdata);

			/*
			 * 					STUN RESPONSE FROM SERVER IN CASE OF CAMERA NOT AVAILABLE
			 */

			if(new String(recvdata).contains("&&&"))
			{
				Log.d("mbp",new String(recvdata));
			}
			/*
			 *                       ####CAMERA CONNECT SUCCESS  WILL BE HERE 
			 */
			else if(new String(recvdata).contains("###"))  
			{
				Log.d("mbp",new String(recvdata));
				return null;
			}
			else if(new String(recvdata).contains("@@@"))
			{
				Log.d("mbp","failure :"+new String(recvdata));
				return null;
			}
		}catch(ExceptionUDT ex){
			ex.printStackTrace(); 
			return null;
		}
		
		
		return relay_socket;
		
	}
}
