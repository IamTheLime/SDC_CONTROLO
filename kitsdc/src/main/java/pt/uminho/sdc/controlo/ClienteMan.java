package pt.uminho.sdc.controlo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.uminho.sdc.cs.SocketClient;
import spread.SpreadException;

import java.io.*;
import java.util.Random;

/**
 * Created by rui on 01-05-2017.
 */
public class ClienteMan {
    private static Logger logger = LoggerFactory.getLogger(ClienteMan.class);

    private final ClienteMan.ControlSupplier supplier;
    private final long time = 0;
    private final Random random;
    private ClienteMan.Worker[] worker;
    private ClienteMan.Stage stage = ClienteMan.Stage.Warmup;
    private int nops;
    private long totalrtt;
    private int totalop;

    public static void main(String [] args) throws IOException, SpreadException {
        ClienteMan.Worker t = new ClienteMan(() -> new ControloClient(
                new SocketClient<Control>("localhost",4803,"Controlo")
        )).new Worker();
        t.start();
    }



    private synchronized void setStage(ClienteMan.Stage stage) {
        if (stage.compareTo(this.stage) <= 0)
            return;
        this.stage = stage;
    }

    private synchronized boolean waitInStage(ClienteMan.Stage stage, long time) throws InterruptedException {
        long now = System.currentTimeMillis();
        long target = now + time;
        while(this.stage == stage && now < target) {
            wait(target-now);
            now = System.currentTimeMillis();
        }
        return this.stage == stage;
    }

    private synchronized void log(long delta) {

        if (stage != ClienteMan.Stage.Run)
            return;

        nops++;
        totalrtt+=delta;
    }

    private static enum Stage { Warmup, Run, Shutdown, Error };
    
    @FunctionalInterface
    public interface ControlSupplier {
        Control get() throws Exception;
    }

    public ClienteMan(ClienteMan.ControlSupplier supplier) {
        this.supplier = supplier;
        this.random = new Random();
    }

    private synchronized boolean isRunning() {
        return stage.compareTo(ClienteMan.Stage.Run) <= 0;
    }

    private class Worker extends Thread {
        private final String[] nomesL = {"Braga-Porto","Porto-Braga","Lisboa-Porto","Porto-Lisboa"};
        private String linha;
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                BufferedWriter ou = new BufferedWriter(new OutputStreamWriter(System.out));
                while(!isRunning()) {}
                Control Control = supplier.get();
                Composicao c = new Composicao();
                String out;
                int aux =  0;//ThreadLocalRandom.current().nextInt(0,3);
                boolean res = false;
                int seg = c.getTamanho() - 1;
                linha = nomesL[aux];
                boolean acabou = false;

                logger.debug("worker connected to Control");

                aux = Control.getId(linha,c);
                c.setId(aux);
                aux = 0;
                while(isRunning()) {
                    long before = System.nanoTime();
                    String input = null;
                    input = in.readLine();
                    String[] tok = input.split(" ");
                    aux = Integer.parseInt(tok[0]);
                    switch (aux) {
                        case 0:
                            //Entrada
                            seg = Integer.parseInt(tok[1]);
                            //Entrada na linha
                            res = Control.resEntr(linha,seg,c.getId());
                            if(res)
                                logger.info("A reserva no segmento " + seg + " da linha " + linha + " foi efetuada com sucesso!");
                            else
                                logger.info("A reserva no segmento " + seg + " da linha " + linha + " falhou!");
                            if(res) {
                                res = false;
                                aux = 1;
                                out = Control.entrada(linha, seg, c.getId());
                                logger.info("Entrada: {}", out);
                                seg++;
                            }
                            break;
                        case 1:
                            //Movimento
                            seg = Integer.parseInt(tok[1]);
                            //Continuar na linha
                            res = Control.resEntr(linha,seg,c.getId());
                            if(res)
                                logger.info("A reserva no segmento {} da linha {} foi efetuada com sucesso!",seg,linha);
                            else
                                logger.info("A reserva no segmento {} da linha {} falhou!",seg,linha);
                            if(res) {
                                res = false;
                                out = Control.entrada(linha, seg, c.getId());
                                logger.info("Entrada: {}", out);
                                acabou = Control.saida(linha, seg, c.getId());
                                if (acabou) {
                                    logger.info("A linha {} acabou", linha);
                                }
                                seg++;
                            }
                            break;
                        case 2:
                            //Erro forçado
                            seg = Integer.parseInt(tok[1]);
                            out = Control.entrada(linha,seg,c.getId());
                            logger.info("ERROR: {}" , out);
                            acabou = Control.saida(linha,seg,c.getId());
                            if(acabou){
                                logger.info("A linha {} acabou", linha);
                            }
                            seg++;
                            break;
                        case -1:
                            setStage(Stage.Shutdown);
                            break;
                    }
                    long after = System.nanoTime();
                    log(after - before);
                }
                Control.saidaTotal(linha,c.getId());
            } catch(Exception e) {
                logger.error("worker stopping on exception", e);
                setStage(ClienteMan.Stage.Error);
            }
        }
    }
    
}
