package pt.uminho.sdc.cs;

public class ClientSideException extends RemoteInvocationException {
    public ClientSideException(Exception nested) {
        super(nested);
    }
}
