package pt.uminho.sdc.bank;

import pt.uminho.sdc.cs.Request;
import pt.uminho.sdc.cs.RemoteInvocationException;
import spread.SpreadException;

import java.io.IOException;

public class BankOperationRequest extends Request<Bank,Boolean> {
    private final int value;

    public BankOperationRequest(int value, int order) {
        super(order);
        this.value = value;
    }

    @Override
    public Boolean apply(Bank state) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        return state.operation(value);
    }

    public String toString() {
        return "Operation Request: value = "+value;
    }
}
