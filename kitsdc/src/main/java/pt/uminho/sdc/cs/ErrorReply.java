package pt.uminho.sdc.cs;

public class ErrorReply extends Reply {
    private final RemoteInvocationException exception;

    public ErrorReply(RemoteInvocationException exception) {
        this.exception = exception;
    }

    public RemoteInvocationException getException() {
        return exception;
    }

    public String toString() {
        return "Exception: "+ exception.getMessage();
    }
}
