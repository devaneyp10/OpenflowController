
import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import tcdIO.Terminal;
import tcdIO.*;

public class Controller extends Node
{
	InetSocketAddress routerAddress1;
	InetSocketAddress routerAddress2;
	InetSocketAddress routerAddress3;
	InetSocketAddress routerAddress4;
	InetSocketAddress routerAddress5;
	InetSocketAddress routerAddress6;
	
	HashMap<Integer, HashMap<Integer, Integer>> routes;
	
	static final int ROUTE_1 = 1; //route ID for - end user 1 to end user 2
	static final int ROUTE_2 = 2; //route ID for - end user 2 to end user 1
	static final int ROUTE_3 = 3; //route ID for - end user 1 to end user 3
	static final int ROUTE_4 = 4; //route ID for - end user 3 to end user 1
	static final int ROUTE_5 = 5; //route ID for - end user 2 to end user 3
	static final int ROUTE_6 = 6; //route ID for - end user 3 to end user 1
	
	static final int CONTROLLER_PORT = 100;
	static final int CONTROLLER_FLAG = 0;
	
	static final int END_USER_1_PORT = 2;
	static final int END_USER_2_PORT = 4;
	static final int END_USER_3_PORT = 6;
	static final int END_USER_4_PORT = 8;
	
	
	static final int ROUTER_PORT_1 = 12;
	static final int ROUTER_PORT_2 = 14;
	static final int ROUTER_PORT_3 = 16;
	static final int ROUTER_PORT_4 = 18;
	static final int ROUTER_PORT_5 = 20;

	public int clientNumber;
	public Terminal terminal;
	static final String DEFAULT_DST_NODE = "localhost";	

	Controller(Terminal terminal, String dstHost, int srcPort)
	{
		try {
			
			routerAddress1 = new InetSocketAddress(dstHost,ROUTER_PORT_1);
			routerAddress2 = new InetSocketAddress(dstHost,ROUTER_PORT_2);
			routerAddress3 = new InetSocketAddress(dstHost,ROUTER_PORT_3);
			routerAddress4 = new InetSocketAddress(dstHost,ROUTER_PORT_4);
			routerAddress5 = new InetSocketAddress(dstHost,ROUTER_PORT_5);
			
			this.terminal= terminal;
			socket= new DatagramSocket(CONTROLLER_PORT);
			buildRoutingTables();
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	public void buildRoutingTables()
	{
		routes = new HashMap<Integer, HashMap<Integer, Integer>>();
		
		//build nextHop table for route 1
		HashMap<Integer, Integer> route1 = new HashMap<Integer, Integer>();
		route1.put(ROUTER_PORT_1, ROUTER_PORT_2);
		route1.put(ROUTER_PORT_2, ROUTER_PORT_3);
		route1.put(ROUTER_PORT_3, END_USER_2_PORT);
		
		routes.put(ROUTE_1, route1);
		
		terminal.println("Routing map built for route: "+ ROUTE_1);
		
		//build nextHop table for route 2
		HashMap<Integer, Integer> route2 = new HashMap<Integer, Integer>();
		route2.put(ROUTER_PORT_1, ROUTER_PORT_4);
		route2.put(ROUTER_PORT_4, END_USER_3_PORT);
		
		routes.put(ROUTE_2, route2);
		
		terminal.println("Routing map built for route: "+ ROUTE_2);
		
		//build nextHop table for route 3
		HashMap<Integer, Integer> route3 = new HashMap<Integer, Integer>();
		route3.put(ROUTER_PORT_1, ROUTER_PORT_5);
		route3.put(ROUTER_PORT_5, END_USER_4_PORT);
		
		routes.put(ROUTE_3, route3);
		
		terminal.println("Routing map built for route: "+ ROUTE_3);
		terminal.println("All routing maps complete.");
		terminal.println("--------------------------------------------------");
		
		
		
	}
	
	
	public void onReceipt(DatagramPacket packet)  
	{
		try
		{
			terminal.println("Request received.");
	        int route = findRoute(packet);
	        terminal.println("Route: "+route);
	        
			if (route!=0)
			{
				int dst = getDestination(packet);
				InetSocketAddress recAddress;
				
				HashMap<Integer, Integer> currentRoute = routes.get(route);
				
				for (Entry<Integer, Integer> entry : currentRoute.entrySet()) 
				{
				    int router = entry.getKey();
				    int nextHop = entry.getValue();
				
				    recAddress = new InetSocketAddress(DEFAULT_DST_NODE, router);
				    
					byte[] buffer= new byte[PacketContent.HEADERLENGTH];
					buffer[0] = (byte)CONTROLLER_FLAG;
					buffer[1] = (byte)route;
					buffer[2] = (byte)dst;
					buffer[3] = (byte)nextHop;
					
					DatagramPacket nextHopPacket = new DatagramPacket(buffer,buffer.length, recAddress);
					socket.send(nextHopPacket);
					terminal.println("Next-Hop information sent to : "+router);
				}
				terminal.println("--------------------------------------------------");

			}
			else
				terminal.println("This is not a valid route.");
		}catch(IOException e) {e.printStackTrace();}
	}
	
	
	public int getRouterNumber(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		int number = data[0];
		packet.setData(data);
		return number;
	}
	
	public int findRoute(DatagramPacket packet)
	{
		int source = getSource(packet);
		int destination = getDestination(packet);
		
		if(source == END_USER_1_PORT && destination == END_USER_2_PORT)
			return ROUTE_1;
		else if(source == END_USER_1_PORT && destination == END_USER_3_PORT)
			return ROUTE_2;
		else if(source == END_USER_1_PORT && destination == END_USER_4_PORT)
			return ROUTE_3;
		else 
			return 0;
	}
	
	public int getSource(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		int source = data[1];
		packet.setData(data);
		return source;
	}
	
	public int getDestination(DatagramPacket packet)
	{
		byte[] data = packet.getData();
		int destination = data[2];
		packet.setData(data);
		return destination;
	}
	public synchronized void start() throws Exception
	{
		
		terminal.println("Waiting for contact...\n");
		this.wait();
	}
	
	public static void main(String[] args) {
		try 
		{					
			Terminal terminal= new Terminal("Controller");		
			(new Controller(terminal, DEFAULT_DST_NODE, CONTROLLER_PORT)).start();
		} catch(java.lang.Exception e) {e.printStackTrace();}}
	
}
