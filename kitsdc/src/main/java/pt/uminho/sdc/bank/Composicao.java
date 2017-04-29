package pt.uminho.sdc.bank;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by rui on 28-04-2017.
 */
public class Composicao implements Serializable {
    private int tamanho;
    private int Id;
    private boolean jaentrou;
    Composicao() {
        tamanho = ThreadLocalRandom.current().nextInt(1,3);
        Id=0;
        jaentrou = false;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }


    public int getTamanho() {
        return tamanho;
    }

    public Composicao(int tamanho, int id) {
        this.tamanho = tamanho;
        this.Id = id;
    }

    @Override
    protected Composicao clone() {
        return new Composicao(tamanho,Id);
    }

    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }

    public boolean isJaentrou() {
        return jaentrou;
    }

    public void setJaentrou(boolean jaentrou) {
        this.jaentrou = jaentrou;
    }
}
