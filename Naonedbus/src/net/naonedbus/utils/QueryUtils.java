package net.naonedbus.utils;

import java.util.List;

public abstract class QueryUtils {
	private QueryUtils() {
	};

	/**
	 * Transformer une liste d'object en statement "in" d'une requête SQL. <br />
	 * Par exemple, la liste {@code [1,2,3]} se transforme en "(1,2,3)".
	 * 
	 * @param items
	 *            Les éléments
	 * @return Une chaîne représentant les éléments pouvant être utilisée dans
	 *         un IN de requête SQL.
	 */
	public static String listToInStatement(List<?> items) {
		final StringBuilder sb = new StringBuilder("(");
		final String sep = ",";
		for (int i = 0; i < items.size(); i++) {
			sb.append(items.get(i));
			if (i < items.size() - 1)
				sb.append(sep);
		}
		sb.append(")").toString();

		return sb.toString();
	}
}
