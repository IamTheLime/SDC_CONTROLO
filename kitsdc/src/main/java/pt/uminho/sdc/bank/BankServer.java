package pt.uminho.sdc.bank;

import pt.uminho.sdc.cs.Server;
import pt.uminho.sdc.cs.SocketServer;
import spread.SpreadException;

import java.io.IOException;


public class BankServer {
    public static void main(String[] args) throws IOException, SpreadException {
        boolean genesis = Boolean.parseBoolean(args[0]); // To tell the server if it should wait for a state
        new Server<>(4803,"localhost", new Controlo(), genesis).serve();
    }
}
