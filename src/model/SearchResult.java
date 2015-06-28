package model;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
	private List<WikiDocument> documents;
	private int totalHits;

	public List<WikiDocument> getDocuments() {
		return documents;
	}
	
	public SearchResult() {
		documents = new ArrayList<WikiDocument>();
		totalHits = 0;
	}

	public int getTotalHits() {
		return totalHits;
	}

	public void setDocuments(List<WikiDocument> documents) {
		this.documents = documents;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}
	
	public void addWikiDocument(WikiDocument document){
		documents.add(document);
	}
}
