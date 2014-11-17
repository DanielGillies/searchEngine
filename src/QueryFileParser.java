import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class QueryFileParser {

	private final LinkedHashMap<String, ArrayList<SearchResult>> map;
	private static final Charset charset = Charset.forName("UTF-8");
	private final Logger logger = LogManager.getLogger(QueryFileParser.class);
	private int pending;
	private final WorkQueue workers;
	private final MultiReaderLock lock;

	public QueryFileParser(int tFlag) {
		map = new LinkedHashMap<>();
		pending = 0;
		if (tFlag != 0) {
			workers = new WorkQueue(tFlag);
		} else {
			workers = new WorkQueue();
		}
		lock = new MultiReaderLock();
	}

	/**
	 * Writes the search result to the output
	 * @param map the result map we are writing out
	 * @param output the output file
	 */
	public void writeSearchResult(String output) {
		lock.lockRead();
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(output), charset)) {
			for (String searchWord : map.keySet()) {

		    	writer.write(searchWord);
		    	writer.newLine();

		    	for (SearchResult result : map.get(searchWord))
			   	writer.write(result.toString());
			    writer.newLine();
			}

		    writer.newLine();
		} catch (IOException ex) {
			System.out.println("Writer file not found");
		}
		lock.unlockRead();
	}

	/**
	 * Parses the query file, does the search and stores the results in the result map
	 * @param path the path of the query file
	 * @param output the file we are writing to
	 * @param index the inverted index we are writing to
	 * @return the result set
	 */
	public void parseQueryFile(String path, String output, InvertedIndex index){
		Path file = Paths.get(path);
		String line;

		try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
			line = reader.readLine();

			while (line != null) {
				lock.lockWrite();
				map.put(line, null);
				lock.unlockWrite();
				this.workers.execute(new SearchWorker(line, index));
				line = reader.readLine();
			}
		}
		catch (IOException e) {
			System.out.println(path + " not found\n");
		}
	}

	/**
	 * Makes sure all of the workers are done
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
	 * shuts down the parser if all of the workers are done
	 */
	public void shutdown() {
        logger.debug("Shutting down");
        finish();
        workers.shutdown();
        logger.debug("Shut down");
	}

	private class SearchWorker implements Runnable {

		private final String line;
		private final InvertedIndex index;
		private ArrayList<SearchResult> result;

		/**
		 * Constructor for a new Search Thread
		 * @param line the line the worker is for
		 * @param index the index being read
		 */
		public SearchWorker(String line, InvertedIndex index) {
			logger.debug("Searcher created for {}", line);
			this.line = line;
			this.index = index;
			incrementPending();
		}

		@Override
		public void run() {
			String[] queryWords;
			queryWords = line.split("\\s+");

			result = index.search(queryWords);
			logger.debug("Stored result for {}", line);
			lock.lockWrite();
			map.put(line, result);
			lock.unlockWrite();

			decrementPending();
			logger.debug("Searcher finished {}", line);

		}

	}
	/**
	 * increments worker count
	 */
	private synchronized void incrementPending() {
		pending++;
		logger.debug("Pending is now {}", pending);
	}

	/**
	 * decrements worker count
	 */
	private synchronized void decrementPending() {
		pending--;
		logger.debug("Pending is now {}", pending);

		if (pending <= 0) {
			this.notifyAll();
		}
	}


}