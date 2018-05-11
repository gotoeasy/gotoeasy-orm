package top.gotoeasy.framework.orm.exception;

/**
 * ROM模块异常
 * 
 * @since 2018/05
 * @author 青松
 */
public class OrmException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造方法
     * 
     * @param message 消息
     */
    public OrmException(String message) {
        super(message);
    }

    /**
     * 构造方法
     * 
     * @param cause 异常
     */
    public OrmException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造方法
     * 
     * @param message 消息
     * @param cause 异常
     */
    public OrmException(String message, Throwable cause) {
        super(message, cause);
    }

}
