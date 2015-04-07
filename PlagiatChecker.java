import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Program which compares given files to check for similarities (plagiarism)
 * 
 * @author Rusu Alina
 */
public class PlagiatChecker {

	public static void main(String[] args) throws InterruptedException {

		Integer threadNumber = null;
		String inputFileName = null;
		String outputFileName = null;

		String suspiciousFileName = null;
		Integer chunkSize = null;
		Float similarityThreshold = null;
		Integer baseFilesNumber = null;
		List<String> baseFiles = null;

		FileReader fileReader = null;
		PrintWriter printWriter = null;
		BufferedReader reader = null;

		WorkPool unfinishedTaskWorkPool = null;
		WorkPool finishedTaskWorkPool = null;

		List<Worker> workers = null;
		List<Thread> threads = null;
		DecimalFormat threeDForm = new DecimalFormat("#.###");

		// sanity checks
		if (args.length == 0) {
			System.err
					.println("Mandatory input:\n1. number of threads\n2. input file name\n3. output file name");
			System.exit(1);
		}

		threadNumber = new Integer(Integer.parseInt(args[0]));

		inputFileName = new String(args[1]);
		try {
			fileReader = new FileReader(inputFileName);
		} catch (FileNotFoundException e) {
			System.out.println("Input file name not given correctly");
		}

		outputFileName = new String(args[2]);
		try {
			printWriter = new PrintWriter(outputFileName);
		} catch (FileNotFoundException e) {
			System.out.println("Output file name not given correctly");
		}

		reader = new BufferedReader(fileReader);
		try {
			suspiciousFileName = reader.readLine();
			chunkSize = new Integer(Integer.parseInt(reader.readLine()));
			similarityThreshold = new Float(Float.parseFloat(reader.readLine()));
			baseFilesNumber = new Integer(Integer.parseInt(reader.readLine()));

			if (suspiciousFileName == null || chunkSize == null
					|| similarityThreshold == null || baseFilesNumber == null) {
				System.out.println("File not properly formatted!");
			}

			baseFiles = new ArrayList<String>();
			for (int i = 0; i < baseFilesNumber; i++) {
				baseFiles.add(reader.readLine());
			}
		} catch (IOException e) {
			System.out.println("IO exception");
		}

		unfinishedTaskWorkPool = new WorkPool(threadNumber);
		finishedTaskWorkPool = new WorkPool(threadNumber);

		try {
			splitFileIntoChunksAndCreateTasks(unfinishedTaskWorkPool,
					baseFiles, chunkSize);
		} catch (IOException e) {
			e.printStackTrace();
		}

		threads = new ArrayList<Thread>();
		workers = new ArrayList<Worker>();

		// create (threadNumber) workers for the Map act.
		for (int i = 0; i < threadNumber; i++) {
			Worker worker = new Worker(unfinishedTaskWorkPool,
					finishedTaskWorkPool);
			workers.add(worker);
			Thread thread = new Thread(workers.get(i));
			threads.add(thread);
		}

		// put the workers to work
		for (Thread t : threads) {
			t.start();
		}

		// wait for them to finish the Map Phase
		for (Thread t : threads) {
			t.join();
		}

		List<ReducePhaseOneSolution> reducep1Tasks = new ArrayList<ReducePhaseOneSolution>();

		// begin the first phase of the reduce part
		for (String file : baseFiles) {
			ReducePhaseOneSolution task = new ReducePhaseOneSolution();
			task.setFileName(file);
			reducep1Tasks.add(task);
		}

		/*
		 * Group all the HashMaps generated in the Map phase for the 500bytes
		 * long parts by filename. Now, workers could aggregate all the hashMaps
		 * into a single one in the first phase of the Reduce act
		 */
		for (int i = 0; i < finishedTaskWorkPool.tasks.size(); i++) {
			MapSolution m = (MapSolution) finishedTaskWorkPool.tasks.get(i);
			for (int j = 0; j < reducep1Tasks.size(); j++) {
				if (m.getFileName().equals(baseFiles.get(j))) {
					reducep1Tasks.get(j).getGatheredWordMaps()
							.add(m.getWordMap());
					reducep1Tasks.get(j).setTotalWordsPerFile(
							reducep1Tasks.get(j).getTotalWordsPerFile()
									+ m.getWordCount());
				}
			}
		}

		// clear from memory all the processed tasks and make room in Heap for
		// the tasks for the Reduce phase
		unfinishedTaskWorkPool = new WorkPool(threadNumber);
		finishedTaskWorkPool = new WorkPool(threadNumber);

		// add (number of files) tasks to the pool
		for (ReducePhaseOneSolution s : reducep1Tasks) {
			unfinishedTaskWorkPool.tasks.add(s);
		}

		// create new workers for the first phase of the Reduce act.
		// note: the finished and unfinished workpool are now changed!
		threads = new ArrayList<Thread>();
		workers = new ArrayList<Worker>();
		for (int i = 0; i < threadNumber; i++) {
			Worker worker = new Worker(unfinishedTaskWorkPool,
					finishedTaskWorkPool);
			workers.add(worker);
			Thread thread = new Thread(workers.get(i));
			threads.add(thread);
		}

		// put the workers to work at the first phase of the Reduce act.
		for (Thread t : threads) {
			t.start();
		}

		// wait for them to finish the the first phase of the Reduce act.
		for (Thread t : threads) {
			t.join();
		}

		for (int i = 0; i < finishedTaskWorkPool.tasks.size(); i++) {
			ReducePhaseOneSolution rq = (ReducePhaseOneSolution) finishedTaskWorkPool.tasks
					.get(i);
			System.out.println(rq.getFileName() + " "
					+ rq.getPercentageWordMap());
		}

		List<ReducePhaseTwoSolution> reducep2Tasks = new ArrayList<ReducePhaseTwoSolution>();

		// begin the second phase of the reduce part
		for (String file : baseFiles) {
			if (file.equals(suspiciousFileName)) 
			{
				continue;
			}

			ReducePhaseTwoSolution task = new ReducePhaseTwoSolution();
			task.setSuspiciousFileName(suspiciousFileName);
			task.setBaseFileName(file);

			for (Solution s : finishedTaskWorkPool.tasks) {
				ReducePhaseOneSolution r = (ReducePhaseOneSolution) s;
				if (r.getFileName().equals(file)) {
					task.setBaseFileWordMap(r.getPercentageWordMap());
					break;
				}
			}

			for (Solution s : finishedTaskWorkPool.tasks) {
				ReducePhaseOneSolution r = (ReducePhaseOneSolution) s;
				if (r.getFileName().equals(suspiciousFileName)) {
					task.setSuspiciousFileWordMap(r.getPercentageWordMap());
					break;
				}
			}

			reducep2Tasks.add(task);
		}

		// clear from memory all the processed reduce phase 1 tasks and make
		// room in Heap for the phase 2 tasks
		unfinishedTaskWorkPool = new WorkPool(threadNumber);
		finishedTaskWorkPool = new WorkPool(threadNumber);

		// add (number of files) tasks to the pool
		for (ReducePhaseTwoSolution s : reducep2Tasks) {
			unfinishedTaskWorkPool.tasks.add(s);
		}

		// create new workers for the second phase of the Reduce act.
		// note: the finished and unfinished workpool are now changed!
		threads = new ArrayList<Thread>();
		workers = new ArrayList<Worker>();
		for (int i = 0; i < threadNumber; i++) {
			Worker worker = new Worker(unfinishedTaskWorkPool,
					finishedTaskWorkPool);
			workers.add(worker);
			Thread thread = new Thread(workers.get(i));
			threads.add(thread);
		}

		// put the workers to work at the second phase of the Reduce act.
		for (Thread t : threads) {
			t.start();
		}

		// wait for them to finish the the second phase of the Reduce act.
		for (Thread t : threads) {
			t.join();
		}

		System.out.println("Rezultate pentru: (" + suspiciousFileName + ")");
		System.out.println();

		printWriter.println("Rezultate pentru: (" + suspiciousFileName + ")\n");
		for (Solution s : finishedTaskWorkPool.tasks) {
			ReducePhaseTwoSolution r = (ReducePhaseTwoSolution) s;
			if (Float.valueOf(threeDForm.format(r
					.getComputedSimilarityPercent() / 100)) > similarityThreshold) {
				System.out.println(r.getBaseFileName()
						+ " ("
						+ Float.valueOf(threeDForm.format(r
								.getComputedSimilarityPercent() / 100)) + "%)");
				printWriter.println(r.getBaseFileName()
						+ " ("
						+ Float.valueOf(threeDForm.format(r
								.getComputedSimilarityPercent() / 100)) + "%)");
			}
		}
	}

	private static void splitFileIntoChunksAndCreateTasks(
			WorkPool unfinishedTaskWorkPool, List<String> baseFiles,
			Integer chunkSize) throws IOException {
		RandomAccessFile reader = null;
		Long fileLength = 0L;
		Integer begin = 0;
		Integer end = 0;

		for (String fileName : baseFiles) {
			reader = new RandomAccessFile(fileName, "r");
			fileLength = reader.length();

			begin = 0;
			end = 0;
			while (end < fileLength) {
				begin = end;
				if (fileLength > begin + chunkSize) {
					end = begin + chunkSize;
				} else {
					end = fileLength.intValue();
				}
				MapSolution solution = new MapSolution();
				solution.setFileName(fileName);
				solution.setBeginOffset(begin);
				solution.setEndOffset(end);

				unfinishedTaskWorkPool.tasks.add(solution);
			}
		}
	}
}
