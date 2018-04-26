import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import tcdIO.*;


public class SenderNode extends Node 
{
	static final int NODE_ID = 1;
	static final int END_USER_1_PORT = 2;
	static final int ROUTER_PORT_1 = 12;
	static final int CONTROLLER_PORT = 100;
	static final String DEFAULT_DST_NODE = "localhost";	
	static int finalDstPort;
	
	Terminal terminal;
	InetSocketAddress routerAddress1;
	
	
	/**
	 * Constructor
	 * 	 
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	SenderNode(Terminal terminal, String dstHost, int dstPort, int srcPort) 
	{
		try 
		{
			this.terminal= terminal;
			routerAddress1= new InetSocketAddress(dstHost, dstPort);
			socket= new DatagramSocket(srcPort);
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
		int flag = getFlag(packet);
		if (flag!=0)
		{
			StringContent content= new StringContent(packet);
			this.notify();
			terminal.println(content.toString());
			terminal.println("\n----------------------------------------------------------------------------------");
	
		}
	}

	/*
	 * This method takes in a string to be sent to the gateway.
	 * The string is put into a byte array along with the sequence
	 * number of the packet and a flag to tell the gateway that the 
	 * packet is coming from the Client.
	 */
	public synchronized void start() throws Exception, SocketTimeoutException 
	{
		boolean finished = false;
		while(!finished)
		{
				DatagramPacket packet= null;
		
				byte[] payload= null;
				byte[] header= null;
				byte[] buffer= null;
				
			
			
				payload=(terminal.readString("String to send: ")).getBytes();
				
				int recr =  terminal.readInt("Which user would you like to send it to?\n(1, 2 or 3):\n");
				switch (recr)
				{
				case 1:
					finalDstPort = 4;
					break;
				case 2:
					finalDstPort = 6;
					break;
				case 3:
					finalDstPort = 8;
					break;
				default:
					terminal.println("This is not a valid user.");
				}
				
					
				
				header= new byte[PacketContent.HEADERLENGTH];
				
				
				buffer= new byte[header.length + payload.length];
				System.arraycopy(header, 0, buffer, 0, header.length);
				System.arraycopy(payload, 0, buffer, header.length, payload.length);
			
				terminal.println("Destination: "+recr);
				
				packet= new DatagramPacket(buffer, buffer.length, routerAddress1);
				setUser1Flag(packet);
				setSource(packet);
				setDestination(packet);
				
				socket.send(packet);
				terminal.println("Packet sent to router 1.\n");
				
				
		}
	}
	
	  
	

	public int getFlag(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		int flag = data[0];
		packet.setData(data);
		return flag;
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
		data[1] = (byte) END_USER_1_PORT;
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
			Terminal terminal= new Terminal("Sender");		
			(new SenderNode(terminal, DEFAULT_DST_NODE, ROUTER_PORT_1, END_USER_1_PORT)).start();
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}