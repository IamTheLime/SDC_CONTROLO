package pt.uminho.sdc.controlo;

import pt.uminho.sdc.cs.RemoteInvocationException;
import pt.uminho.sdc.cs.Request;
import spread.SpreadException;

import java.io.IOException;

/**
 * Created by rui on 29-04-2017.
 */
public class IdRequest extends Request<Control,Integer> {
    private Composicao C;
    private String L;

    protected IdRequest(int order, Composicao c, String l) {
        super(order);
        this.C = c;
        L = l;
    }


    @Override
    public Integer apply(Control state) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        return state.getId(L,C);
    }
}
