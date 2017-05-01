package pt.uminho.sdc.controlo;

import pt.uminho.sdc.cs.RemoteInvocationException;
import pt.uminho.sdc.cs.Request;
import spread.SpreadException;

import java.io.IOException;

/**
 * Created by rui on 01-05-2017.
 */
public class SaidaTotalRequest extends Request<Control, Integer> {
    private final int Id;
    private final String L;

    public SaidaTotalRequest(int id, String l, int order) {
        super(order);
        this.Id = id;
        this.L = l;
    }

    @Override
    public Integer apply(Control state) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        state.saidaTotal(L,Id);
        return 1;
    }
}
