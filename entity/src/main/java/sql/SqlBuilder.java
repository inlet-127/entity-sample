package sql;

/**
 * SQLビルダー
 *
 * @author KeisukeUrakawa
 *
 */
public class SqlBuilder extends SqlBuilderBase<SqlBuilder> {

	@Override
	public SqlBuilder build() {
		if (super.sql.lastIndexOf(" ") == sql.length() - 1) {
			super.sql.delete(sql.length() - 1, sql.length());
		}
		super.sql.append(";");
		return this;
	}

	@Override
	protected SqlBuilder self() {
		return this;
	}

}
