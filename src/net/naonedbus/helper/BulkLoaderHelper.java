/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe gérer les fichiers de PreparedStatement.
 * 
 * @author romain
 * 
 */
public class BulkLoaderHelper {

	private static final String COMMENT_TOKEN = "#";
	private static final char CHAR_PARAM_SPLIT = ';';

	private final InputStream inputStream;
	private final List<BulkQuery> bulkQueries;

	public BulkLoaderHelper(final InputStream inputStream) {
		this.inputStream = inputStream;
		bulkQueries = new ArrayList<BulkLoaderHelper.BulkQuery>();
	}

	public List<BulkQuery> getQueries() throws IOException {
		if (bulkQueries.isEmpty()) {
			load();
		}
		return bulkQueries;
	}

	private void load() throws IOException {

		final BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));

		BulkQuery bulkQuery = null;
		boolean changePattern = true;
		String line;

		while ((line = buffer.readLine()) != null) {
			if (line.trim().length() == 0) {
				changePattern = true;
				continue;
			}
			if (line.startsWith(COMMENT_TOKEN)) {
				continue;
			}

			if (changePattern) {
				changePattern = false;
				if (bulkQuery != null) {
					bulkQueries.add(bulkQuery);
				}
				bulkQuery = new BulkQuery(line);
			} else {
				bulkQuery.addValues(fastSplit(line, CHAR_PARAM_SPLIT));
			}

		}

		buffer.close();

		if (bulkQuery != null) {
			bulkQueries.add(bulkQuery);
		}
	}

	/**
	 * Découper une chaîne selon un caractère.
	 * 
	 * @param line
	 *            La chaîne à découper
	 * @param split
	 *            Le caractère de séparation
	 * @return La chaîne découpée sous forme de tableau
	 */
	private static String[] fastSplit(final String line, final char split) {
		final String[] temp = new String[line.length() / 2];
		int wordCount = 0;
		int i = 0;
		int j = line.indexOf(split); // First substring
		while (j >= 0) {
			temp[wordCount++] = line.substring(i, j);
			i = j + 1;
			j = line.indexOf(split, i); // Rest of substrings
		}
		temp[wordCount++] = line.substring(i); // Last substring
		final String[] result = new String[wordCount];
		System.arraycopy(temp, 0, result, 0, wordCount);
		return result;
	}

	/**
	 * Classe définissant une requête.
	 * 
	 * @author romain
	 * 
	 */
	public static class BulkQuery {
		private static final char paramChar = '?';

		private String pattern;
		private List<String[]> values;
		private int paramCount = 0;

		public BulkQuery(final String pattern) {
			this.values = new ArrayList<String[]>();
			setPattern(pattern);
		}

		public String getPattern() {
			return pattern;
		}

		public void setPattern(final String pattern) {
			this.pattern = pattern;
			this.paramCount = countOccurrences(pattern, paramChar, 0);
		}

		public void addValues(final String[] values) {
			if (values.length != paramCount) {
				throw new IllegalArgumentException("Nombre de paramètres incorrect : " + values.length + " au lieu de "
						+ paramCount + " attendus. [" + Arrays.toString(values) + "]");
			}

			this.values.add(values);
		}

		public List<String[]> getValues() {
			return values;
		}

		public void setValues(final List<String[]> values) {
			this.values = values;
		}

		private static int countOccurrences(final String haystack, final char needle, final int index) {
			if (index >= haystack.length()) {
				return 0;
			}

			final int contribution = haystack.charAt(index) == needle ? 1 : 0;
			return contribution + countOccurrences(haystack, needle, index + 1);
		}
	}
}
