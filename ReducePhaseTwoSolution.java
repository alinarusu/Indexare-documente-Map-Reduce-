import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ReducePhaseTwoSolution extends Solution {

	private String suspiciousFileName = null;
	private String baseFileName = null;
	private Map<String, Float> suspiciousFileWordMap = new HashMap<String, Float>();
	private Map<String, Float> baseFileWordMap = new HashMap<String, Float>();
	private HashSet<String> aggregatedWords = new HashSet<String>();

	public HashSet<String> getAggregatedWords() {
		return aggregatedWords;
	}

	public Float getComputedSimilarityPercent() {
		return computedSimilarityPercent;
	}

	public void setSuspiciousFileName(String suspiciousFileName) {
		this.suspiciousFileName = suspiciousFileName;
	}

	public void setBaseFileName(String baseFileName) {
		this.baseFileName = baseFileName;
	}

	public void setSuspiciousFileWordMap(
			Map<String, Float> suspiciousFileWordMap) {
		this.suspiciousFileWordMap = suspiciousFileWordMap;
	}

	public String getSuspiciousFileName() {
		return suspiciousFileName;
	}

	public String getBaseFileName() {
		return baseFileName;
	}

	public void setBaseFileWordMap(Map<String, Float> baseFileWordMap) {
		this.baseFileWordMap = baseFileWordMap;
	}

	private Float computedSimilarityPercent = 0.0f;

	private void aggregateWords() {
		this.aggregatedWords.addAll(suspiciousFileWordMap.keySet());
		this.aggregatedWords.addAll(baseFileWordMap.keySet());
	}

	@Override
	public void doWork() throws IOException {
		aggregateWords();
		for (String word : this.aggregatedWords) {
			this.computedSimilarityPercent += (suspiciousFileWordMap.get(word) == null ? 0
					: suspiciousFileWordMap.get(word))
					* (baseFileWordMap.get(word) == null ? 0 : baseFileWordMap
							.get(word));
		}
	}
}
