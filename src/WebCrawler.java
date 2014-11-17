import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class WebCrawler {


	private final Logger logger = LogManager.getLogger(InvertedIndexBuilder.class);
	private final WorkQueue workers;
	private int pending;
	private final InvertedIndex map;
	private final MultiReaderLock lock;
	private final HashSet<String> urls;

	public WebCrawler(InvertedIndex index, int tFlag) {
		map = index;
		lock = new MultiReaderLock();
		if (tFlag != 0) {
			workers = new WorkQueue(tFlag);
		} else {
			workers = new WorkQueue();
		}
		pending = 0;
		urls = new HashSet<>();
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
	 * add the words on the page to the map
	 * @param url the url being parsed
	 */
	private void parseWeb(String url, String base) {
		ArrayList<String> words = new ArrayList<>();
		ArrayList<String> localURL = new ArrayList<>();

		try {
			URL absolute;
			URL baseURL = new URL(base);
			absolute = new URL(baseURL, url);
			logger.debug("ABSOLUTE: {}", absolute.toString());
			HTMLFetcher fetcher = new HTMLFetcher(absolute.toString());
			String html = fetcher.fetch();
			logger.debug("Fetched html for {}", url);
			localURL = HTMLLinkParser.listLinks(html);
			logger.debug("Got links for {}", url);
			lock.lockWrite();
			for (String link : localURL) {
				URL temp = new URL(baseURL, link);
				if (!this.urls.contains(temp.getPath()) && (temp.getPath() != null)) {
					if (this.urls.size() < 50) {
						this.urls.add(temp.getPath());
						this.workers.execute(new CrawlWorker(temp.getPath(), base));
					}
				}
			lock.unlockWrite();
			html = HTMLCleaner.cleanHTML(html);
			words = HTMLCleaner.parseWords(html);
			addWords(words, absolute.toString());

			}
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println("URL is not valid");
		}
	}

	/**
	 * crawl the web
	 * @param url the seed URL
	 */
	public void crawlWeb(String url) {

		lock.lockWrite();
		this.urls.add(url);
		lock.unlockWrite();
		this.workers.execute(new CrawlWorker(url, url));

	}

	/**
	 * add the words from the arraylist to the index
	 * @param words the words to be added
	 * @param url the url they are from
	 */
	public void addWords(ArrayList<String> words, String url) {
		InvertedIndex local = new InvertedIndex();
		for (int i = 0; i < words.size(); i++) {
			local.addWord(words.get(i), url, i+1);
			logger.debug("Added {} to local", words.get(i));
		}
		map.addAll(local);
	}

	private class CrawlWorker implements Runnable {

		private final String url;
		private final String base;

		public CrawlWorker(String url, String base) {
			logger.debug("CrawlWorker created for {}", url);
			this.url = url;
			this.base = base;
			incrementPending();
		}

		@Override
		public void run() {
			parseWeb(url, base);
			decrementPending();
			logger.debug("CrawlWorker finished {}", url);
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

	/**
	 * Shuts down the builder after all workers are done
	 */
	public void shutdown() {
        logger.debug("Shutting down");
        finish();
        workers.shutdown();
        logger.debug("Shut down");
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


}