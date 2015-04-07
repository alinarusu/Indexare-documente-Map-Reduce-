import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Class representing a task in the WorkPool, in the Map phase Here, the given
 * document in processed and the consequent maps are built.
 * </p>
 * 
 * @author Rusu Alina
 * @since 16.11.2013
 */

public class MapSolution extends Solution {

	private ArrayList<Byte> rawData = null;
	private byte[] trimmedRawData = null;
	private String rawDataAsString = null;
	private Integer beginOffset = null;
	private Integer endOffset = null;
	private ArrayList<String> words = null;
	private Integer wordCount = 0;
	private HashMap<String, Integer> wordMap = null;

	public void setBeginOffset(Integer beginOffset) {
		this.beginOffset = beginOffset;
	}

	public void setEndOffset(Integer endOffset) {
		this.endOffset = endOffset;
	}

	public ArrayList<Byte> getRawData() {
		return rawData;
	}

	public void setRawData(ArrayList<Byte> rawData) {
		this.rawData = rawData;
	}

	public Integer getBeginOffset() {
		return beginOffset;
	}

	public Integer getEndOffset() {
		return endOffset;
	}

	public Integer getWordCount() {
		return wordCount;
	}

	public HashMap<String, Integer> getWordMap() {
		return wordMap;
	}

	/**
	 * <p>
	 * Firstly check if the rawData begins in the middle of a word. There are
	 * two possible scenarios:<br>
	 * </p>
	 * 1. if rawData starts in the middle of a word then it must begin
	 * processing beginning from the next word<br>
	 * 2. if rawData starts exactly at the beginning of a word, then it must
	 * process from there on<br>
	 * 
	 * @throws IOException
	 * 
	 */

	@Override
	public void doWork() throws IOException {
		RandomAccessFile reader = null;
		Boolean skipFirstWord = false;
		Integer charsAfterEndOffset = 0;
		Integer trimmedRawDataSize = 0;

		try {
			reader = new RandomAccessFile(this.fileName, "r");
		} catch (FileNotFoundException e) {
			System.err.println("The given filename is invalid");
		}

		reader.seek(endOffset);
		try {
			if (endOffset < reader.length()) {
				try {
					char c = reader.readChar();
					charsAfterEndOffset = 0;
					while (Character.isAlphabetic(c)) {
						c = reader.readChar();
						charsAfterEndOffset++;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		reader.seek(beginOffset);
		if (beginOffset > 0) {
			try {
				reader.seek(beginOffset - 1);
				char charBeforeBeginOffset = reader.readChar();
				if (Character.isAlphabetic(charBeforeBeginOffset)) {
					skipFirstWord = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		reader.seek(beginOffset);
		trimmedRawDataSize = endOffset + charsAfterEndOffset - beginOffset;
		this.trimmedRawData = new byte[trimmedRawDataSize];

		reader.read(trimmedRawData);
		rawDataAsString = new String(trimmedRawData);

		// split the raw data into into individual words using a regular
		// expression
		String[] rawWords = new String[trimmedRawDataSize];
		rawWords = rawDataAsString.split("[^a-zA-Z]");
		
		//rawDataAsString = rawDataAsString.replaceAll("[^(\\w|\\s)]|\\d|\\(|\\)", " ").replaceAll("\\s+", " ").toLowerCase().trim();
		//rawWords = rawDataAsString.split(" ");

		aggregateWords(skipFirstWord, rawWords);
		buildWordMap();

	}

	private void aggregateWords(Boolean skipFirstWord, String[] rawWords) {
		this.words = new ArrayList<String>();
		if (!skipFirstWord) {
			if (!rawWords[0].equals("")) {
				this.words.add(rawWords[0].toLowerCase());
			}
		}
		for (int i = 1; i < rawWords.length; i++) {
			if (!rawWords[i].equals("")) {
				words.add(rawWords[i].toLowerCase());
			}
		}
		this.wordCount += words.size();
	}

	private void buildWordMap() {
		this.wordMap = new HashMap<String, Integer>();
		for (String word : this.words) {
			if (!wordMap.containsKey(word)) {
				wordMap.put(word, 1);
			} else {
				Integer sightings = wordMap.get(word);
				wordMap.put(word, sightings + 1);
			}
		}
	}
}
