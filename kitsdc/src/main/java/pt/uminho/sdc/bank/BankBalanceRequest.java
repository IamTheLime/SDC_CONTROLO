package pt.uminho.sdc.bank;

import pt.uminho.sdc.cs.Request;
import pt.uminho.sdc.cs.RemoteInvocationException;
import spread.SpreadException;

import java.io.IOException;

public class BankBalanceRequest extends Request<Bank,Integer> {
    public BankBalanceRequest(int order) {
        super(order);
    }

    @Override
    public Integer apply(Bank state) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        return state.getBalance();
    }

    public String toString() {
        return "Balance Request";
    }
}
