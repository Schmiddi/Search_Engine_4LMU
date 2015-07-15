package model;

public enum Fieldname {
	TITLE("title"), CATEGORIES("categories"), LINKS("links"), FREQWORDS("frequentWords"), /*BODY("textBody"), ID("id"),*/ SPELLCHECK("spellCheck");
	
	private final String name;
	
	private Fieldname(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
       return name;
    }
}
