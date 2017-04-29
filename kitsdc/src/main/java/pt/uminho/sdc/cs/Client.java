package pt.uminho.sdc.cs;

import spread.SpreadException;

import java.io.IOException;
import java.net.UnknownHostException;

public interface Client<T> {
    <V> V request(Request<T,V> req) throws RemoteInvocationException, IOException, SpreadException, ClassNotFoundException;
}
