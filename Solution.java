import java.io.IOException;

public abstract class Solution {

	String fileName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public abstract void doWork() throws IOException;
}
