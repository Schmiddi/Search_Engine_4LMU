package general;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

public class Config {
	public final static String indexDir = "resources/index";
	public final static String wikiFile = "resources/data/Wiki/en/test2.xml";

	public final static Analyzer analyzer = new StandardAnalyzer();
}
