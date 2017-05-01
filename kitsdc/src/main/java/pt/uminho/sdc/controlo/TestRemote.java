package pt.uminho.sdc.controlo;

import pt.uminho.sdc.cs.SocketClient;

public class TestRemote {
    public static void main(String[] args) throws Exception {
        new ControloTester(
                () -> new ControloClient(
                    new SocketClient<Control>("localhost",/*Integer.parseInt(args[0])*/ 4803,"Controlo")
                ), args)
            .test();
    }
}
