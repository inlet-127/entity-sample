package datasource;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;

import identifier.DbProperty;

/**
 * データソースファクトリ
 *
 * @author KeisukeUrakawa
 *
 */
public class DataSourceFactory {

	private static DataSource dataSource;
	private static final String PROPERTY_FILE = "../database.properties";
	public static String JNDI_DB_IDENTIFIER;

	private DataSourceFactory() {
		// singleton
	}

	/**
	 * データソースを取得する。
	 *
	 * @return データソース
	 *
	 */
	public static DataSource getDataSource() {
		if (dataSource == null) {
			dataSource = new HikariDataSourceForJNDI(createHikariConfig());
		}
		return dataSource;
	}

	/**
	 * HikariCPの初期設定
	 *
	 * @return HikariCPの設定情報
	 *
	 */
	private static HikariConfig createHikariConfig() {
		HikariConfig conf = new HikariConfig();
		Properties dbProperty = new Properties();
		try {
			dbProperty.load(DataSourceFactory.class.getResourceAsStream(PROPERTY_FILE));
		} catch (IOException e) {
			// 設計不良
			throw new AssertionError("プロパティファイルの読み込みに失敗しました");
		}
		String url = dbProperty.getProperty(DbProperty.SERVER.getKey());
		String[] parsedUrl = url.split(":");
		JNDI_DB_IDENTIFIER = parsedUrl[0] + "/" + parsedUrl[1];

		conf.setDriverClassName(dbProperty.getProperty(DbProperty.DRIVER_CLASS_NAME.getKey()));
		conf.setJdbcUrl(url);
		conf.setUsername(dbProperty.getProperty(DbProperty.USER.getKey()));
		conf.setPassword(dbProperty.getProperty(DbProperty.PASSWORD.getKey()));
		conf.setMaximumPoolSize(Integer.parseInt(dbProperty.getProperty(DbProperty.MAX_POOL_SIZE.getKey())));
		return conf;
	}
}
