

public class SearchResult implements Comparable<SearchResult>{

	private final String path;
	private int frequency;
	private int position;

	/**
	 * constructor
	 * @param path the path of the result
	 */
	public SearchResult(String path) {
		this.path = path;
		this.frequency = 0;
		this.position = Integer.MAX_VALUE;
	}

	/**
	 * Compares based on frequency, then position, then alphabetical
	 */
	@Override
	public int compareTo(SearchResult arg0) {
		// Higher frequency
		if (this.frequency != arg0.frequency) {
			return Integer.compare(arg0.frequency, this.frequency);
		}
		else {
			// Lower Position
			if (this.position != arg0.position) {
				return Integer.compare(this.position, arg0.position);
			}
			else {
				// Alphabetical
				return String.CASE_INSENSITIVE_ORDER.compare(this.path.toString(), arg0.path.toString());
			}
		}
	}

	@Override
	public String toString() {
		return "\"" + path.toString() + "\"" + ", " + frequency + ", " + position + "\n";

	}

	/**
	 * increases the frequency
	 * @param frequency the frequency to add to the existing frequency
	 */
	public void addFrequency(int frequency) {
		this.frequency += frequency;
	}

	/**
	 * sets the position
	 * @param position the new position to set it to
	 */
	public void setPosition(int position) {
		if (this.position > position)
			this.position = position;
	}

	/**
	 * gets the frequency of the result
	 * @return the frequency
	 */
	public int getFrequency() {
		return this.frequency;
	}

	/**
	 * returns the path
	 * @return the path
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * gets the position
	 * @return the position
	 */
	public int getPosition() {
		return this.position;
	}

}