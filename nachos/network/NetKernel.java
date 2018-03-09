package nachos.network;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;
import nachos.network.*;

/**
 * A kernel with network support.
 */
public class NetKernel extends UserKernel {
    /**
     * Allocate a new networking kernel.
     */
    public NetKernel() {
		super();
    }

    /**
     * Initialize this kernel.
     */
    public void initialize(String[] args) {
		super.initialize(args);

		postOffice = new PostOffice();
    }

    /**
     * Test the network. Create a server thread that listens for pings on port
     * 1 and sends replies. Then ping one or two hosts. Note that this test
     * assumes that the network is reliable (i.e. that the network's
     * reliability is 1.0).
     */
    public void selfTest() {
	super.selfTest();

	/*
	KThread serverThread = new KThread(new Runnable() {
		public void run() { pingServer(); }
	    });

	serverThread.fork();

	System.out.println("Press any key to start the network test...");
	console.readByte(true);

	int local = Machine.networkLink().getLinkAddress();

	// ping this machine first
	ping(local);

	// if we're 0 or 1, ping the opposite
	if (local <= 1)
	    ping(1-local);
	*/
    }

    private void ping(int dstLink) {
	int srcLink = Machine.networkLink().getLinkAddress();
	
	System.out.println("PING " + dstLink + " from " + srcLink);

	long startTime = Machine.timer().getTime();
	
	NetMessage ping;

	try {
	    ping = new NetMessage(dstLink, 1,
				   Machine.networkLink().getLinkAddress(), 0,
				   3, 127986, "hello world".getBytes());
	}
	catch (MalformedPacketException e) {
	    Lib.assertNotReached();
	    return;
	}

	postOffice.send(ping);
	NetMessage ack = postOffice.receive(0);
	System.out.println(Lib.bytesToString(ack.contents, 0, ack.contents.length));
	
	long endTime = Machine.timer().getTime();

	System.out.println("time=" + (endTime-startTime) + " ticks");	
    }

    private void pingServer() {
	while (true) {
	    NetMessage ping = postOffice.receive(1);

	    NetMessage ack;
	    System.out.println(ping.status);
	    try {
		ack = new NetMessage(ping.packet.srcLink, ping.srcPort,
				      ping.packet.dstLink, ping.dstPort,
				      ping.status, ping.seqNum,
				      ping.contents);
	    }
	    catch (MalformedPacketException e) {
		// should never happen...
		continue;
	    }

	    postOffice.send(ack);
	}	
    }
    
    /**
     * Start running user programs.
     */
    public void run() {
	super.run();
    }
    
    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
	super.terminate();
    }

    //modified postOffice so it can be accessed elsewhere
    public static PostOffice postOffice;

    // dummy variables to make javac smarter
    private static NetProcess dummy1 = null;
}
