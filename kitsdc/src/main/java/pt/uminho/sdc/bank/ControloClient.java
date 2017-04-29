package pt.uminho.sdc.bank;

/**
 * Created by rui on 29-04-2017.
 */

import pt.uminho.sdc.cs.Client;
import pt.uminho.sdc.cs.RemoteInvocationException;
import spread.SpreadException;

import java.io.IOException;

public class ControloClient implements Control {

    private final Client<Control> client;
    private int order = 0;

    public ControloClient(Client<Control> client) {
        this.client = client;
    }

    @Override
    public int getId(String L, Composicao C) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        return client.request(new IdRequest(order++, C, L));
    }

    @Override
    public boolean resEntr(String L, int Seg, int Id) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        return client.request(new ReservaEntradaRequest(Id,L,order++,Seg));
    }

    @Override
    public void entrada(String L, int Seg, int Id) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        client.request(new EntradaRequest(Id,Seg,L,order++));
    }

    @Override
    public boolean saida(String L, int Seg, int Id) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        return client.request(new SaidaRequest(Id,Seg,L,order++));
    }
}
