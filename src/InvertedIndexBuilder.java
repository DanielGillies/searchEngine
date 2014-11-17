import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InvertedIndexBuilder {

	// Defining Variables
	private static final Charset charset = Charset.forName("UTF-8");
	private final Logger logger = LogManager.getLogger(InvertedIndexBuilder.class);
	private final WorkQueue workers;
	private int pending;
	private final InvertedIndex map;

	public InvertedIndexBuilder(InvertedIndex index, int tFlag) {
		map = index;
		if (tFlag != 0) {
			workers = new WorkQueue(tFlag);
		} else {
			workers = new WorkQueue();
		}
		pending = 0;
	}

	/**
	 * Removes whitespace, all non-word characters, all underscores, and converts to lower case
	 * @param word word to be converted
	 * @return converted word
	 */
	public static String convertWord(String word) {
		if (word != null) {
			word = word.replaceAll("\\W+", "");
			word = word.replaceAll("_", "");
			word = word.trim();
			word = word.toLowerCase();
		}
		return word;
	}


	/**
	 * parses the file and adds the words to local inverted index, and then adds
	 * them all to the class instance
	 * @param file the file being parsed
	 */
	public void parseFile(Path file) {
		// New reader for the file
		InvertedIndex local = new InvertedIndex();
		try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
			String line = null;
			Integer counter = new Integer(0);

			// Add the words to the Inverted Index until it reaches the end of the file
			while ((line = reader.readLine()) != null) {
				// Explode the line into an array where there are spaces
				String[] textArray = line.split("\\s+");
				for (String word: textArray) {
					// Convert the word to normalize it
					word = convertWord(word);
					// Add the word if it still exists after being normalized
					if ((word != null) && !word.equals("")) {
						counter++;
						local.addWord(word, file.toString(), counter);
					}
				}

			}
		}
		catch (IOException e) {
			System.out.println("Could not find the specified file");
		}
		map.addAll(local);
	}

	/**
	 * Go through directories and get text files
	 * @param path the path of the directory or file
	 * @param map the InvertedIndex we would like to build
	 * @throws IOException unable to find input file
	 */
	public void traverse(String path, InvertedIndex map) throws IOException {
		if (Files.isDirectory(Paths.get(path))) {
			// Create a new directory worker to traverse it
			this.workers.execute(new DirectoryWorker(Paths.get(path)));
		} else {
			// Check if file is a text file, if not then ignore
			if (Paths.get(path).getFileName().toString().toLowerCase().endsWith(".txt")) {
				parseFile(Paths.get(path));
			}
		}
	}

	/**
	 * Makes sure all the workers are done before shutting down
	 */
	public synchronized void finish() {
		try {
			while (pending > 0) {
				logger.debug("Waiting until finished");
				this.wait();
			}
		}
		catch (InterruptedException e) {
			logger.debug("Finish interrupted", e);
		}
	}

	/**
	 * Shuts down the builder after all workers are done
	 */
	public void shutdown() {
        logger.debug("Shutting down");
        finish();
        workers.shutdown();
        logger.debug("Shut down");
	}

	private class DirectoryWorker implements Runnable {

		private final Path dir;

		/**
		 * Worker for a directory
		 * @param dir the directory the worker is for
		 */
		public DirectoryWorker(Path dir) {
			logger.debug("Worker created for {}", dir);
			this.dir = dir;
			incrementPending();
		}

		@Override
		public void run() {
			try {
				for (Path path : Files.newDirectoryStream(dir)) {
					// If path is a directory
					if (Files.isDirectory(path)) {
						// Create a new directory worker to traverse it
						workers.execute(new DirectoryWorker(path));
					}
					else {
						// Otherwise it is a file
						if (path.getFileName().toString().toLowerCase().endsWith(".txt")) {
							// New reader for the file
							logger.debug("{} is a valid file", path);

							parseFile(path);
						}
					}
				}


			}
			catch (IOException e) {
				logger.warn("Unable to parse {}", dir);
				logger.catching(Level.DEBUG, e);
			}
			decrementPending();
			logger.debug("Worker finished {}", dir);

		}
	}

	/**
	 * Increases the worker count
	 */
	private synchronized void incrementPending() {
		pending++;
		logger.debug("Pending is now {}", pending);
	}

	/**
	 * Decreases the worker count
	 */
	private synchronized void decrementPending() {
		pending--;
		logger.debug("Pending is now {}", pending);

		if (pending <= 0) {
			this.notifyAll();
		}
	}
}