package model;

import java.util.Arrays;
import java.util.List;

public class WikiDocument {
	private String title;
	private List<String> categories;
	private List<String> links;
	private List<String> freqWords;
	private String textBody;
	private int id;

	public void setField(Fieldname field, String value) {
		switch (field) {
		case TITLE:
			setTitle(value);
			break;
		case CATEGORIES:
			setCategories(stringToList(value));
			break;
		case LINKS:
			setLinks(stringToList(value));
			break;
		case FREQWORDS:
			setFreqWords(stringToList(value));
			break;
		case BODY:
			setTextBody(value);
			break;
		case ID:
			setId(Integer.parseInt(value));
			break;
		}
	}

	private List<String> stringToList(String input){
		String [] split = input.split(" ");
		return Arrays.asList(split);
	}
	
	public String getTitle() {
		return title;
	}

	public List<String> getCategories() {
		return categories;
	}

	public List<String> getLinks() {
		return links;
	}

	public List<String> getFreqWords() {
		return freqWords;
	}

	public String getTextBody() {
		return textBody;
	}

	public int getId() {
		return id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public void setLinks(List<String> links) {
		this.links = links;
	}

	public void setFreqWords(List<String> freqWords) {
		this.freqWords = freqWords;
	}

	public void setTextBody(String textBody) {
		this.textBody = textBody;
	}

	public void setId(int id) {
		this.id = id;
	}
}
