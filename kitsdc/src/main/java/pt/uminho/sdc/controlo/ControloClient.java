package pt.uminho.sdc.controlo;

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
    public String entrada(String L, int Seg, int Id) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        Boolean b = client.request(new EntradaRequest(Id,Seg,L,order++));
        if(b)
            return "ERROR alarme: Linha -> " + L + ", Seguemento -> " + Seg + ", Composicao -> " + Id;
        else
            return "Entrou na linha sem problemas";
    }

    @Override
    public boolean saida(String L, int Seg, int Id) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        return client.request(new SaidaRequest(Id,Seg,L,order++));
    }

    @Override
    public void saidaTotal(String L, int Id) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        client.request(new SaidaTotalRequest(Id,L,order++));
    }
}
