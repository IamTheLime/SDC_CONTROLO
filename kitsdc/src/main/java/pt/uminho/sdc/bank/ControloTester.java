package pt.uminho.sdc.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.uminho.sdc.cs.RemoteInvocationException;
import spread.SpreadException;

import java.io.IOException;
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

        /*try {
            Control = supplier.get();
            logger.debug("connected to Control");

            initial = Control.getBalance();
        } catch(Exception e) {
            logger.error("cannot get initial balance: test aborted", e);
            return;
        }*/

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

        /*int result = 0;
        try {
            result = Control.getBalance();
        } catch(RemoteInvocationException e) {
            logger.error("cannot get final balance: test aborted", e);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SpreadException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (initial+totalop == result)
            logger.info("test PASSED: final balance matches operations");
        else
            logger.error("test FAILED: final balance does not match operations");*/
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
                Control Control = supplier.get();
                Composicao c = new Composicao(ThreadLocalRandom.current().nextInt(1,3),0);
                int aux = ThreadLocalRandom.current().nextInt(0,3);
                boolean res = false;
                int seg = c.getTamanho() - 1;
                linha = nomesL[1];
                boolean acabou = false;

                logger.debug("worker connected to Control");

                aux = 0;

                aux = Control.getId(linha,c);
                c.setId(aux);
                aux = 1;
                while(isRunning() && !acabou) {
                    long before = System.nanoTime();
                    switch (aux) {
                        case 0:
                            //Entrada
                            while(!res) {
                                res = Control.resEntr(linha,seg,c.getId());
                                System.out.println("Yolo " + res);
                            }
                            aux = 1;
                            Control.entrada(linha,seg,c.getId());
                            System.out.println("Entrada");
                            seg++;
                            break;
                        case 1:
                            while(!res) {
                                res = Control.resEntr(linha,seg,c.getId());
                                System.out.println("Yolo " + res);
                            }
                            aux = 2;
                            Control.entrada(linha,seg,c.getId());
                            aux = 3;
                            System.out.println("Entrada");
                            //Thread.sleep(2000);
                            acabou = Control.saida(linha,seg,c.getId());
                            System.out.println("Saida");
                            if(acabou) System.out.println("Acabou a linha");
                            seg++;
                            aux = 1;
                            break;
                    }
                    long after = System.nanoTime();
                    log(after - before);
                }

                /*while (isRunning()) {
                    int op = 0;
                    long before = System.nanoTime();
                    if (random.nextFloat() < .1) {
                        Control.getBalance();
                    } else {
                        op = random.nextInt(100) - 60;
                        if (!Control.operation(op))
                            op = 0;
                    }
                    long after = System.nanoTime();
                    log(after - before, op);
                }*/
            } catch(Exception e) {
                logger.error("worker stopping on exception", e);
                setStage(ControloTester.Stage.Error);
            }
        }
    }
}
