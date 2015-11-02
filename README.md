# Content-Addressable-Network
A P2P hash table based network system.

Introduction
This CAN system is implemented by Java. It can do file insert, file retrieve, node join and displaying the peer information. Node leave is not implemented. I mainly use Java RMI to implement all the functions of the CAN system, and use the socket to assist the routing mechanism.


File description
For implement the bootstrap:
BootStrap.java			Implement the interface that need to call remotely
BootStrapInt.java		The interface of the RMI function 
BootstrapServer.java		Start the RMI service and bind the bootstrap object to network
For implement the CAN system:
CanNode.java			All the remote function of the CAN system in this file
CanNodeInt.java		The interface of the CAN functions
CanNodePeer.java		Start RMI service and implement the join, view, insert and search
InetArea.java			The object for contain area of the node in the zone
InetPoint.java			The object for contain the coordinate and the IP address for find 
RoutingTable.java		The object contain the surrounding peer IP and area information
SysParameter.java		The system parameter of the CAN system
RunNetwork.java		Main function for accept the required command


How to compile and run
All the files need to compile: javac *.java
To start the bootstrap server: java BootstrapServer.java
To start a peer: java RunNetwork.java


Set Bootstrap server IP
I assume the bootstrap server is running on: glados.cs.rit.edu. So make sure the bootstrap server is running on glados. 
If want to run the bootstrap server on other machine, open file SysParameter.java and change the variable: 
public static final String BOOT_IP = “glados.cs.rit.edu”
to the IP address of the machine that you want running the bootstrap server.


Run the peer
The peers can run on the machine with different IP. It accept the command required by the project. To exit the program can use command “exit” or “quit”. To exit the bootstrap server press Ctrl + C on keyboard.


Yi Huang		yxh1126@rit.edu
