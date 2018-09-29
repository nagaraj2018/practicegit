package main.com.galaxy.logic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InputFileProcessor{
	static Map<String, String> tokenRomanValueMapping = new HashMap<String, String>();
	static Map<String, Float> tokenIntegerValue = new HashMap<String, Float>(); 
	static Map<String, String> questionAndReply = new HashMap<String, String>();
	static ArrayList<String> missingValues = new ArrayList<String>();
	static Map<String, Float> elementValueList = new HashMap<String, Float>(); 
	public static void ProcessFile(String filePath) throws IOException {
		BufferedReader bufferedReader = null;
		if (filePath == null){
			InputStream in = InputFileProcessor.class.getResourceAsStream("Input");
			bufferedReader =new BufferedReader(new InputStreamReader(in));
		}
		else{
			FileReader fileReader = new FileReader(filePath);
			bufferedReader = new BufferedReader(fileReader);
		}
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			processLine(line);
		}
		bufferedReader.close();
	}

	public static void processLine(String line){
		String arr[] = line.split("((?<=:)|(?=:))|( )");

		if (line.endsWith("?")){
			questionAndReply.put(line,"");
		}
		else if (arr.length == 3 && arr[1].equalsIgnoreCase("is")){
			tokenRomanValueMapping.put(arr[0], arr[arr.length-1]);
		}
		else if(line.toLowerCase().endsWith("credits")){
			missingValues.add(line);
		}
	}
	public static void MapTokentoIntegerValue(){

		Iterator it = tokenRomanValueMapping.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry token = (Map.Entry)it.next();
			float integerValue = new RomanToDecimal().romanToDecimal(token.getValue().toString());
			tokenIntegerValue.put(token.getKey().toString(), integerValue);
		}
		mapMissingEntities();
	}
	private static void mapMissingEntities(){
		for (int i = 0; i < missingValues.size(); i++) {
			deCodeMissingQuery(missingValues.get(i));
		}
	}

	private static void deCodeMissingQuery(String query){
		String array[] = query.split("((?<=:)|(?=:))|( )");
		int splitIndex = 0;
		int creditValue = 0; String element= null; String[] valueofElement = null;
		for (int i = 0; i < array.length; i++) {
			if(array[i].toLowerCase().equals("credits")){
				creditValue = Integer.parseInt(array[i-1]);
			}
			if(array[i].toLowerCase().equals("is")){
				splitIndex = i-1;
				element = array[i-1];
			}
			valueofElement = java.util.Arrays.copyOfRange(array, 0, splitIndex);
		}

		StringBuilder stringBuilder = new StringBuilder();
		for (int j = 0; j < valueofElement.length; j++) {
			stringBuilder.append(tokenRomanValueMapping.get(valueofElement[j]));
		}
		float valueOfElementInDecimal = new RomanToDecimal().romanToDecimal(stringBuilder.toString());
		elementValueList.put(element, creditValue/valueOfElementInDecimal);
	}

}
