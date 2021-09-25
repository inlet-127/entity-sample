package sql;

/**
 * SQLビルダーの基幹クラス
 *
 * @author KeisukeUrakawa
 *
 * @param <T> SqlBuilderBaseを継承するクラス自身
 */
public abstract class SqlBuilderBase<T extends SqlBuilderBase<T>> {
	private enum SQL_IDENTIFIER {
		SELECT, UPDATE, INSERT, DELETE, TRANCATE, DROP, MODIFY, ADD, VALUES, SET, FROM, WHERE, INNER, OUTER, JOIN, ON,
		ORDER_BY, UNION, DISTINCT, LIKE, BETWEEN, AND, OR, IN, NOT, INTO, ALTER
	}

	protected final StringBuilder sql = new StringBuilder();

	public T addCommand(SQL_IDENTIFIER identifier) {
		addCommand(identifier.name());
		return self();
	}

	public T addCommand(String command) {
		sql.append(command);
		sql.append(" ");
		return self();
	}

	public T select(String... columns) {
		addCommand(SQL_IDENTIFIER.SELECT);
		columnsAppend(columns);
		return self();
	}

	public T where(String conditions) {
		addCommand(SQL_IDENTIFIER.WHERE);
		addCommand(conditions);
		return self();
	}

	public T from(String table) {
		addCommand(SQL_IDENTIFIER.FROM);
		addCommand(table);
		return self();
	}

	private void columnsAppend(String... columns) {
		for (int i = 0; i < columns.length; i++) {
			addCommand(columns[i]);
			if (i != columns.length - 1) {
				addCommand(",");
			}
		}
	}

	abstract SqlBuilderBase<T> build();

	protected abstract T self();

	public String getQuery() {
		return this.sql.toString();
	}

}
