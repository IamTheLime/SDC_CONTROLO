package pt.uminho.sdc.bank;

import pt.uminho.sdc.cs.RemoteInvocationException;
import spread.SpreadException;

import java.io.IOException;

/**
 * Created by rui on 29-04-2017.
 */
public interface Control {

    public int getId(String L,Composicao C) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException;

    public boolean resEntr(String L,int Seg, int Id) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException;

    public void entrada(String L, int Seg,int Id) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException;

    public boolean saida(String L, int Seg, int Id) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException;

}
