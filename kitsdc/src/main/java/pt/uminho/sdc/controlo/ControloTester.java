package pt.uminho.sdc.controlo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by rui on 29-04-2017.
 */
public class ControloTester {
    private static Logger logger = LoggerFactory.getLogger(ControloTester.class);

    private final ControloTester.ControlSupplier supplier;
    private final long time;
    private final Random random;
    private ControloTester.Worker[] worker;
    private ControloTester.Stage stage = ControloTester.Stage.Warmup;
    private int nops;
    private long totalrtt;
    private int totalop;

    @FunctionalInterface
    public interface ControlSupplier {
        Control get() throws Exception;
    }

    public ControloTester(ControloTester.ControlSupplier supplier, int nthr, long seconds) {
        this.worker = new ControloTester.Worker[nthr];
        this.supplier = supplier;
        this.time = seconds*1000;
        this.random = new Random();
        logger.info("testing Control implementation: threads = {}, seconds = {}", nthr, seconds);
    }

    public ControloTester(ControloTester.ControlSupplier supplier, String[] args) {
        int nthr = 1;
        if (args.length >= 1)
            nthr = Integer.parseInt(args[0]);
        this.worker = new ControloTester.Worker[nthr];
        this.supplier = supplier;
        int seconds = 10;
        if (args.length >= 2)
            seconds = Integer.parseInt(args[1]);
        this.time = seconds*1000;
        this.random = new Random();
        logger.info("testing Control implementation: threads = {}, seconds = {}", nthr, seconds);
    }

    public void test() throws InterruptedException {
        int initial;

        Control Control;

        for(int i=0; i<worker.length; i++)
            worker[i] = new ControloTester.Worker();
        for(int i=0; i<worker.length; i++)
            worker[i].start();

        if (!waitInStage(ControloTester.Stage.Warmup, time/10)) {
            logger.error("test aborted during warmup");
        }

        long before = System.nanoTime();

        logger.info("warmup complete: running!");

        setStage(ControloTester.Stage.Run);

        if (!waitInStage(ControloTester.Stage.Run, time)) {
            logger.error("test aborted during measurement");
        }

        setStage(ControloTester.Stage.Shutdown);

        long after = System.nanoTime();

        logger.info("complete: shutting down");

        for(int i=0; i<worker.length; i++)
            worker[i].join();

        if (stage != ControloTester.Stage.Shutdown) {
            logger.error("test aborted");
            return;
        }

        logger.info("performance: {} ops, {} ops/s, {} s", nops, nops/((after-before)/1e9d), (totalrtt/1e9d)/nops);
    }

    private static enum Stage { Warmup, Run, Shutdown, Error };

    private synchronized void setStage(ControloTester.Stage stage) {
        if (stage.compareTo(this.stage) <= 0)
            return;
        this.stage = stage;
    }

    private synchronized boolean waitInStage(ControloTester.Stage stage, long time) throws InterruptedException {
        long now = System.currentTimeMillis();
        long target = now + time;
        while(this.stage == stage && now < target) {
            wait(target-now);
            now = System.currentTimeMillis();
        }
        return this.stage == stage;
    }

    private synchronized void log(long delta) {

        if (stage != ControloTester.Stage.Run)
            return;

        nops++;
        totalrtt+=delta;
    }

    private synchronized boolean isRunning() {
        return stage.compareTo(ControloTester.Stage.Run) <= 0;
    }

    private class Worker extends Thread {
        private final String[] nomesL = {"Braga-Porto","Porto-Braga","Lisboa-Porto","Porto-Lisboa"};
        private String linha;
        public void run() {
            try {
                waitInStage(Stage.Warmup,200);
                Control Control = supplier.get();
                Composicao c = new Composicao();
                String out;
                int aux =  ThreadLocalRandom.current().nextInt(0,3);
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
                    switch (aux) {
                        case 0:
                            //Entrada na linha
                            while(!res) {
                                res = Control.resEntr(linha,seg,c.getId());
                                Thread.sleep(ThreadLocalRandom.current().nextInt(10,20));
                            }
                            res = false;
                            aux = 1;
                            Control.entrada(linha,seg,c.getId());
                            seg++;
                            break;
                        case 1:
                            //Continuar na linha
                            while(!res) {
                                res = Control.resEntr(linha,seg,c.getId());
                                Thread.sleep(ThreadLocalRandom.current().nextInt(10,20));
                            }
                            res = false;
                            out = Control.entrada(linha,seg,c.getId());
                            logger.info("Entrada: {}" , out);
                            acabou = Control.saida(linha,seg,c.getId());
                            if(acabou){
                                aux = 3;
                                logger.info("Final da linha {}" + linha);
                            }
                            seg++;
                            break;
                        case 3:
                            break;
                    }
                    long after = System.nanoTime();
                    log(after - before);
                }
                Control.saidaTotal(linha,c.getId());
            } catch(Exception e) {
                logger.error("worker stopping on exception", e);
                setStage(ControloTester.Stage.Error);
            }
        }
    }
}
