package searching;

import general.Config;

import java.io.IOException;
import java.nio.file.Paths;

import model.Fieldname;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.LuceneLevenshteinDistance;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class CheckInput {
	private static Directory indexSpellCheckerDir;
	private static Directory indexDir;
	private static IndexReader indexReader;
	
	private static IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
	
	private static SpellChecker spellchecker;
	static{
		try{
		indexSpellCheckerDir = FSDirectory.open(Paths.get(Config.indexSpellCheckerDir));
		indexDir = FSDirectory.open(Paths.get(Config.indexDir));
		indexReader = DirectoryReader.open(indexDir);
		
		iwc = new IndexWriterConfig(new StandardAnalyzer());
		
		spellchecker = new SpellChecker(indexSpellCheckerDir, new LuceneLevenshteinDistance());

		// To index a field of a user index:
		spellchecker.indexDictionary(new LuceneDictionary(indexReader, Fieldname.SPELLCHECK.toString()), iwc , true);
		
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static String getSuggestion(String input){
		String[] suggestions = null;
		
		try {
			suggestions = spellchecker.suggestSimilar(input, 1,indexReader, Fieldname.SPELLCHECK.toString(), SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(suggestions.length >= 1){
			return suggestions[0];
		} else
			return null;
	}
}
