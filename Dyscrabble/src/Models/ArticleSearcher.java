package Models;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ArticleSearcher {
	
	private ArrayList<String[]> mapList;		//list of file map (fileName, date)
	private Set<String> dateSet;				//set storing the emerging date 
	private static ArticleSearcher instance;
	
	private ArticleSearcher() {
		mapList = new ArrayList<String[]>();
		dateSet = new HashSet<String>();
		readFileNames();
	}
 
	public static synchronized ArticleSearcher getInstance() {
		if (instance == null)	instance = new ArticleSearcher();
		return instance;
	}
	
	public Set<String> getDateSet() {
		return this.dateSet;
	}
	
	public ArrayList<String[]> getMapList() {
		return this.mapList;
	}
	
	//invoke the crawler
	public void callCrawler() {
//		jython api can not recognize python encode('gb18030'), so do not use this
//		PythonInterpreter interpreter = new PythonInterpreter();
//		interpreter.execfile("articles/Crawler.py");
//		PyFunction func = (PyFunction)interpreter.get("searchBreakingNews", PyFunction.class);
//		func.__call__(new PyInteger(40));
		try {
			int getArtNum = 20;
			Process process = Runtime.getRuntime().exec(String.format("./articles/Crawler.py %d", getArtNum));
			
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));  
			String line = null;
			while((line = br.readLine()) != null ) {
				System.out.println(line);
				System.out.flush();
			}
			
			br.close(); 
			//process.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		readFileNames();
	}
	
	public String pickArticle() {
		//shuffle the files
		Collections.shuffle(mapList);
		
		String pickedDate = pickDate();
		
		if (pickedDate != null) {
			for(String[] fileNameGroup : mapList) 
				if (fileNameGroup[1].equals(pickedDate))	return fileNameGroup[0];
		}
		
		return null;
	}
	
	public void readFileNames() {
		//update the mapList and dataSet
		mapList.clear();
		dateSet.clear();
		
		File path = new File("articles");
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(path.list()));
		
		Iterator<String> iterator = list.iterator();
		
		//filter the irrelevant filenames and construct the map list with (fileName, date)
		while (iterator.hasNext()) {
			String temp = iterator.next();
			String[] cutArr = temp.split("--");
			if (cutArr.length >= 3)  {
				mapList.add(new String[]{temp,cutArr[0]});
				dateSet.add(cutArr[0]);
			}
		}
	}

	//pick a date, more timely date will be picked with higher probability
	private String pickDate() {
		
		if (dateSet.size() == 0) return null;
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String today = df.format(new Date());
		
		while(true) {
			Iterator<String> it = dateSet.iterator();
			if (dateSet.size()==1)  return it.next();
			
			while(it.hasNext()) {
				String temp = it.next();
				int diff = Integer.parseInt(today) - Integer.parseInt(temp);
				double prob = Math.random();
				
				switch (diff) {
					case 0:		//today
						if (prob > 0.15)	return temp;
						break;
					case 1:		//yesterday
						if (prob > 0.40)	return temp;
						break;
					case 2:		//2 days ago
						if (prob > 0.75)	return temp;
						break;
					case 3:		//3 days ago
						if (prob > 0.85)	return temp;
						break;
					default:	//over 3 days ago
						if (prob > 0.95)	return temp;
						break;
				}
			}
		}
	}
}
