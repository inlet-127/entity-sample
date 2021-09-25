package datasource;

import java.io.Serializable;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * JNDIでHikariDataSourceを利用するためのクラス
 * 
 * @author KeisukeUrakawa
 *
 */
public class HikariDataSourceForJNDI extends HikariDataSource implements Serializable {

	private static final long serialVersionUID = 1L;

	public HikariDataSourceForJNDI(HikariConfig config) {
		super(config);
	}

}
