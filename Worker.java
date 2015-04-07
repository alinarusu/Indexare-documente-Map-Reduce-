import java.io.IOException;

public class Worker extends Thread {
	private WorkPool unfinishedTasks;
	private WorkPool finishedTasks;

	public Worker(WorkPool unfinishedTasks, WorkPool finishedTasks) {
		this.unfinishedTasks = unfinishedTasks;
		this.finishedTasks = finishedTasks;
	}

	/**
	 * Procesarea unei solutii partiale. Aceasta poate implica generarea unor
	 * noi solutii partiale care se adauga in workpool folosind putWork(). Daca
	 * s-a ajuns la o solutie finala, aceasta va fi afisata.
	 */
	void processTask(Solution task) throws IOException {
		task.doWork();
		finishedTasks.tasks.add(task);
	}

	@Override
	public void run() {
		while (true) {
			Solution task = unfinishedTasks.getWork();
			if (task == null) {
				break;
			}
			try {
				processTask(task);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
