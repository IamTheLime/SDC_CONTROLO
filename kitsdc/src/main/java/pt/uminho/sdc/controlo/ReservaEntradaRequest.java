package pt.uminho.sdc.controlo;

import pt.uminho.sdc.cs.RemoteInvocationException;
import pt.uminho.sdc.cs.Request;
import spread.SpreadException;

import java.io.IOException;

/**
 * Created by rui on 29-04-2017.
 */
public class ReservaEntradaRequest extends Request<Control,Boolean> {
    private final int Id;
    private final int seg;
    private final String L;

    public ReservaEntradaRequest(int id, String l, int order, int seg) {
        super(order);
        this.Id = id;
        this.L = l;
        this.seg = seg;
    }

    @Override
    public Boolean apply(Control state) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        return state.resEntr(L,seg,Id);
    }
}
