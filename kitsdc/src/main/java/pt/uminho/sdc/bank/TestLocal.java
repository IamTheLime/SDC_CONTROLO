package pt.uminho.sdc.bank;

import java.io.IOException;

public class TestLocal {
    public static void main(String[] args) throws Exception {
        Bank b = new BankImpl();
        new BankTester(() -> b, args).test();
    }
}
