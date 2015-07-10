package evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import searching.SearchFiles;
import model.Fieldname;
import model.SearchResult;
import model.WikiDocument;

public class EvalSearch {
	private static String inputFile = "resources/TestData.txt";
	private static float [] gridValues = {0f, 0.1f, 0.3f, 0.7f, 1f, 2f, 5f, 10f, 25f};
	
	public static void main(String[] args) throws IOException {
		ArrayList<String[]> testData = loadTestData();
		for (String[] str : testData) {
			System.out.println("Search Article: " + str[0] + " | " + str[1] + ", " + str[2]);
		}

		System.out.println("\nStarting grid search...\n");
		
		SearchFiles searchFile = new SearchFiles();
		int overallBestScore = 0;
		float[] bestSetting = {-1, -1, -1};
		
		long startTime = System.nanoTime();   

		for(int a = 0; a < gridValues.length; a ++){
			for(int b=0; b < gridValues.length; b ++){
				for( int c=0; c < gridValues.length; c++){
					int totalScore = runTest(testData, a, b, c);
					
					if (totalScore > overallBestScore){
						overallBestScore = totalScore;
						bestSetting[0] = gridValues[a];
						bestSetting[1] = gridValues[b];
						bestSetting[2] = gridValues[c];
						
						System.out.println("New best score: " + totalScore + " (boostCat: " + gridValues[a] +", boostLinks: " + gridValues[b] + ", boostFreqWords: " + gridValues[c] + ")");
					}
				}
			}
		}

		long estimatedTime = System.nanoTime() - startTime;
		double timeInSec = estimatedTime / 1000000000.0;
		
		System.out.println("Total time taken: " + timeInSec + "s");
	}

	public static int runTest(ArrayList<String[]> testData, float boostCat, float boostLinks, float boostFreqWords){
		SearchFiles searchFile = new SearchFiles();
		int totalScore = 0;
		
		for (String[] str : testData) {
			
			SearchResult searchResult = searchFile.search(str[0], 1, 1, Fieldname.TITLE);

			if (!searchResult.getDocuments().isEmpty()) {
				searchResult = searchFile.searchSimilarDocs(searchResult.getDocuments().get(0), 10, boostCat, boostLinks, boostFreqWords);
				for (int i = 0; i < searchResult.getDocuments().size(); i++) {
					if(searchResult.getDocuments().get(i).getTitle().trim().equals(str[1])){
						totalScore += getScoreByPosition(i+1);
					} else if(searchResult.getDocuments().get(i).getTitle().trim().equals(str[2])){
						totalScore += getScoreByPosition(i+1);
					}
				}
			}
		}
		
		return totalScore;
	}
	public static ArrayList<String[]> loadTestData() throws IOException {

		BufferedReader read = new BufferedReader(new FileReader(inputFile));

		ArrayList<String[]> testData = new ArrayList<String[]>();

		String line = read.readLine();

		while (line != null) {
			String[] splitted = line.split("\\t");
			testData.add(splitted);
			line = read.readLine();
		}

		return testData;
	}

	/**
	 * Get the score py the position of the match, points according to the Formula One scoring system.
	 * 
	 * @param position
	 * @return
	 */
	private static int getScoreByPosition(int position) {
		switch (position) {
		case 1:
			return 25;
		case 2:
			return 18;
		case 3:
			return 15;
		case 4:
			return 12;
		case 5:
			return 10;
		case 6:
			return 8;
		case 7:
			return 6;
		case 8:
			return 4;
		case 9:
			return 2;
		case 10:
			return 1;
		default:
			return 0;
		}
	}
}
