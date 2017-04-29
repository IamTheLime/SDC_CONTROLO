package pt.uminho.sdc.bank;

import pt.uminho.sdc.cs.SocketClient;

import java.io.IOException;

public class TestRemote {
    public static void main(String[] args) throws Exception {
        new ControloTester(
                () -> new ControloClient(
                    new SocketClient<Control>("localhost",/*Integer.parseInt(args[0])*/ 4803,"Controlo")
                ), args)
            .test();
    }
}
