import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import tcdIO.Terminal;
import tcdIO.*;

public class Router extends Node
{
	
	static final int CONTROLLER_PORT = 100;	
	static DatagramPacket toBeSent = null;
	static int routerNumber;
	static final int numberOfRouters=5;
	HashMap<Integer, Integer> nextHopInfo;//structure - <destinationPort, nextHopForThatDestination>
	
	public Terminal terminal;
	static final String DEFAULT_DST_NODE = "localhost";	
	
	InetSocketAddress controllerAddress = new InetSocketAddress(DEFAULT_DST_NODE, CONTROLLER_PORT);

	Router(Terminal terminal, String dstHost)
	{
		try {
			this.terminal= terminal;
			int j = 12;
			for (int i =1;i<=numberOfRouters;i++)
			{
				terminal.println("Router "+ i +" port: "+j);
				j+=2;
			}
			int port = terminal.readInt("Enter a port to determine\nwhich router this is: ");
			switch (port)
			{
			case 12: 
				terminal.println("This is router 1.\n");
				routerNumber = 1;
				break;
			case 14: 
				terminal.println("This is router 2.\n");
				routerNumber = 2; 
				break;
			case 16:
				terminal.println("This is router 3.\n");
				routerNumber = 3;
				break;
			case 18:
				terminal.println("This is router 4.\n");
				routerNumber = 4;
				break;
			case 20:
				terminal.println("This is router 5.\n");
				routerNumber = 5;
				break;
			default:
				terminal.println("There is no router operating on this port.");
				break;
			}
			nextHopInfo = new HashMap<Integer,Integer>();
			socket= new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}
	/*
	 * onReceipt method handles the receiveing of packets. It first determines where the packet came from
	 * If a received packet is from the controller the packet contains information on next-hop ports.
	 *  so the router updates its next hop table.
	 *  If a received packet is from a user or router, it checks the next hop table to see if it has the required infomration.
	 *  If it has the required information it sends on the packet. Otherwise it requests the information from the controller beforee sending .
	 */
	public void onReceipt(DatagramPacket packet)  
	{
		try {
			
			int flag = getFlag(packet);
			int nextHopPort;
			InetSocketAddress nextHopAddr;
			if (flag == 1) // packet is from end user
			{
				System.out.print("\nPACKET RECEIVED FROM ROUTER/USER\n");

				terminal.println("Received packet.\nChecking destination and checking if\nnext hop information is known");
				int dst = getDestination(packet);
				int src = getSource(packet);
				boolean hasInfo = nextHopInfo.containsKey(dst);
				if(hasInfo) //router already knows the nextHop for this destination
				{
					nextHopPort = nextHopInfo.get(dst);
					terminal.println("\nNext hop is "+ nextHopPort + ".\nForwarding packet now");
					terminal.println("--------------------------------------------------");

					nextHopAddr = new InetSocketAddress(DEFAULT_DST_NODE, nextHopPort);
					packet.setSocketAddress(nextHopAddr);
					socket.send(packet);
				}
				else
				{
					terminal.println("The port for the next hop is unknown.\nA request will now be sent to the controller.");
					terminal.println("--------------------------------------------------");

					toBeSent = packet;
					DatagramPacket request = buildRequestPacket(src, dst);	
					socket.send(request);
				}
			}
			
			else if (flag == 0) // packet is from controller
			{
				int hopPort = extractNextHopPort(packet);
				terminal.println("Information received from controller.\nUpdating Next-Hop table and\nsending any unsent packets.");
				terminal.println("--------------------------------------------------");
				updateInfo(packet);
				if (toBeSent!=null)
					sendUnsent(toBeSent, hopPort);
			}
		}catch(IOException e) {e.printStackTrace();}
		
	}
	/*
	 * This method builds a datagrampacket with the required info to be sent to the controller in request of infomration.
	 */
	public DatagramPacket buildRequestPacket(int src, int dst)
	{
		byte[] buffer = new byte[PacketContent.HEADERLENGTH];
		
		buffer[0]=(byte)2; // 2 is routerFlag
		buffer[1]=(byte)src;
		buffer[2]=(byte)dst;
		
		DatagramPacket request = new DatagramPacket(buffer,buffer.length, controllerAddress);
		return request;
	}
	/*
	 * Updates next-hop table when a packet is received from controller
	 */
	public void updateInfo(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		int dst = (int)data[2];
		int hop = (int)data[3];
		nextHopInfo.put(dst, hop);
	}
	/*
	 * Sends an unsent packet after the router has found out where to send it.
	 */
	public void sendUnsent(DatagramPacket packet, int port)
	{
		try {
			InetSocketAddress address = new InetSocketAddress (DEFAULT_DST_NODE, port);
			packet.setSocketAddress(address);
			socket.send(packet);
		}catch(java.lang.Exception e) {e.printStackTrace();}
		
	}
	
	/*
	 * returns the next-hop port for a certain destination
	 */
	public int extractNextHopPort(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		int nextHop = (int)data[3];
		return nextHop;
	}
	
	/*
	 * returns the flag to determine where a packet came from
	 */
	public int getFlag(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		int flag = data[0];
		return flag;
	}
	
	/*
	 * returns the destination a given packet is trying to arrive at
	 */
	public int getDestination(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		int destination = (int)data[2];
		return destination;
	}
	/*
	 * returns where a given packet originated from
	 */
	public int getSource(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		int source = (int)data[1];
		return source;
	}
	
	public synchronized void start() throws Exception
	{
		terminal.println("Waiting for contact...\n");
		this.wait();
	}
	
	public static void main(String[] args) {
		try 
		{					
			Terminal terminal= new Terminal("Router");		
			(new Router(terminal, DEFAULT_DST_NODE)).start();
		} catch(java.lang.Exception e) {e.printStackTrace();}}
	
}