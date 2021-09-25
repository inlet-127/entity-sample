package identifier;

/**
 * データベース処理で扱うプロパティキーを定義した列挙型
 *
 * @author KeisukeUrakawa
 *
 */
public enum DbProperty {

	SERVER("jdbc.url"), USER("db.user"), PASSWORD("db.password"), MAX_POOL_SIZE("database.maximumPoolSize"),
	DRIVER_CLASS_NAME("jdbc.driverClassName");

	DbProperty(String key) {
		this.key = key;
	}

	private String key;

	public String getKey() {
		return key;
	}

}
