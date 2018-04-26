import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import tcdIO.*;


public class Receiver extends Node 
{
	static final int NODE_ID = 1;
	static final int END_USER_3_PORT = 6;
	static final int ROUTER_PORT_3 = 16;
	static final int CONTROLLER_PORT = 100;
	static final String DEFAULT_DST_NODE = "localhost";	
	static int finalDstPort;
	static final int numberOfReceivers=3;
	static int receiverNumber;
	Terminal terminal;
	
	
	/**
	 * Constructor
	 * 	 
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Receiver(Terminal terminal, String dstHost) 
	{
		try 
		{
			this.terminal= terminal;
			int j = 4;
			for (int i =1;i<=numberOfReceivers;i++)
			{
				terminal.println("Receiver "+ i +" port: "+j);
				j+=2;
			}
			int port = terminal.readInt("Enter a port to determine\nwhich reciever this is: ");
			switch (port)
			{
			case 4: 
				terminal.println("This is receiver 1.\n");
				receiverNumber = 1;
				break;
			case 6: 
				terminal.println("This is receiver 2.\n");
				receiverNumber = 2; 
				break;
			case 8:
				terminal.println("This is receiver 3.\n");
				receiverNumber = 3;
				break;
			default:
				terminal.println("There is no receiver operating on this port.");
				break;
			}
			this.terminal= terminal;
			socket= new DatagramSocket(port);
			listener.go();

		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	/*
	 * This method receives a packet from the gateway.
	 * It checks for an error before printing the acknowledgement.
	 * If an error has occured in the sequence numbers at the Server
	 * node the client is notified and prints an error message.
	 */
	public synchronized void onReceipt(DatagramPacket packet) 
	{
		StringContent content= new StringContent(packet);
		this.notify();
		terminal.println("Packet received:");
		terminal.println(content.toString());
		terminal.println("----------------------------------------------------------------------------------");
	}

	/*
	 * This method takes in a string to be sent to the gateway.
	 * The string is put into a byte array along with the sequence
	 * number of the packet and a flag to tell the gateway that the 
	 * packet is coming from the Client.
	 */
	public synchronized void start() throws Exception, SocketTimeoutException 
	{
		terminal.println("Waiting to receive...");
		this.wait();
	}
	  
	public void setUser1Flag(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		data[0]=(byte)1;
		packet.setData(data);
	}
	
	public void setSource(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		data[1] = (byte) END_USER_3_PORT;
		packet.setData(data);
	}
	
	public void setDestination(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		data[2] = (byte)finalDstPort;
		packet.setData(data);
	}
	
	public void setID(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		data[3] = (byte)NODE_ID;
		packet.setData(data);
	}
	
	public static void main(String[] args) {
		try 
		{					
			Terminal terminal= new Terminal("Receiver");		
			(new Receiver(terminal, DEFAULT_DST_NODE)).start();
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}