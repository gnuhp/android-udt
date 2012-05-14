package com.barchart.udt.io;

import java.io.IOException;
import java.io.InputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;

public class UDTInputStream extends InputStream {

	private static final int PACKET_BUFFER_SIZE = 16*1024;

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
			receive();
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
			receive();
		}

		int lenRemaining = len;

		while(available() < lenRemaining) {
			System.arraycopy(ddata,
					packIdx,
					buff,
					off + (len - lenRemaining),
					available());
			lenRemaining -= available();
			receive();
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
			receive();
		}

		long lenRemaining = len;

		while(available() < lenRemaining) {
			lenRemaining -= available();
			receive();
		}

		packIdx += (int) lenRemaining;
		return len;
	}

	/****************** receiving more data ******************/
	private void receive() throws IOException {
		int received = dsock.receive(ddata);
		packIdx = 0;
		packSize = received;
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
