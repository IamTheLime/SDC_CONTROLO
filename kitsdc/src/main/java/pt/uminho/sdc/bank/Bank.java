package pt.uminho.sdc.bank;

import pt.uminho.sdc.cs.RemoteInvocationException;
import spread.SpreadException;

import java.io.IOException;

public interface Bank {
    boolean operation(int value) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException;
    int getBalance() throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException;
}
