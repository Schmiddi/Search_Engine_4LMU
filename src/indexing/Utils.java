package indexing;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class Utils {
	
	public static String wiki2text(String wikitext){	
		/*
		 * Remove category tags
		 */
		wikitext = wikitext.replaceAll("\\[\\[Category:[^\\]]*\\]\\]", "");
		String text = Jsoup.parse(ParseWikiToHTMLUtility.parseMediaWiki(wikitext)).text();
		
		/*
		 * <ref> </ref> <ref .... /> are not removed, so they must be removed manually
		 */
		text = text.replaceAll("</ref>|<ref[^>]*>", "");
		/*
		 * In the info box the <br /> are not removed
		 */
		text = text.replaceAll("<br[^>]*>", " ");
		/*
		 * The info box contains control sequences of the style |some_name= 
		 * they should also be remove as they are no blank text
		 */
		text = text.replaceAll("\\|[ ]?(\\w)*[ ]?=", "");
		/*
		 * Remove urls of the style |http....
		 */
		text = text.replaceAll("\\|http[^ ]*", "");
		/*
		 * Remove {{DEFAULTSORT:....}}
		 */
		text = text.replaceAll("\\{\\{DEFAULTSORT[^\\}]*\\}\\}", "");
		/*
		 * Remove }} and {{
		 */
		text = text.replaceAll("\\{\\{|\\}\\}", "");
		return text;
	}
	
	public static String wiki2html(String wikitext){		
		return ParseWikiToHTMLUtility.parseMediaWiki(wikitext);
	}
	
	public static String getMostFrequentWords(String wikitext, int count){
		String text = wiki2text(wikitext);

		Attribute input = new Attribute("tweet", (List<String>) null);

		ArrayList<Attribute> inputVec = new ArrayList<Attribute>();
		inputVec.add(input);

		Instances insts = new Instances("wikitext", inputVec, 1);
		insts.add(new DenseInstance(1));
		insts.get(0).setValue(0, text);

		StringToWordVector filter = new StringToWordVector();
		filter.setLowerCaseTokens(true);
		filter.setMinTermFreq(1);
		filter.setUseStoplist(true);
		filter.setOutputWordCounts(true);
		filter.setWordsToKeep(count);
		
		try {
			filter.setInputFormat(insts);
			
			Instances dataFiltered = Filter.useFilter(insts, filter);
			
			String words = "";
			for(int i=0; i < dataFiltered.get(0).numAttributes(); i++){
				words += dataFiltered.get(0).attribute(i).name() + " ";
			}
			return words;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
