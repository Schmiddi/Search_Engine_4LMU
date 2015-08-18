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
			System.out.println("Enter search article: ");
			input = scanner.nextLine();
			
			// Validate the query term
			String suggestion = CheckInput.getSuggestion(input);
			if(suggestion != null && !suggestion.equals(input)){
				String answer = null;
				do{
					System.out.println("Did you mean: \"" + suggestion + "\" (y/n):");
					answer = scanner.nextLine();
				}while(!(answer.equals("y")||answer.equals("n")));
				
				if(answer.equals("y"))
					input = suggestion;
			}
			
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
			}else
				System.out.println("No result found!");
			System.out.println("\n\n");
		}while(!input.equals("exit"));
		
		scanner.close();
	}
}
