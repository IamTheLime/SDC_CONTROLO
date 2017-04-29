package pt.uminho.sdc.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankImpl implements Bank {

    private static Logger logger = LoggerFactory.getLogger(BankImpl.class);

    int balance = 0;

    @Override
    public synchronized boolean operation(int value) {
        if (value < 0 && value+balance < 0) {
            logger.trace("operation = {} failed", value);
            return false;
        }
        balance += value;
        logger.trace("operation = {} done, balance = {}", value, balance);
        return true;
    }

    @Override
    public synchronized int getBalance() {
        logger.trace("balance = {}", balance);
        return balance;
    }

    public synchronized void setBalance(int balance) {
        logger.trace("Getting Balance");
        this.balance = balance;
    }

    public synchronized String toString() {
        return "bank balance = "+balance;
    }
}
