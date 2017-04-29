package pt.uminho.sdc.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.uminho.sdc.cs.RemoteInvocationException;
import spread.SpreadException;

import java.io.IOException;
import java.util.Random;

public class BankTester {
    private static Logger logger = LoggerFactory.getLogger(BankTester.class);

    private final BankSupplier supplier;
    private final long time;
    private final Random random;
    private Worker[] worker;
    private Stage stage = Stage.Warmup;
    private int nops;
    private long totalrtt;
    private int totalop;

    @FunctionalInterface
    public interface BankSupplier {
        Bank get() throws Exception;
    }

    public BankTester(BankSupplier supplier, int nthr, long seconds) {
        this.worker = new Worker[nthr];
        this.supplier = supplier;
        this.time = seconds*1000;
        this.random = new Random();
        logger.info("testing bank implementation: threads = {}, seconds = {}", nthr, seconds);
    }

    public BankTester(BankSupplier supplier, String[] args) {
        int nthr = 1;
        if (args.length >= 1)
            nthr = Integer.parseInt(args[0]);
        this.worker = new Worker[nthr];
        this.supplier = supplier;
        int seconds = 10;
        if (args.length >= 2)
            seconds = Integer.parseInt(args[1]);
        this.time = seconds*1000;
        this.random = new Random();
        logger.info("testing bank implementation: threads = {}, seconds = {}", nthr, seconds);
    }

    public void test() throws InterruptedException {
        int initial;

        Bank bank;

        try {
            bank = supplier.get();
            logger.debug("connected to bank");

            initial = bank.getBalance();
        } catch(Exception e) {
            logger.error("cannot get initial balance: test aborted", e);
            return;
        }

        for(int i=0; i<worker.length; i++)
            worker[i] = new Worker();
        for(int i=0; i<worker.length; i++)
            worker[i].start();

        if (!waitInStage(Stage.Warmup, time/10)) {
            logger.error("test aborted during warmup");
        }

        long before = System.nanoTime();

        logger.info("warmup complete: running!");

        setStage(Stage.Run);

        if (!waitInStage(Stage.Run, time)) {
            logger.error("test aborted during measurement");
        }

        setStage(Stage.Shutdown);

        long after = System.nanoTime();

        logger.info("complete: shutting down");

        for(int i=0; i<worker.length; i++)
            worker[i].join();

        if (stage != Stage.Shutdown) {
            logger.error("test aborted");
            return;
        }

        logger.info("performance: {} ops, {} ops/s, {} s", nops, nops/((after-before)/1e9d), (totalrtt/1e9d)/nops);

        int result = 0;
        try {
            result = bank.getBalance();
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
            logger.error("test FAILED: final balance does not match operations");
    }

    private static enum Stage { Warmup, Run, Shutdown, Error };

    private synchronized void setStage(Stage stage) {
        if (stage.compareTo(this.stage) <= 0)
            return;
        this.stage = stage;
    }

    private synchronized boolean waitInStage(Stage stage, long time) throws InterruptedException {
        long now = System.currentTimeMillis();
        long target = now + time;
        while(this.stage == stage && now < target) {
            wait(target-now);
            now = System.currentTimeMillis();
        }
        return this.stage == stage;
    }

    private synchronized void log(long delta, int op) {
        totalop+=op;

        if (stage != Stage.Run)
            return;

        nops++;
        totalrtt+=delta;
    }

    private synchronized boolean isRunning() {
        return stage.compareTo(Stage.Run) <= 0;
    }

    private class Worker extends Thread {
        public void run() {
            try {
                Bank bank = supplier.get();

                logger.debug("worker connected to bank");

                while (isRunning()) {
                    int op = 0;
                    long before = System.nanoTime();
                    if (random.nextFloat() < .1) {
                        bank.getBalance();
                    } else {
                        op = random.nextInt(100) - 60;
                        if (!bank.operation(op))
                            op = 0;
                    }
                    long after = System.nanoTime();
                    log(after - before, op);
                }
            } catch(Exception e) {
                logger.error("worker stopping on exception", e);
                setStage(Stage.Error);
            }
        }
    }
}
