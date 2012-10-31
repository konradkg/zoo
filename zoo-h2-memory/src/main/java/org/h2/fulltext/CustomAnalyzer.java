package org.h2.fulltext;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

public class CustomAnalyzer extends StopwordAnalyzerBase {

	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	/**
	 * Specifies whether deprecated acronyms should be replaced with HOST type.
	 * See {@linkplain "https://issues.apache.org/jira/browse/LUCENE-1068"}
	 */
	private final boolean replaceInvalidAcronym;

	/**
	 * An unmodifiable set containing some common English words that are usually
	 * not useful for searching.
	 */
	public static final Set<?> STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

	public CustomAnalyzer(Version matchVersion, Set<?> stopWords) {
		super(matchVersion, stopWords);
		replaceInvalidAcronym = matchVersion.onOrAfter(Version.LUCENE_24);
	}

	public CustomAnalyzer(Version matchVersion) {
		this(matchVersion, STOP_WORDS_SET);
	}


	@Deprecated
	public CustomAnalyzer(Version matchVersion, File stopwords) throws IOException {
		this(matchVersion, WordlistLoader.getWordSet(IOUtils.getDecodingReader(stopwords,
				IOUtils.CHARSET_UTF_8), matchVersion));
	}

	/**
	 * Builds an analyzer with the stop words from the given reader.
	 * 
	 * @see WordlistLoader#getWordSet(Reader, Version)
	 * @param matchVersion
	 *            Lucene version to match See
	 *            {@link <a href="#version">above</a>}
	 * @param stopwords
	 *            Reader to read stop words from
	 */
	public CustomAnalyzer(Version matchVersion, Reader stopwords) throws IOException {
		this(matchVersion, WordlistLoader.getWordSet(stopwords, matchVersion));
	}

	/**
	 * Set maximum allowed token length. If a token is seen that exceeds this
	 * length then it is discarded. This setting only takes effect the next time
	 * tokenStream or reusableTokenStream is called.
	 */
	public void setMaxTokenLength(int length) {
		maxTokenLength = length;
	}

	/**
	 * @see #setMaxTokenLength
	 */
	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	@Override
	protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
		final StandardTokenizer src = new StandardTokenizer(matchVersion, reader);
		src.setMaxTokenLength(maxTokenLength);
		src.setReplaceInvalidAcronym(replaceInvalidAcronym);
		TokenStream tok = new StandardFilter(matchVersion, src);
		tok = new LowerCaseFilter(matchVersion, tok);
		tok = new StopFilter(matchVersion, tok, stopwords);
		tok = new ASCIIFoldingFilter(tok);
		return new TokenStreamComponents(src, tok) {
			@Override
			protected boolean reset(final Reader reader) throws IOException {
				src.setMaxTokenLength(CustomAnalyzer.this.maxTokenLength);
				return super.reset(reader);
			}
		};
	}

}
