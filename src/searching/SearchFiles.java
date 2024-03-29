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

import javax.management.InvalidAttributeValueException;

import model.Fieldname;
import model.SearchResult;
import model.WikiDocument;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/** Simple command-line based search demo. */
public class SearchFiles {
	/*
	 * Static variables, for performance reasons, to avoid unnecessary creation of IndexSearcher
	 */

	private static IndexSearcher searcher = null;

	static {
		BooleanQuery.setMaxClauseCount(2048);
		
		try {
			searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(Config.indexDir))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public SearchResult search(String queryString, int startResult, int numberOfResults, Fieldname fieldname) {

		String[] fieldnames = new String[1];
		fieldnames[0] = fieldname.toString();

		return executeSearch(queryString, startResult, numberOfResults, fieldnames);
	}

	public SearchResult searchSimilarDocs(WikiDocument doc) {
		return searchSimilarDocs(doc, 10);
	}
	
	public SearchResult searchSimilarDocs(WikiDocument doc, int numResults) {
		return searchSimilarDocs(doc, numResults, 1, 1, 1);
	}
	
	public SearchResult searchSimilarDocs(WikiDocument doc, int numResults, float boostCat, float boostLinks, float boostFreqWords) {
		SearchResult searchResult = null;
		
		BooleanQuery bq = new BooleanQuery();

		// Add the categories to the query
		if(!doc.getCategories().isEmpty()){
			BooleanQuery categories = new BooleanQuery();
			
			for(String cat: doc.getCategories()){
				TermQuery tq = new TermQuery(new Term(Fieldname.CATEGORIES.toString(), cat));
				categories.add(tq, Occur.SHOULD);
			}
			categories.setBoost(boostCat);
			bq.add(categories,Occur.SHOULD);
		}

		// Add the categories to the query
		if(!doc.getLinks().isEmpty()){
			BooleanQuery links = new BooleanQuery();
			
			for(String cat: doc.getLinks()){
				TermQuery tq = new TermQuery(new Term(Fieldname.LINKS.toString(), cat));
				links.add(tq, Occur.SHOULD);
			}
			links.setBoost(boostLinks);
			bq.add(links,Occur.SHOULD);
		}
		
		// Add the frequent words to the query
		if(!doc.getFreqWords().isEmpty()){
			BooleanQuery freqWords = new BooleanQuery();
			
			for(String cat: doc.getFreqWords()){
				TermQuery tq = new TermQuery(new Term(Fieldname.FREQWORDS.toString(), cat));
				freqWords.add(tq, Occur.SHOULD);
			}
			freqWords.setBoost(boostFreqWords);
			bq.add(freqWords,Occur.SHOULD);
		}
		
//		System.out.println("Query: " + bq.toString());

		try {
			searchResult = doPagingSearch(searcher, bq, 2, numResults);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidAttributeValueException e) {
			e.printStackTrace();
		}

		return searchResult;
	}

	private SearchResult executeSearch(String queryString, int startResult, int numberOfResults, String[] fieldnames) {
		SearchResult searchResult = null;

		try {
			QueryParser parser = new MultiFieldQueryParser(fieldnames, Config.analyzer);

			queryString = queryString.trim();

			if (queryString.length() == 0) {
				return null;
			}

			Query query = parser.parse(queryString);
//			System.out.println("Query: " + query.toString());

			searchResult = doPagingSearch(searcher, query, startResult, numberOfResults);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InvalidAttributeValueException e) {
			e.printStackTrace();
		}

		return searchResult;
	}

	/**
	 * This demonstrates a typical paging search scenario, where the search engine presents pages of size n to the user.
	 * The user can then go to the next page if interested in the next hits.
	 * 
	 * When the query is executed for the first time, then only enough results are collected to fill 5 result pages. If
	 * the user wants to page beyond this limit, then the query is executed another time and all hits are collected.
	 * @throws InvalidAttributeValueException 
	 * 
	 */
	private SearchResult doPagingSearch(IndexSearcher searcher, Query query, int offset, int numberOfResults)
			throws IOException, InvalidAttributeValueException {

		int requestedResults = offset + numberOfResults - 1;

		TopDocs results = searcher.search(query, requestedResults);

		int numTotalHits = results.totalHits;

		ScoreDoc[] hits = results.scoreDocs;

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
