package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */

/** the implementation is not allowed to include semaphores, so we use locks instead. For the data structures, mainly I referred to the functions in Lock class and do some modifications to fit this environment. The implementation is very much like the implementation of Condition, but there are still several things different: To make the operation atomic, we must disable interrupt from beginning to the end in all functions. And I use waitQueue instead of linkedlist so that in task V the strategy is not like round-robin one. */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

	boolean intStatus = Machine.interrupt().disable();

	conditionLock.release();

	KThread thread = KThread.currentThread();

	waitQueue.waitForAccess(thread);

	KThread.sleep();

	conditionLock.acquire();

	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

	boolean intStatus = Machine.interrupt().disable();

	if ((conditionWaiter = waitQueue.nextThread()) != null)
	    conditionWaiter.ready();

	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

	boolean intStatus = Machine.interrupt().disable();

	while ((conditionWaiter = waitQueue.nextThread()) != null)
	    conditionWaiter.ready();

	Machine.interrupt().restore(intStatus);
    }

    private Lock conditionLock;
    private KThread conditionWaiter = null;
    private ThreadQueue waitQueue =
	ThreadedKernel.scheduler.newThreadQueue(true); /** same as the waitqueue in Lock.java */
}
