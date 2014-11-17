import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;


public class InvertedIndex {

	// Defining the map
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> map;

	// Defining Charset
	private static final Charset charset = Charset.forName("UTF-8");
	private final MultiReaderLock lock;

	/**
	 * Constructor
	 */
	public InvertedIndex() {
		map = new TreeMap<>();
		lock = new MultiReaderLock();
	}

	/**
	 *
	 * Write the map to the target text file
	 * @param output the text file to be written to
	 */
	public void writeOutput(String output) {
		lock.lockRead();
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(output), charset)) {
		    // Grab the array of words from the map
		    for (String word : map.keySet() ) {
		    	writer.write(word);
		    	writer.newLine();
		    	// Grab the array of paths from the map
		    	for (String path : map.get(word).keySet()) {
			    	writer.write("\"" + path + "\"");
			    	// Grab the array of positions from the map
		    		for (Integer position : map.get(word).get(path)) {
				    	writer.write(", " + position.toString());
		    		}
		    	writer.newLine();
		    	}
		    writer.newLine();
		    }
		} catch (IOException ex) {
			System.out.println("Writer file not found");
		}
		lock.unlockRead();
	}

	/**
	 * adds a word to the Inverted Index
	 * @param word the word to be added
	 * @param path the text file that the word is from
	 * @param counter the word number within the text file
	 * @return true if it completes without any problems.
	 */
	public boolean addWord(String word, String path, Integer counter) {
		if (word != null) {
			lock.lockWrite();
			// If the word exists in the map
			if (map.containsKey(word)) {
				// If the word exists in the current text file
				if (map.get(word).containsKey(path)) {
					// Add the word position to the set
					map.get(word).get(path).add(counter);
				} else {
					// Add the text file to the map for the current word and add the position
					TreeSet<Integer> set = new TreeSet<Integer>();
					set.add(counter);
					map.get(word).put(path, set);
				}
			} else {
				TreeMap<String, TreeSet<Integer>> innerMap = new TreeMap<String, TreeSet<Integer>>();
				TreeSet<Integer> set = new TreeSet<Integer>();
				set.add(counter);
				innerMap.put(path, set);
				map.put(word, innerMap);
			}
			lock.unlockWrite();
		}
		return true;

	}

	/**
	 * Search the Inverted Index for the query words
	 * @param queryWords the array of words being searched for
	 * @return a list of the search results
	 */
	public ArrayList<SearchResult> search(String[] queryWords) {
		int position;
		int frequency;
		// Initialize new ArrayList for search results
		ArrayList<SearchResult> result = new ArrayList<>();
		HashMap<String, SearchResult> resultMap = new HashMap<>();

		lock.lockRead();
		// Loop through the queries
		for (String searchWord : queryWords) {
			// Loop through the words
			for (String word : this.map.tailMap(searchWord).keySet()) {
				if (!word.startsWith(searchWord))
					break;
				// Found a match for the word
				if (word.startsWith(searchWord)) {
					// Loop through the paths for that word
					for (String path : this.map.get(word).keySet()) {
						if (resultMap.containsKey(path)) {
							SearchResult temp = resultMap.get(path);
							position = this.map.get(word).get(path).first();
							frequency = this.map.get(word).get(path).size();
							temp.addFrequency(frequency);
							temp.setPosition(position);
						}
						else {
							SearchResult temp = new SearchResult(path);
							position = this.map.get(word).get(path).first();
							frequency = this.map.get(word).get(path).size();
							// Updates frequency and position in the temporary search result
							temp.addFrequency(frequency);
							temp.setPosition(position);
							resultMap.put(path, temp);
						}
					}
				}
			}
		}
		lock.unlockRead();
		result.addAll(resultMap.values());
		Collections.sort(result);
		return result;
	}


	/**
	 * Adds one inverted index to another
	 * @param other the inverted index to be added
	 */
	public void addAll(InvertedIndex other) {
		lock.lockWrite();
		for (String word : other.map.keySet()) {
			if (this.map.containsKey(word) == false) {
				this.map.put(word, other.map.get(word));
			}
			else {
				for (String path : other.map.get(word).keySet()) {
					if (this.map.get(word).containsKey(path)) {
						this.map.get(word).get(path).addAll(other.map.get(word).get(path));
					} else {
						map.get(word).put(path, other.map.get(word).get(path));
					}
				}
			}
		}
		lock.unlockWrite();
	}

}