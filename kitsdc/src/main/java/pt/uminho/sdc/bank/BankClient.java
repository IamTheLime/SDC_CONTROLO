package pt.uminho.sdc.bank;

import pt.uminho.sdc.cs.Client;
import pt.uminho.sdc.cs.RemoteInvocationException;
import spread.SpreadException;

import java.io.IOException;

public class BankClient implements Bank {

    private final Client<Bank> client;
    private int order = 0;

    public BankClient(Client<Bank> client) {
        this.client = client;
    }

    @Override
    public boolean operation(int value) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        return client.request(new BankOperationRequest(value, order++));
    }

    @Override
    public int getBalance() throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        return client.request(new BankBalanceRequest(order++));
    }
}
