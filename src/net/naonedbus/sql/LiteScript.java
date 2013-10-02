package net.naonedbus.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * LiteScript is a very simple script parser for SQL files. <br />
 * It provides conditional block that allow you to executes queries depending on
 * the database old and new versions.
 * 
 * @author Romain Guefveneu
 * @version 1.0
 */
public class LiteScript {
	private static final String COMMENT_TOKEN = "--";
	private static final String CONDITION_TOKEN = "IF";
	private static final String BLOCK_END_TOKEN = "END";
	private static final String QUERY_END_TOKEN = ";";
	private static final String OLD_VERSION_TOKEN = "OLD_VERSION";
	private static final String NEW_VERSION_TOKEN = "NEW_VERSION";
	private static final String GREATER_THAN = ">";
	private static final String LESSER_THAN = "<";
	private static final String EQUALS = "=";

	final StringBuilder mStringBuilder;
	private int mOldVersion;
	private int mNewVersion;

	public LiteScript() {
		mStringBuilder = new StringBuilder();
	}

	/**
	 * @param newVersion
	 *            The new version of the database
	 */
	public void setOldVersion(final int oldVersion) {
		mOldVersion = oldVersion;
	}

	/**
	 * @param newVersion
	 *            The new version of the database
	 */
	public void setNewVersion(final int newVersion) {
		mNewVersion = newVersion;
	}

	/**
	 * Get queries that matches the corresponding versions.
	 * 
	 * @param inputStream
	 *            the stream to read
	 * @return a list of sql queries
	 * @throws IOException
	 */
	public List<String> getQueries(final InputStream inputStream) throws IOException {
		final BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
		final List<String> queries = new ArrayList<String>();
		String line;

		while ((line = buffer.readLine()) != null) {
			line = line.trim();
			readLine(buffer, line, queries);
		}
		buffer.close();

		return queries;
	}

	/**
	 * Read a line. <br />
	 * If the line is not a complete sql query (i.e. not ending with a
	 * semi-colon), it will be added to {@link #mStringBuilder}.
	 * 
	 * @param buffer
	 *            the script buffer
	 * @param line
	 *            the line to read
	 * @param queries
	 *            the list of previous queries
	 * @throws IOException
	 */
	private void readLine(final BufferedReader buffer, final String line, final List<String> queries)
			throws IOException {
		if (line.length() == 0)
			return;
		if (line.startsWith(COMMENT_TOKEN))
			return;

		if (line.startsWith(CONDITION_TOKEN)) {
			readConditionalBlock(buffer, line, queries);
		} else if (!line.endsWith(QUERY_END_TOKEN)) {
			mStringBuilder.append(line).append(' ');
		} else {
			if (mStringBuilder.length() > 0) {
				mStringBuilder.append(line);
				queries.add(mStringBuilder.toString());
			} else {
				queries.add(line);
			}
			mStringBuilder.setLength(0);
		}

	}

	/**
	 * Read a conditional block. <br />
	 * If the condition is {@code true}, the block queries will be added to the
	 * query list.
	 * 
	 * @param buffer
	 *            the script buffer
	 * @param condition
	 *            the condition line to evaluate
	 * @param queries
	 *            the list of previous queries
	 * @throws IOException
	 */
	private void readConditionalBlock(final BufferedReader buffer, final String condition, final List<String> queries)
			throws IOException {
		String line;
		final boolean isTrue = eval(condition);
		while ((line = buffer.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;
			if (line.startsWith(BLOCK_END_TOKEN))
				break;

			if (isTrue)
				readLine(buffer, line, queries);
		}
	}

	/**
	 * Evaluate an expression. <br />
	 * The expression must folow this pattern :
	 * 
	 * <pre>
	 * IF [OLD_VERSION|NEW_VERSION] [>|<|=] [0-9*]
	 * </pre>
	 * 
	 * @param expression
	 *            the expression to evaluate
	 * @return {@code true} if the expression is true, {@code false} otherwise.
	 */
	private boolean eval(final String expression) {

		final String[] tokens = fastSplit(expression, ' ');
		final String token = tokens[1];
		final String operator = tokens[2];
		final int compareTo = Integer.valueOf(tokens[3]);
		final int compareFrom;

		if (OLD_VERSION_TOKEN.equals(token))
			compareFrom = mOldVersion;
		else if (NEW_VERSION_TOKEN.equals(token))
			compareFrom = mNewVersion;
		else
			compareFrom = compareTo;

		if (GREATER_THAN.equals(operator))
			return compareFrom > compareTo;
		else if (EQUALS.equals(operator))
			return compareFrom == compareTo;
		else if (LESSER_THAN.equals(operator))
			return compareFrom < compareTo;
		else
			return false;

	}

	/**
	 * Split a string.
	 * 
	 * @param string
	 *            The string to split
	 * @param delimiter
	 *            the delimiting char
	 * @return the array of strings computed by splitting this string around
	 *         matches of the given regular expression
	 */
	private static String[] fastSplit(final String string, final char delimiter) {
		final String[] temp = new String[string.length() / 2];
		int wordCount = 0;
		int i = 0;
		int j = string.indexOf(delimiter); // First substring
		while (j >= 0) {
			temp[wordCount++] = string.substring(i, j);
			i = j + 1;
			j = string.indexOf(delimiter, i); // Rest of substrings
		}
		temp[wordCount++] = string.substring(i); // Last substring
		final String[] result = new String[wordCount];
		System.arraycopy(temp, 0, result, 0, wordCount);
		return result;
	}
}
