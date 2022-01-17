package dao;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import datasource.DataSourceFactory;
import entity.BaseEntity;
import sql.SqlBuilder;

/**
 * ORマッピングとデータベースアクセスに対する基幹機能を提供するDAO
 *
 * @author KeisukeUrakawa
 *
 * @param <T> エンティティクラス
 */
public abstract class BaseDao<T extends BaseEntity> {
	private static final InitialContext context;
	private Map<String, Class<?>> mapper;
	/**
	 * クラス初期化時にJNDIにデータソースを登録
	 */
	static {
		try {
			// jndi.propertiesで定義するとSpringBootで本プロジェクトを使用する際,
			// 起動時間が著しく増加したため, 一旦, ここで明示的にプロパティ値をセットすることにする.
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
			System.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
			DataSource dataSource = DataSourceFactory.getDataSource();
			context = new InitialContext();
			context.createSubcontext("jdbc");
			context.bind(DataSourceFactory.JNDI_DB_IDENTIFIER, dataSource);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	protected BaseDao() {
		mapper = createMapper();
	}

	/**
	 * DAOが指定するエンティティクラスを動的に生成する。
	 *
	 * @return エンティティクラスのインスタンス
	 */
	protected final T createEntity() {
		try {
			return (T) getEntityType().getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * エンティティクラスを取得する。
	 *
	 * @return エンティティクラス
	 */
	@SuppressWarnings("unchecked")
	protected final Class<T> getEntityType() {
		Class<?> clazz = this.getClass();
		Type type = clazz.getGenericSuperclass();
		ParameterizedType pt = (ParameterizedType) type;
		Type[] bindTypes = pt.getActualTypeArguments();
		return (Class<T>) bindTypes[0];
	}

	/**
	 * DBコネクションを確立する。
	 *
	 * @return DBコネクション
	 * @throws SQLException
	 * @throws NamingException
	 */
	private Connection createConnection() throws SQLException {
		return lookupDataSource().getConnection();
	}

	/**
	 * SQLを実行する
	 *
	 * @param sql
	 * @return レコード
	 * @throws SQLException SQLの実行に失敗したとき
	 */
	public List<T> exec_query(SqlBuilder sql) throws SQLException {
		try (Connection con = createConnection(); PreparedStatement ps = con.prepareStatement(sql.getQuery());) {
			return getRecord(ps.executeQuery());
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * JNDIリソースルックアップでデータソースを取得する。
	 *
	 * @return データソース
	 */
	private DataSource lookupDataSource() {
		DataSource ds;
		try {
			ds = (DataSource) context.lookup(DataSourceFactory.JNDI_DB_IDENTIFIER);
		} catch (NamingException e) {
			throw new RuntimeException("JNDIリソースが見つかりません。");
		}
		return Objects.requireNonNull(ds);
	}

	/**
	 * レコードとEntityクラスをマッピングするためのオブジェクトを作成する。
	 *
	 * @param entity エンティティクラス
	 * @return レコードとEntityクラスをマッピング情報
	 */
	private Map<String, Class<?>> createMapper() {
		Map<String, Class<?>> mapper = new HashMap<String, Class<?>>();
		Class<T> clazz = getEntityType();
		for (Field f : clazz.getDeclaredFields()) {
			f.setAccessible(true);
			mapper.put(f.getName(), f.getType());
			f.setAccessible(false);
		}
		return Collections.unmodifiableMap(mapper);
	}

	/**
	 * レコードを取得する。
	 *
	 * @param dbResult ResultSetの生データ
	 * @param entity   エンティティクラスのインスタンス
	 * @return レコード
	 * @throws SQLException ResultSetからのレコード取得に失敗したとき
	 */
	private List<T> getRecord(ResultSet dbResult) throws SQLException {
		final String setterIdentifier = "set";
		final String getterIdentifier = "get";
		List<T> result = new ArrayList<>();
		ResultSetMetaData rsmd = dbResult.getMetaData();
		try {
			while (dbResult.next()) {
				T entity = createEntity();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String getterMethodName = getterIdentifier
							+ replaseResultSetMethod(mapper.get(rsmd.getColumnName(i)).getSimpleName());
					String setterMethodName = setterIdentifier + convertToMethodName(rsmd.getColumnName(i));
					Method getter = dbResult.getClass().getMethod(getterMethodName, String.class);
					Method setter = entity.getClass().getMethod(setterMethodName, mapper.get(rsmd.getColumnName(i)));

					// 取得したレコードをエンティティクラスに格納
					setter.invoke(entity, getter.invoke(dbResult, rsmd.getColumnName(i)));
				}
				result.add(entity);
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * メソッド名の命名規則に合わせて変換する
	 *
	 * @param val 変換対象の文字列
	 * @return メソッド名
	 */
	private static String convertToMethodName(String val) {
		char[] arr = val.toCharArray();
		arr[0] = Character.toUpperCase(arr[0]);
		return new String(arr);
	}

	/**
	 * エンティティクラスのフィールドの型名をResultSetのメソッド名にあわせて変換する
	 *
	 * @param simpleClassName 簡易クラス名称
	 * @return 対応するResultSetのメソッド名称
	 */
	private static String replaseResultSetMethod(String simpleClassName) {
		simpleClassName = simpleClassName.replace("[]", "s");
		simpleClassName = simpleClassName.replace("Integer", "Int");
		return convertToMethodName(simpleClassName);
	}
}
