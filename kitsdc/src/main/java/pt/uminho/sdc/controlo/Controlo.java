package pt.uminho.sdc.controlo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rui on 28-04-2017.
 */
public class Controlo  implements Control, Serializable{
    private static Logger logger = LoggerFactory.getLogger(ClienteMan.class);
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
        return res;
    }

    public boolean resEntr(String L,int Seg, int Id) {
        Linha l = linhas.get(L);
        boolean res = l.reserva(Seg, Id);
        if (!res)
            logger.info("O pedido de reserva de entrada no segmento {} da linha {} pela composicao {} falhou!",Seg,L,Id);
        else
            logger.info("O pedido de reserva de entrada no segmento {} da linha {} pela composicao {} foi executado com sucesso!",Seg,L,Id);
        return res;
    }

    public String entrada(String L, int Seg,int Id) {
        Linha l = linhas.get(L);
        String al = l.setEntrada(Seg,Id);
        if(al.equalsIgnoreCase("alarme!")){
            String aux;
            aux = "Entrada indevida"+ ": Composicao" + Id + " Segmento " + Seg + " Linha " + L ;
            logger.info("ALARME {}", aux);
            alarmes.add(aux);
        }
        else
            logger.info("Entrada com sucesso no segmento {} na linha {} da composicao {}",Seg,L,Id);
        return al;
    }

    public boolean saida(String L, int Seg, int Id) {
        Linha l = linhas.get(L);
        boolean res = l.setSaida(Seg,Id);
        if(res)
            logger.info("O comboio {} chegou ao fim da linha",Id,L);
        else
            logger.info("Saida da composicao {} do segmento {} da linha {} efetuado falhou",Id,Seg,L);
        return res;
    }

    public void saidaTotal(String L, int Id) {
        Linha l = linhas.get(L);
        l.saidaTotal(Id);
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

    public static Controlo fromByteArray(byte[] array) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (Controlo)ois.readObject();
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this);
        return baos.toByteArray();
    }

    @Override
    public String toString() {
        return "Controlo{" +
                "linhas=" + linhas +
                ", alarmes=" + alarmes +
                '}';
    }
}
