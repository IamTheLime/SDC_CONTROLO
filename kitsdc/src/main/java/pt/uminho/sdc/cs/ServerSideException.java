package pt.uminho.sdc.cs;

public class ServerSideException extends RemoteInvocationException {
    public ServerSideException(Exception nested) {
        super(nested);
    }
}
