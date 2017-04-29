package pt.uminho.sdc.cs;

import spread.SpreadException;

import java.io.IOException;

public abstract class Request<T,V> extends Message {
    private final int order;

    protected Request(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public abstract V apply(T state) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException;
}
