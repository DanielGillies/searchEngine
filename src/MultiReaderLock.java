import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * A simple custom lock that allows simultaneously read operations, but
 * disallows simultaneously write and read/write operations.
 *
 * You do not need to implement any form or priority to read or write
 * operations. The first thread that acquires the appropriate lock should be
 * allowed to continue.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 */
public class MultiReaderLock {
	private int numWriters;
	private int numReaders;
	private static Logger logger = LogManager.getLogger(MultiReaderLock.class);


	/**
	 * Initializes a multi-reader (single-writer) lock.
	 */
	public MultiReaderLock() {
		numWriters = 0;
		numReaders = 0;
	}

	public int getNumReaders() {
		return numReaders;
	}

	public int getNumWriters() {
		return numWriters;
	}

	/**
	 * Will wait until there are no active writers in the system, and then will
	 * increase the number of active readers.
	 */
	public synchronized void lockRead() {
		// Wait (give up lock) until no active writers
		while (numWriters > 0) {
			try {
				this.wait();
			}
			catch (InterruptedException e) {
				logger.error("Interruped");
			}
		}
		numReaders++;
		// Use a loop to avoid spurious wakeups
		// use wait() and notifyAll() to avoid busy-wait
		// Increase number of readers to grab lock
	}

	/**
	 * Will decrease the number of active readers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockRead() {
		numReaders--;
		if (numReaders == 0){
			this.notifyAll();
		}
		//Decrease number of readers
		// Wake up threads if necessary using notifyAll()
	}

	/**
	 * Will wait until there are no active readers or writers in the system, and
	 * then will increase the number of active writers.
	 */
	public synchronized void lockWrite() {
		while ((numWriters > 0) || (numReaders > 0)) {
			try {
				this.wait();
			}
			catch (InterruptedException e) {
				logger.error("Interruped");
			}
		}
		numWriters++;
	}

	/**
	 * Will decrease the number of active writers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockWrite() {
		numWriters--;
		this.notifyAll();
	}
}