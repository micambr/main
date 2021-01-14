package cz.ambroz.bestgolf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class IOHumanTransform {
	
	public static void main(String[] args) throws IOException {
		FileWriter fw = new FileWriter("data/IOData.txt", true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    PrintWriter out = new PrintWriter(bw);

		FileReader fr = new FileReader("data/failuretData.txt");
	    BufferedReader br = new BufferedReader(fr);
	    while(br.ready()) {
	    	String line = br.readLine();
	    	showLine(line);
	    	String result = getResult();
	    	line = appendLine(line, result);
		    out.println(line);
	    }
	    
	    out.flush();
	    out.close();

		
	}

	public static String appendLine(String line, String result) {
		StringBuffer buff = new StringBuffer(line);
		for(int i = 0; i < 26; i++) {
			int charNr = result.charAt(0) - 'A';
			buff.append(i == charNr ? "1":"0");
			buff.append(",");
		}
		for(int i = 0; i < 26; i++) {
			int charNr = result.charAt(1) - 'A';
			buff.append(i == charNr ? "1":"0");
			buff.append(",");
		}
		buff.setLength(buff.length() - 1);
		return buff.toString();
	}

	private static String getResult() throws IOException {
		System.out.println("Two letters and ENTER please: ");
		BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
		String line = buffer.readLine();
		return line;
	}

	private static void showLine(String line) {
		String[] tokens = line.split(",");
		for (int i = 0; i < 80; i++) {
			for (int j = 0; j < 150; j++) {
				System.out.print(tokens[150*i+j]);
			}
			System.out.println();
		}

	}

}
