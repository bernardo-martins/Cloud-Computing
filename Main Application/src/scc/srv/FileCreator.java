package scc.srv;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
public class FileCreator {

	public FileCreator() {
		
	}
	
	public void newFile(String fileID, String content) throws IOException {
		File file = new File(fileID);
		if(file.exists()) {
			return;
		}
		FileWriter fw = new FileWriter(file);
		fw.write(content);
		fw.close();
	}
	
	public String getFileContent(String fileID) throws IOException {
		File file = new File(fileID);
		if(!file.exists()) {
			throw new IOException();
		}
		FileReader fr = new FileReader(file);
		String result = "";
		char[] content = new char[1024];
		int data = fr.read(content);
		result = String.valueOf(content);
		    while(data != -1) {
		    	data = fr.read(content);
		    	result += String.valueOf(content);
		    }
			return result;
		    
	}
	
	public void removeFile(String fileID) throws IOException {
		File file = new File(fileID);
		if(!file.exists()) {
			throw new IOException();
		}
		file.delete();
	}
	
	
}
