package cc.oit.exception;

/**
 * 表示数据提交失败。
 *
 * Created by Chanedi
 */
public class DataCommitException extends RuntimeException {

	private static final long serialVersionUID = 1669843560642800254L;

	public DataCommitException() {
		super("Data commit failed!");
	}

}
