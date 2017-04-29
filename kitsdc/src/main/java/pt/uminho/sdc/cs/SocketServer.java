package pt.uminho.sdc.cs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.uminho.sdc.bank.BankImpl;
import pt.uminho.sdc.bank.BankOperationRequest;
import spread.*;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class SocketServer<T> {

    private static Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private final T state;
    private String server;
    private int port;
    private boolean genesis; // boolean to check if it shoudl wait for a state

    private SpreadConnection connection;
    private SpreadGroup group;

    public SocketServer(int port, String server, T state, boolean genesis) throws IOException, SpreadException {
        this.port = port;
        this.server = server;
        this.genesis = genesis;
        this.state = state;

        byte[] ipAddr = new byte[]{127, 0, 0, 1};
        connection = new SpreadConnection();
        connection.connect(InetAddress.getByAddress(ipAddr),port,"",false,true);

        group = new SpreadGroup();
        group.join(connection,"bank");
    }

    public void serve() throws IOException, SpreadException {
        logger.info("server starting at port {}", port);
        try {
            while (true) {
                SpreadMessage m = connection.receive();
                new Worker(m).run();
            }
        } finally {
            logger.info("server at port {} stopped", port);
        }
    }

    private class Worker {
        private SpreadMessage message;

        public Worker(SpreadMessage message) {
            this.message= message;
        }

        public void run() {
            try {
                logger.info("Received Message {}", message);
                Reply rep = null;
                int order  = 0;
                Message m;
                if(message.isMembership()) {
                    MembershipInfo i = message.getMembershipInfo();
                    if(i.isCausedByLeave() || i.isCausedByDisconnect()) return;
                    if(i.isCausedByJoin()) {
                        if(i.getJoined().equals(connection.getPrivateGroup())) {
                            logger.info("Ups its a message for myself");
                            if(!genesis) {
                                logger.info("Not genesis need to wait for state");
                                logger.debug("Waiting for state");
                                ArrayList<SpreadMessage> fifo = new ArrayList<>();
                                SpreadMessage info;
                                while(true) {
                                    info = connection.receive();
                                    m = Message.fromByteArray(info.getData());
                                    if(m instanceof  Request)
                                        fifo.add(info);
                                    else
                                        break;
                                }
                                logger.debug("Received State");
                                ValueReply<T> s = (ValueReply<T>) ValueReply.fromByteArray(info.getData());
                                ((BankImpl) state).setBalance((Integer) s.getValue());
                                System.out.println("O Saldo clonado é " + ((BankImpl) state).getBalance());
                                genesis = true;
                                for (int j = 0; j < fifo.size(); j++) {
                                    SpreadMessage temp = fifo.get(j);
                                    m = Message.fromByteArray(temp.getData());
                                    Request<T,?> treq = (Request<T,?>) m;
                                    rep = new ValueReply<>(treq.apply(state), order);
                                    SpreadGroup group = temp.getSender();
                                    SpreadMessage resp = new SpreadMessage();
                                    resp.addGroup(group);
                                    resp.setSafe();
                                    resp.setData(rep.toByteArray());
                                    try {
                                        connection.multicast(resp);
                                    } catch (SpreadException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return;
                            }
                            else{
                                logger.info("I was able to discard it");
                                return;
                            }
                        }
                        else {
                            System.out.println("O saldo enviado foi " + ((BankImpl) state).getBalance());
                            rep = new ValueReply<>(new Integer(((BankImpl) state).getBalance()));
                        }
                    }
                }
                else {
                    if(message.getSender().equals(connection.getPrivateGroup()))
                    {
                        logger.info("Message got discarded");
                        return;
                    }
                    m = Message.fromByteArray(message.getData());
                    if(m instanceof ValueReply) return;
                    logger.info("client connected: {} {}", message.getSender(),connection.getPrivateGroup());

                    logger.info("receiving request from message: {}",message);
                    Request<T, ?> req = (Request<T, ?>) m;
                    order  = req.getOrder();
                    logger.debug("received request: {}", req);
                    try {
                        rep = new ValueReply<>(req.apply(state), order);
                        logger.trace("current state: {}", state);
                    } catch (RemoteInvocationException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        logger.warn("unexpected application exception", e);
                        rep = new ErrorReply(new ServerSideException(e));
                    }
                }
                logger.debug("sending reply: {}", rep);

                SpreadGroup group = message.getSender();
                SpreadMessage resp = new SpreadMessage();
                resp.addGroup(group);
                resp.setSafe();
                resp.setData(rep.toByteArray());
                try {
                    connection.multicast(resp);
                } catch (SpreadException e) {
                    e.printStackTrace();
                }


            } catch(EOFException e) {
                logger.info("client disconnected: {}", message);
            } catch (IOException | ClassNotFoundException e) {
                logger.error("error reading request, closing connection", e);
            } catch (SpreadException e) {
                e.printStackTrace();
            } catch (RemoteInvocationException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() throws IOException {
        logger.info("closing server at port {}", port);
    }
}
