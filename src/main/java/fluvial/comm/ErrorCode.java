package fluvial.comm;

/**
 * Created by superttmm on 10/12/16.
 */
public enum ErrorCode {

    Success(0, "Success"),

    // Communication fail.
    TcpFailException(                              10001, "Tcp connection fail"),
    NoTcpSetupException(                           10002, "No tcp connection setup"),
    IpOrPortNotValidException(                     10003, "Port not valid"),
    TcpCloseFailException(                         10004, "Tcp close fail"),
    TcpResponseTimeoutException(                   10005, "Tcp response timeout"),

    // Agent command fail.
    ProtocolNotSupportedException(                 20011, "Protocol is not supported for connection with robot modules"),
    ProtocolNotSpecifiedException(                 20012, "Protocol is not specified"),
    CommandNotValidException(                      20002, "Command not valid"),
    ActionNotSupportedException(                   20003, "Action not supported"),
    ModuleNotSupportedException(                   20004, "Module not supported"),
    ActionTimeOutException(                        20005, "Action time out"),

    // Common exception.
    NotImplementedException(                       90001, "Not implemented"),
    UndefinedException(                            90002, "Not defined");

    private final int code;
    private final String description;

    private ErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + ":" + description;
    }
}