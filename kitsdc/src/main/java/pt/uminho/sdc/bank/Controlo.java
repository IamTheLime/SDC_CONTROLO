package pt.uminho.sdc.bank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rui on 28-04-2017.
 */
public class Controlo  implements Control{
    private Map<String,Linha> linhas;
    private String[] nomesL = {"Braga-Porto","Porto-Braga","Lisboa-Porto","Porto-Lisboa"};
    private ArrayList<String> alarmes = new ArrayList<>();
    private int NLinhas = 4;

    Controlo() {
        linhas = new HashMap<String,Linha>();
        for (int i=0; i<NLinhas; i++) {
            Linha l = new Linha(nomesL[i]);
            linhas.put(nomesL[i],l);
        }
    }




    public Map<String, Linha> getLinhas() {
        Map<String,Linha> res = new HashMap<>();
        for(Map.Entry<String, Linha> entry : linhas.entrySet()) {
            String key = entry.getKey();
            Linha value = entry.getValue();
            res.put(key,value.clone());
        }
        return res;
    }

    public void setLinhas(Map<String, Linha> linhas) {
        this.linhas = linhas;
    }

    public String[] getNomesL() {
        String[] res = new String[NLinhas];
        for(int i = 0; i < NLinhas;i++) {
            res[i] = nomesL[i];
        }
        return res;
    }

    public void setNomesL(String[] nomesL) {
        this.nomesL = nomesL;
    }

    public ArrayList<String> getAlarmes() {
        ArrayList<String> res = new ArrayList<>();
        for(String s: alarmes) {
            res.add(s);
        }
        return res;
    }

    public void setAlarmes(ArrayList<String> alarmes) {
        this.alarmes = alarmes;
    }

    public int getId(String L, Composicao C) {
        Linha l = linhas.get(L);
        int res = l.getNewId(C);
        System.out.println(l.toString());
        return res;
    }

    public boolean resEntr(String L,int Seg, int Id){
        Linha l = linhas.get(L);
        System.out.println("resEntr <------" + L + " " + Seg + " " + Id);
        boolean res = l.reserva(Seg,Id);
        System.out.println(l.toString());
        return res;
    }

    public void entrada(String L, int Seg,int Id) {
        Linha l = linhas.get(L);
        System.out.println("Entrada<------" +L + " " + Seg + " " + Id);
        String al = l.setEntrada(Seg,Id);
        //Falta juntar linha ao alarme
        if(al.equalsIgnoreCase("alarme")){
            String aux = new String();
            aux = al + ": " + Id + " " + Seg;
            alarmes.add(aux);
        }
        System.out.println(l.toString());

    }

    public boolean saida(String L, int Seg, int Id) {
        Linha l = linhas.get(L);
        System.out.println("saida<------" + L + " " + Seg + " " + Id);
        boolean res = l.setSaida(Seg,Id);
        //System.out.println("DLSMJNGUASHGKLADNFVKJAJRIOJEQGIOGJRIEQJGFKLSDAJGIOEHGIUOHEQRKIGJARIOGJ\nJKHDIUGHRWAUIGHQEURGHQEUGHdjskhgukashguasdhgioaso\nJKSDHUIJGAJGAOL\n" + res);
        System.out.println(l.toString());
        System.out.println("Saida <--------" + res);
        return res;
    }

    public int getNLinhas() {
        return NLinhas;
    }

    public void setNLinhas(int NLinhas) {
        this.NLinhas = NLinhas;
    }

    public Controlo(Map<String, Linha> linhas, String[] nomesL, ArrayList<String> alarmes, int NLinhas) {
        this.linhas = linhas;
        this.nomesL = nomesL;
        this.alarmes = alarmes;
        this.NLinhas = NLinhas;
    }

    public void setGeral(Controlo c) {
        this.linhas = c.getLinhas();
        this.nomesL = c.getNomesL();
        this.alarmes = c.getAlarmes();
        this.NLinhas = c.getNLinhas();
    }

    @Override
    public Controlo clone() {
        return new Controlo(this.getLinhas(),this.getNomesL(),this.getAlarmes(),this.getNLinhas());
    }
}
