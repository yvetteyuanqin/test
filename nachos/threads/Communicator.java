package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
	//one thread has something to say.  
	comLock.acquire();
	//if nobody is listening or if something has already been said, speaker goes to sleep
	while (listening == 0 || message != null) {
	    speaker.sleep();
	}
	//make a new message and wake up someone to listen
	message = new Integer(word);
	listener.wake();
	comLock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */
    public int listen() {
	//there's another thread listening
	comLock.acquire();
	listening++;
	//if there's not a message for the listener to hear, wake up a speaker and then immediately goes to sleep
	while (message == null) {
	    speaker.wake();
	    listener.sleep();
	}
	//this thread hears the message and there is one fewer listener
	int receivedMessage = message.intValue();
	message = null;
	listening--;
	comLock.release();
	return receivedMessage;
    }

    private static class Speaker implements Runnable {
	Speaker(Communicator com, String name) {
	    this.com = com;
	    this.name = name;
	}

	public void run() {
	    //two things to say
	    for (int i = 0; i < 2; i++) {
		com.speak(i);
		System.out.println(name + " says " + i);
	    }
	    System.out.println(name + " is done");
	}

	private Communicator com;
	private String name;
    }

    private static class Listener implements Runnable {
	Listener(Communicator com, String name) {
	    this.com = com;
	    this.name = name;
	}

	public void run() {
	    //two things to hear
	    for (int i = 0; i < 2; i++) {
		int heard = com.listen();
		System.out.println(name + " hears " + heard);
	    }
	    System.out.println(name + " is done");
	}

	private Communicator com;
	private String name;
    }

    public static void selfTest() {
	Communicator com1 = new Communicator();
	
	KThread thread1 = new KThread(new Speaker(com1, "Sherri"));
	KThread thread2 = new KThread(new Listener(com1, "Yasin"));
	KThread thread3 = new KThread(new Speaker(com1, "Karen"));
	thread1.fork();
	thread2.fork();
	thread3.fork();
	//once billy joe is done then the other people get cut off because he is the main thread which is done
	new Listener(com1, "Billy Joe").run();
    }

    private Integer message = null;
    private int listening = 0;
    private Lock comLock = new Lock();
    private Condition2 listener = new Condition2(comLock);
    private Condition2 speaker = new Condition2(comLock);
}
