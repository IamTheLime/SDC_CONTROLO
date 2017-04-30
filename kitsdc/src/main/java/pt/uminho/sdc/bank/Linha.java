package pt.uminho.sdc.bank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by rui on 28-04-2017.
 */
public class Linha implements Serializable {
    private int Nseg;
    private int[] res;
    private int[] seg;
    private String Name;
    private ArrayList<Composicao> comboios;
    private int Id = 0;

    Linha(String N) {
        Name = N;
        Nseg = ThreadLocalRandom.current().nextInt(5000,6000);
        seg = new int[Nseg];
        res = new int[Nseg];
        for(int i = 0; i<Nseg; i++) {
            seg[i] = -1;
            res[i] = -1;
        }
        comboios = new ArrayList<Composicao>();
    }

    public int getNewId (Composicao c) {
        c.setId(Id);
        comboios.ensureCapacity(Id);
        comboios.add(Id,c);
        int res = Id;
        Id++;
        return res;
    }

    public boolean reserva(int S, int Id) {
        Composicao c = comboios.get(Id);
        if (c == null) return false;
        if(S < 0) return false;
        if(S >=  Nseg) return true;
        int s = S + 2;
        if(s > Nseg) s = Nseg;
        for (int i = S; i < s; i++) {
            if (seg[i] != -1 && res[i] != -1) return false;
        }
        s = S - (c.getTamanho() -1) ;
        for (int i = S; i < s && i >= 0; i--) {
            if((seg[i] != Id && res[i] != Id) && (seg[i] != -1 && res[i] != -1)) return false;
        }
        if(c.isJaentrou()) {
            res[S] = Id;
        }
        else {
            System.out.println("reserva " + s + " S " + S);
            for (int i = s; i <= S; i++) {
                res[i] = Id;
            }
        }
        return true;
    }

    public String setEntrada(int S, int Id){
        Composicao c = comboios.get(Id);
        String re=" ";
        if(S >=  Nseg) return re;
        if(res[S]!=Id) {
            re = "alarme!";
        }
        if(seg[S] != -1) {
            re = "alarme!";
        }
        int s;
        if(c.isJaentrou()) {
            s = S - (c.getTamanho());
            res[s] = -1;
            seg[S] = Id;
        }
        else {
            s = S - (c.getTamanho() - 1);
            System.out.println();
            System.out.println("setEntrada <--------" + S + " " + Id + " " + c.getId() + " " + c.getTamanho() + " " + s);
            System.out.println();
            for (int i = 0; i <= S ; i++) {
                System.out.println("Duplicado " + i + " ");
                res[i] = -1;
                seg[i] = Id;
            }
            c.setJaentrou(true);
        }
        return re;
    }

    public boolean setSaida(int S,int Id) {
        Composicao c = comboios.get(Id);
        int li =S - c.getTamanho() ;
        System.out.println("setSaida " + c.getTamanho() + " li " + li + " seg " + Nseg);
        seg[li] = -1;
        res[li] = -1;
        if(li==Nseg - 1) {
            c.setJaentrou(false);
            return true;
        }
        else{
            return false;
        }
    }

    public Linha(int nseg, int[] res, int[] seg, String name, ArrayList<Composicao> comboios, int id) {
        Nseg = nseg;
        this.res = res;
        this.seg = seg;
        Name = name;
        this.comboios = comboios;
        Id = id;
    }

    public ArrayList<Composicao> getComboios() {
        ArrayList<Composicao> res = new ArrayList<>();
        for(Composicao c:comboios) {
            res.add(c.clone());
        }
        return res;
    }

    @Override
    protected Linha clone() {
        return new Linha(Nseg,res,seg,Name,this.getComboios(),Id);
    }

    @Override
    public String toString() {
        return "Linha{" +
                "Nseg=" + Nseg +
                ", res=" + Arrays.toString(res) +
                ", seg=" + Arrays.toString(seg) +
                ", Name='" + Name + '\'' +
                ", comboios=" + comboios +
                ", Id=" + Id +
                '}';
    }
}
