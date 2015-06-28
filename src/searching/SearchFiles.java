package searching;

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

import general.Config;

import java.io.IOException;
import java.nio.file.Paths;

import model.Fieldname;
import model.SearchResult;
import model.WikiDocument;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/** Simple command-line based search demo. */
public class SearchFiles {
	/*
	 * Static variables, for performance reasons, to avoid unnecessary creation
	 * of IndexSearcher
	 */

	private static IndexSearcher searcher = null;

	static{
		try {
			searcher = new IndexSearcher(DirectoryReader
					.open(FSDirectory.open(Paths.get(Config.indexDir))));
		} catch (IOException e) {
			e.printStackTrace();
	}
	}
	public static void main(String[] argv) {
		SearchFiles searchFile = new SearchFiles();
		
		
		 SearchResult searchResult = searchFile.search("Aristotle",1,10,Fieldname.TITLE);
		 System.out.println(searchResult.getDocuments().get(0).getFreqWords());
	}

	public SearchResult search(String queryString, int startResult,
			int numberOfResults, Fieldname fieldname) {

		String[] fieldnames = new String[1];
		fieldnames[0] = fieldname.toString();

		return executeSearch(queryString, startResult, numberOfResults,
				fieldnames);
	}

	private SearchResult executeSearch(String queryString, int startResult,
			int numberOfResults, String[] fieldnames) {
		SearchResult searchResult = null;

		try {
			QueryParser parser = new MultiFieldQueryParser(fieldnames, Config.analyzer);

			queryString = queryString.trim();

			if (queryString.length() == 0) {
				return null;
			}

			Query query = parser.parse(queryString);
			System.out.println("Query: " + query.toString(fieldnames[0]));

			searchResult = doPagingSearch(searcher, query, startResult,
					numberOfResults);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return searchResult;
	}

	/**
	 * This demonstrates a typical paging search scenario, where the search
	 * engine presents pages of size n to the user. The user can then go to the
	 * next page if interested in the next hits.
	 * 
	 * When the query is executed for the first time, then only enough results
	 * are collected to fill 5 result pages. If the user wants to page beyond
	 * this limit, then the query is executed another time and all hits are
	 * collected.
	 * 
	 */
	private SearchResult doPagingSearch(IndexSearcher searcher, Query query,
			int offset, int numberOfResults) throws IOException {

		int requestedResults = offset + numberOfResults - 1;

		TopDocs results = searcher.search(query, requestedResults);

		int numTotalHits = results.totalHits;

		ScoreDoc[] hits = results.scoreDocs;
		System.out.println(hits.length); //TODO: Maybe remove
		SearchResult searchResult = new SearchResult();

		searchResult.setTotalHits(numTotalHits);

		int end = Math.min(numTotalHits, requestedResults);

		for (int i = offset - 1; i < end; i++) {
			WikiDocument wikiDoc = new WikiDocument();

			Document doc = searcher.doc(hits[i].doc);

			for (Fieldname f : Fieldname.values()) {
				if (doc.get(f.toString()) != null)
					wikiDoc.setField(f, doc.get(f.toString()));
			}

			searchResult.addWikiDocument(wikiDoc);
		}
		return searchResult;
	}

}
