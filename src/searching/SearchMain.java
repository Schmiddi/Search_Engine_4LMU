package searching;

import java.util.Scanner;

import model.Fieldname;
import model.SearchResult;
import model.WikiDocument;

public class SearchMain {

	public static void main(String[] argv) {
		defaultStdInWithSimilarSearch(30);
	}
	
	public static void defaultStdInWithSimilarSearch(int numResultsSimSearch){
		SearchFiles searchFile = new SearchFiles();

		Scanner scanner = new Scanner(System.in);
		String input = "";
		do{
			System.out.println("Enter search word: ");
			input = scanner.nextLine();
			
			SearchResult searchResult = searchFile.search(input,1,5,Fieldname.TITLE);

			if(!searchResult.getDocuments().isEmpty()){
//				System.out.println(searchResult.getDocuments().get(0).getTitle());
//				System.out.println(searchResult.getDocuments().get(0).getLinks());
//				System.out.println(searchResult.getDocuments().get(0).getCategories());
//				System.out.println(searchResult.getDocuments().get(0).getFreqWords());

				for (WikiDocument doc : searchResult.getDocuments()) {
					System.out.println(doc.getTitle());
				}
				System.out.println("-------------------------------");
				searchResult = searchFile.searchSimilarDocs(searchResult.getDocuments().get(0), numResultsSimSearch);
				for (WikiDocument doc : searchResult.getDocuments()) {
					System.out.println(doc.getTitle());
				}
			}
			System.out.println("\n\n");
		}while(!input.equals("exit"));
	}
}
