import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Class representing a task in the WorkPool, in the first phase of the Reduce
 * paradigm.<br>
 * 
 * </p>
 * 
 * @author Rusu Alina
 * @since 16.11.2013
 */

public class ReducePhaseOneSolution extends Solution {

	private Integer totalWordsPerFile = 0;

	private Map<String, Integer> wordMap = new HashMap<String, Integer>();

	private ArrayList<HashMap<String, Integer>> gatheredWordMaps = new ArrayList<HashMap<String, Integer>>();

	private HashMap<String, Float> percentageWordMap = new HashMap<String, Float>();
	
	public void setTotalWordsPerFile(Integer totalWordsPerFile) {
		this.totalWordsPerFile = totalWordsPerFile;
	}

	public ArrayList<HashMap<String, Integer>> getGatheredWordMaps() {
		return gatheredWordMaps;
	}

	public void setGatheredWordMaps(
			ArrayList<HashMap<String, Integer>> gatheredWordMaps) {
		this.gatheredWordMaps = gatheredWordMaps;
	}

	public Integer getTotalWordsPerFile() {
		return totalWordsPerFile;
	}

	public Map<String, Float> getPercentageWordMap() {
		return percentageWordMap;
	}

	/**
	 * Unificare mortii calului.. Frecventa cuvintelor/document
	 */

	@SuppressWarnings("unused")
	@Override
	public void doWork() throws IOException {
		Set<String> wordsFromCurrentMap;

		/*
		 * Aggregate all words from all Map phased maps into one map, which
		 * contains all words from a single file.
		 */
		for (HashMap<String, Integer> map : gatheredWordMaps) {
			wordsFromCurrentMap = new HashSet<String>();
			wordsFromCurrentMap = map.keySet();

			for (String word : wordsFromCurrentMap) {
				if (!wordMap.containsKey(word)) {
					wordMap.put(word, map.get(word));
				} else {
					wordMap.put(word, wordMap.get(word) + map.get(word));
				}
			}
		}
		
		for (String word : wordMap.keySet()) {
			Float percent = 0.0f;
			//if (!percentageWordMap.containsKey(word)) 
			{
				percent = (float) (wordMap.get(word) * 1.0) / this.totalWordsPerFile * 100;
				DecimalFormat twoDForm = new DecimalFormat("#.##");
				percentageWordMap.put(word, Float.valueOf(twoDForm.format(percent)));
				//percentageWordMap.put(word, percent);
			}
		}
	}

}
