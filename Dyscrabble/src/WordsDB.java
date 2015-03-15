import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;

public class WordsDB {
	private java.sql.Connection connection;
	private Statement stat;
	private File dbFile;
	private HashMap<String, FreqInfo> cacheMap;
	
	public WordsDB() {
		boolean dbCreated = false;
		File tempFile = new File("wordsDB/wordsDB.db");
		
		if (tempFile.isFile() && tempFile.exists())	dbCreated = true;	
			
		try {
			Class.forName("org.sqlite.JDBC");	
			connection = DriverManager.getConnection("jdbc:sqlite:wordsDB/wordsDB.db");		//will create wordsDB.db, quite slow
			stat = connection.createStatement();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cacheMap = new HashMap<String, FreqInfo>();
		dbFile = new java.io.File("wordsDB/Lex_ratio.txt");
		
		if (!dbCreated)	createDB();		//if database not exist, generate the database
	}
	
	//create the sqlite3 database using the Lex_ratio.txt file
	public boolean createDB() {
		if (dbFile.isFile() && dbFile.exists()) {
			try {
				InputStreamReader reader = new InputStreamReader(new FileInputStream(dbFile));
				BufferedReader bufferedReader = new BufferedReader(reader);
				
				String lineTxt = null;
				
				stat.executeUpdate("create table words(word varchar(40) PRIMARY KEY, freq long);"); //create the table
				while((lineTxt = bufferedReader.readLine()) != null){
					String[] para = lineTxt.split("\t| ");
                    String sql = String.format("insert into words values('%s',%d);", para[0],Long.parseLong(para[1]));
                    stat.executeUpdate(sql);
                }
                reader.close();
                connection.close();
                System.out.println("Finished!");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.out.printf("file not exists");
			return false;
		}
		return true;
	}
	
	public long queryForFreq(String word) {
		long res = 0;
		
		if (cacheMap.containsKey(word)) {
			FreqInfo info = cacheMap.get(word);
			res = info.getAbFreq();
			info.setRelFreq(info.getRelFreq() + 1);		//increase the relative frequency
		}
		else {
			String sql = String.format("select freq from words where word = '%s';",word);
			try {
				ResultSet resSet = stat.executeQuery(sql);
				stat.clearBatch();
				while (resSet.next()) {
					res = Long.parseLong(resSet.getString("freq"));
				}
				//word not found in the database
//				if (res == -1) {
//					String insertSql = String.format("insert into words values('%s',%d)", word,1);
//					stat.execute(insertSql);
//					res = 0;
//				}
				cacheMap.put(word, new FreqInfo(res, 1)); 		//add to the cache
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return res;
	}
	
	//clear the cache and write back to the database the cache info 
	public void updateFreq() {
		Iterator<java.util.Map.Entry<String, FreqInfo>> it = cacheMap.entrySet().iterator();
		
		while(it.hasNext()) {
			java.util.Map.Entry<String, FreqInfo> entry = it.next();
			String key = entry.getKey();
			FreqInfo value = entry.getValue();
			String sql = String.format("update words set freq = %d where word = '%s'",value.getAbFreq() + value.getRelFreq(), key);
			
			try {
				stat.execute(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		cacheMap.clear();	//clear the cache
	}
	
	//record the absolute and relative frequency of the words in hash map
	class FreqInfo {
		private long abFreq;
		private int relFreq;
		public FreqInfo(long aFreq, int rFreq) {
			// TODO Auto-generated constructor stub
			this.setAbFreq(aFreq);
			this.setRelFreq(rFreq);
		}
		public long getAbFreq() {
			return abFreq;
		}
		public void setAbFreq(long abFreq) {
			this.abFreq = abFreq;
		}
		public int getRelFreq() {
			return relFreq;
		}
		public void setRelFreq(int relFreq) {
			this.relFreq = relFreq;
		}
		
	}
}
