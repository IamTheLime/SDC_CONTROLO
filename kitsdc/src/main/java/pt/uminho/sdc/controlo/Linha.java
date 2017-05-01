package pt.uminho.sdc.controlo;

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
        Nseg = ThreadLocalRandom.current().nextInt(10000,20000);
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
        if(s > Nseg) s = Nseg - 1;
        for (int i = S ; i <= s; i++) {
            //System.out.print("i " + i + " res " + res[i] + " seg " + seg[i]);
            if (seg[i] != -1 || res[i] != -1) return false;
        }
        System.out.println();
        s = S - (c.getTamanho() -1) - 2;

        if(s < 0) s = 0;

        for (int i = s; i < S; i++) {
            //System.out.print("i " + i + " res " + res[i] + " seg " + seg[i] + " s " + s);
            if((seg[i] != Id && res[i] != Id) && (seg[i] != -1 || res[i] != -1)) return false;
        }
        //System.out.println();
        s = S - (c.getTamanho() -1);
        if(c.isJaentrou()) {
            //System.out.println("reserva " + s + " S " + S);
            res[S] = Id;
        }
        else {
            //System.out.println("reserva " + s + " S " + S);
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
            s = S - (c.getTamanho() - 1);
            res[s] = -1;
            res[S] = -1;
            seg[S] = Id;
        }
        else {
            s = S - (c.getTamanho() - 1);
            //System.out.println();
            //System.out.println("setEntrada <--------" + S + " " + Id + " " + c.getId() + " " + c.getTamanho() + " " + s);
           // System.out.println();
            for (int i = s; i <= S ; i++) {
                //System.out.println("Duplicado " + i + " ");
                res[i] = -1;
                seg[i] = Id;
            }
            c.setJaentrou(true);
        }
        //System.out.println("Entrada 525 " + re);
        return re;
    }

    public boolean setSaida(int S,int Id) {
        Composicao c = comboios.get(Id);
        int li =S - c.getTamanho() ;
        //System.out.println("setSaida " + c.getTamanho() + " li " + li + " seg " + Nseg);
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


    public void saidaTotal(int Id) {
        for (int i = 0; i < Nseg; i++) {
            if(res[i] == Id)
                res[i] = -1;
            if(seg[i] == Id)
                seg[i] = -1;
        }
    }

    @Override
    protected Linha clone() {
        return new Linha(Nseg,res,seg,Name,this.getComboios(),Id);
    }

    @Override
    public String toString() {
        return "Linha{" +
                ", res=" + Arrays.toString(res) +
                ", seg=" + Arrays.toString(seg) +
                '}';
    }
}
