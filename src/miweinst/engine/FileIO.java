package miweinst.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**This class generically saves a plaintext file called save_data.txt in the game's
 * resources folder. Can be subclassed and partially overwritten if another file name
 * is desired. Methods are all accessed as static.*/

public class FileIO{
	
	protected final static String OUTPUT_FILE_NAME = "src/miweinst/resources/save_data.txt";
	
	/* Creates or overwrites a file save_data.txt in /resources folder. 
	 * Takes in List of Strings that will be read upon load. String data 
	 * should be separated by line; each item in List<String> represents
	 * a line in save_data.txt.*/
	public static void write(List<String> data){
		try {
			//Either overwrite or create a save file in resources
			File file = new File(OUTPUT_FILE_NAME);
			if (!file.exists())
				file.createNewFile();

			//Handle permissions for user
			file.setReadable(true, false);
			file.setWritable(true, false);
			file.setExecutable(true, false);
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			//write each item in data List as line in file.
			for (String line: data) {
				bw.write(line);
				bw.newLine();
			}
			//close stream
			bw.close();

			System.out.println("Save complete");
		}
		catch (IOException e) {
			System.err.println("Save failed");
			e.printStackTrace();
		}
	}
	
	/*Reads file written by write() line by line. Returns
	 * a Map from any string before colon followed by space ': ' 
	 * to any string after. Label should be before colon-space.*/
	public static Map<String, String> read() {
		Map<String, String> map = new HashMap<String, String>();
		try {		
			File file = new File(OUTPUT_FILE_NAME);
			if (!file.exists()) 
				System.err.println("File does not exist! (FileIO.read");
			FileReader fr = new FileReader(file.getAbsoluteFile());
			BufferedReader br = new BufferedReader(fr);
			
			try {
				String str;
				while ((str = br.readLine()) != null) {
					String[] strArr = str.split(": ");
					map.put(strArr[0], strArr[1]);
				}
			} catch(ArrayIndexOutOfBoundsException e) {
				System.err.println("Load failed: invalid save_data.txt. Delete file and play game to restore.");
			}
			br.close();
			
			System.out.println("Load complete");

		} catch (FileNotFoundException e) {
			System.err.println("Load failed");
//			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Load failed");
//			e.printStackTrace();
		}
		return map;
	}	
}




