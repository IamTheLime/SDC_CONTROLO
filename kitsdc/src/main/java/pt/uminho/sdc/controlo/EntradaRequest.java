package pt.uminho.sdc.controlo;

import pt.uminho.sdc.cs.RemoteInvocationException;
import pt.uminho.sdc.cs.Request;
import spread.SpreadException;

import java.io.IOException;

/**
 * Created by rui on 29-04-2017.
 */
public class EntradaRequest extends Request<Control,Boolean> {
    private final int Id;
    private final int seg;
    private final String L;

    public EntradaRequest(int id, int seg, String l, int order) {
        super(order);
        this.Id = id;
        this.seg = seg;
        this.L = l;
    }

    @Override
    public Boolean apply(Control state) throws RemoteInvocationException, SpreadException, IOException, ClassNotFoundException {
        String res = state.entrada(L,seg,Id);
        if(res.contains("alarme!"))
            return true;
        else
            return false;
    }
}
