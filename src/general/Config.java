package general;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class Config {
	public final static String indexDir = "resources/index";
	public final static String wikiFile = "resources/data/Wiki/en/enwiki-first8miolines(withCloseTag).xml";

	public final static Analyzer analyzer = new StandardAnalyzer();
	
	/*
	 * Weka tries to find that many, but the actual number varies, since if there are many words with the same count it
	 * either keeps all or ignores all
	 */
	public final static int numOfFreqWords = 40;
}
