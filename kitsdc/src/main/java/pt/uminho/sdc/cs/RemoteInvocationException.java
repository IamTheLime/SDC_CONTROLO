package pt.uminho.sdc.cs;

public class RemoteInvocationException extends Exception {
    public RemoteInvocationException(Exception nested) {
        super(nested);
    }
}
