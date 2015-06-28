package indexing;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import model.Fieldname;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;
import general.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing. Run it with no command-line arguments for
 * usage information.
 */
public class IndexFiles {
	private static IndexWriterConfig iwc = new IndexWriterConfig(Config.analyzer);
	private static IndexWriter writer = null;

	/*
	 * Weka tries to find that many, but the actual number varies, since if there are many words with the same count it
	 * either keeps all or ignores all
	 */
	private final static int numOfFreqWords = 10;

	static {
		try {
			writer = new IndexWriter(FSDirectory.open(Paths.get(Config.indexDir)), iwc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Index all text files under a directory. */
	public static void main(String[] args) {
		Date start = new Date();

		try {
			// :Post-Release-Update-Version.LUCENE_XY:

			// Create a new index in the directory, removing any
			// previously indexed documents:
			iwc.setOpenMode(OpenMode.CREATE);

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer. But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);

			indexDocs(new File(Config.wikiFile));

			// NOTE: if you want to maximize search performance,
			// you can optionally call forceMerge here. This can be
			// a terribly costly operation, so generally it's only
			// worth it when your index is relatively static (ie
			// you're done adding documents to it):
			//
			// writer.forceMerge(1);

			writer.close();

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
		Date end = new Date();
		System.out.println(end.getTime() - start.getTime() + " total milliseconds");
	}

	/**
	 * Indexes the given file using the given writer, or if a directory is given, recurses over files and directories
	 * found under the given directory.
	 * 
	 * NOTE: This method indexes one document per input file. This is slow. For good throughput, put multiple documents
	 * into your input file(s). An example of this is in the benchmark module, which can create "line doc" files, one
	 * document per line, using the <a href=
	 * "../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
	 * >WriteLineDocTask</a>.
	 * 
	 * @param writer
	 *            Writer to the index where the given file/dir info will be stored
	 * @param file
	 *            The file to index, or the directory to recurse into to find files to index
	 * @throws IOException
	 *             If there is a low-level I/O error
	 */
	static void indexDocs(File file) throws IOException {
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(new File(file, files[i]));
					}
				}
			} else {

				WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(Config.wikiFile);

				try {

					wxsp.setPageCallback(new PageCallbackHandler() {
						public void process(WikiPage page) {

							/*
							 * Skip all redirection pages
							 */
							if (!page.isRedirect()) {
								/** create new dataset */
								Document doc = new Document();
								try {
									doc = parseWikipage(doc, page);

									/** New index, so we just add the document */
									System.out.println("adding " + page.getTitle().trim());
									if (doc != null)
										writer.addDocument(doc);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					});

					wxsp.parse();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Parses and stores the content of each Document to the fields
	 * 
	 * @param doc
	 *            The Document to which data is stored
	 * @param file
	 *            The input file which is being processed
	 * @return The Document with all fields or null if relevant fields could not be found in the datasets
	 * @throws IOException
	 */
	private static Document parseWikipage(Document doc, WikiPage page) throws IOException {

		doc.add(new TextField(Fieldname.TITLE.toString(), page.getTitle().trim(), Field.Store.YES));
		doc.add(new TextField(Fieldname.FREQWORDS.toString(), Utils.getMostFrequentWords(page.getWikiText(),
				numOfFreqWords), Field.Store.YES));
		doc.add(new TextField(Fieldname.BODY.toString(), Utils.wiki2text(page.getWikiText()), Field.Store.YES));
		doc.add(new IntField(Fieldname.ID.toString(), Integer.parseInt(page.getID()), Field.Store.YES));

		/*
		 * Remove whitespace and parenthesis within the link, such that in tokenization each link is handled as one
		 * token.
		 */
		String links = "";
		for (String link : page.getLinks()) {
			links += link.replaceAll("[() ]", "") + " ";
		}
		doc.add(new TextField(Fieldname.LINKS.toString(), links, Field.Store.YES));
		/*
		 * Remove whitespace and parenthesis within the category, such that in tokenization each category is handled as
		 * one token.
		 */
		String categories = "";
		for (String category : page.getCategories()) {
			categories += category.replaceAll("[() ]", "") + " ";
		}
		doc.add(new TextField(Fieldname.CATEGORIES.toString(), categories, Field.Store.YES));

		return doc;
	}
}