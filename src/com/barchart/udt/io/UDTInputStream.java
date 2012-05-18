package com.barchart.udt.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;

public class UDTInputStream extends InputStream {

	//private static final int PACKET_BUFFER_SIZE = 16*1024;
	private static final int PACKET_BUFFER_SIZE = 64;

	private SocketUDT dsock = null;
	
	byte[] ddata = new byte[PACKET_BUFFER_SIZE];
	int packSize = 0;
	int packIdx = 0;

	int value;

	/********************** constructors ********************/
	public UDTInputStream() {}

	

	/************ opening and closing the stream ************/
	public void setUDTConnection (SocketUDT openedSocket)
	{
		dsock = openedSocket;
	}

	
	public void close()throws IOException 
	{
		if (dsock != null)
		{
			dsock.close();
		}
	}
	
	/****** reading, skipping and checking available data ******/
	public int available() throws IOException {
		return packSize - packIdx;
	}

	public int read() throws IOException {
		if (packIdx == packSize) {
			try 
			{
				receive();
			}
			catch (ExceptionUDT eudt)
			{
				//Connection Broken--- no more data
				if (eudt.errorUDT.code == 2001 )
				{
					return -1;
					
				}
				else {
					throw eudt;
				}
			}
		}

		value = ddata[packIdx] & 0xff;
		packIdx++;
		return value;
	}

	public int read(byte[] buff) throws IOException {
		return read(buff, 0, buff.length);
	}

	public int read(byte[] buff, int off, int len) throws IOException {
		if (packIdx == packSize) {
			try 
			{
				receive();
			}
			catch (ExceptionUDT eudt)
			{
				//Connection Broken--- no more data
				if (eudt.errorUDT.code == 2001 )
				{
					return -1;
					
				}
				else {
					throw eudt;
				}
			}
			
		}

		int lenRemaining = len;
		int readSofar = 0 ; 

		while(available() < lenRemaining) {
			System.arraycopy(ddata,
					packIdx,
					buff,
					off + (len - lenRemaining),
					available());
			lenRemaining -= available();
			
			readSofar += available();
			try 
			{
				receive();
			}
			catch (ExceptionUDT eudt)
			{
				//Connection Broken--- no more data
				if (eudt.errorUDT.code == 2001 )
				{
					return readSofar ;
					
				}
				else {
					throw eudt;
				}
			}
			
		}

		System.arraycopy(ddata,
				packIdx,
				buff,
				off + (len - lenRemaining),
				lenRemaining);
		packIdx += lenRemaining;
		return len;
	}
	public long skip(long len) throws IOException {
		if (packIdx == packSize) {
			try 
			{
				receive();
			}
			catch (ExceptionUDT eudt)
			{
				//Connection Broken--- no more data
				if (eudt.errorUDT.code == 2001 )
				{
					return 0;
					
				}
				else {
					throw eudt;
				}
			}
		}

		long lenRemaining = len;
		int skipSofar = 0 ; 

		while(available() < lenRemaining) {
			lenRemaining -= available();
			skipSofar += available();
			try 
			{
				receive();
			}
			catch (ExceptionUDT eudt)
			{
				//Connection Broken--- no more data
				if (eudt.errorUDT.code == 2001 )
				{
					return skipSofar ;
					
				}
				else {
					throw eudt;
				}
			}
		}

		packIdx += (int) lenRemaining;
		return len;
	}

	/****************** receiving more data ******************/
	private void receive() throws IOException {
		int received = -1; 
		try 
		{
			received = dsock.receive(ddata);
			packIdx = 0;
			packSize = received;
		}
		catch (ExceptionUDT eudt)
		{
			//Connection Broken--- no more data
			if (eudt.errorUDT.code == 2001 )
			{
				packIdx = 0;
				packSize = 0;
			}
			
			throw eudt; 
		}
		
	}

	/********* marking and reseting are unsupported ********/
	public void mark(int readlimit) {}

	public void reset() throws IOException {
		throw new IOException("Marks are not supported by UDPInputStream.");
	}

	public boolean markSupported() {
		return false;
	}

}
