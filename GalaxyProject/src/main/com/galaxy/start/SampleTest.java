package main.com.galaxy.start;

import main.com.galaxy.logic.InputFileProcessor;
import main.com.galaxy.logic.OutputResultProcessor;

public class SampleTest{
	public static void main(String[] args) {
		String filePath = null;
		if (args.length != 0)
			filePath = args[0];
		try{
			InputFileProcessor.ProcessFile(filePath);
			InputFileProcessor.MapTokentoIntegerValue();
			OutputResultProcessor.processReplyForQuestion();
		}
		catch(Exception e){
			System.out.println("Oops !! Input File Not Found ");
		}
	}

}
