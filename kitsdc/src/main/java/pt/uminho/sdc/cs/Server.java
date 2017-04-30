package pt.uminho.sdc.cs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.uminho.sdc.bank.BankImpl;
import pt.uminho.sdc.bank.BankOperationRequest;
import pt.uminho.sdc.bank.Controlo;
import spread.*;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by rui on 29-04-2017.
 */
public class Server<T> {

    private static Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private final T state;
    private String server;
    private int port;
    private boolean genesis; // boolean to check if it shoudl wait for a state

    private SpreadConnection connection;
    private SpreadGroup group;

    public Server(int port, String server, T state, boolean genesis) throws IOException, SpreadException {
        this.port = port;
        this.server = server;
        this.genesis = genesis;
        this.state = state;

        byte[] ipAddr = new byte[]{127, 0, 0, 1};
        connection = new SpreadConnection();
        connection.connect(InetAddress.getByAddress(ipAddr),port,"",false,true);

        group = new SpreadGroup();
        group.join(connection,"Controlo");
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
                //logger.info("Received Message {}", message);
                Reply rep = null;
                int msize = 0;
                byte[] buf;
                int order  = 0;
                Message m;
                if(message.isMembership()) {
                    //Codigo de repolicacao em principio nao se mexe
                    MembershipInfo i = message.getMembershipInfo();
                    if(i.isCausedByLeave() || i.isCausedByDisconnect()) return;
                    if(i.isCausedByJoin()) {
                        if(i.getJoined().equals(connection.getPrivateGroup())) {
                            //logger.info("Ups its a message for myself");
                            if(!genesis) {
                                logger.info("Not genesis need to wait for state");
                                logger.info("Waiting for state");
                                ArrayList<SpreadMessage> fifo = new ArrayList<>();
                                SpreadMessage info;
                                while(true) {
                                    logger.info("ESTOU NO CICLO DE PEDIDOS");
                                    //Guardar os pedidos que chegam enquanto o estado não chega
                                    info = connection.receive();
                                    m = Message.fromByteArray(info.getData());
                                    if(m instanceof  Request)
                                        //pedidos que chegaram entretanto
                                        fifo.add(info);
                                    else
                                        //chegou o estado
                                        break;
                                }
                                System.out.print("SLKAJDHGUJIAHDUGIHASDKJVNASJKDGUIASDHGVJKSADUIVBAHDFIUANIFGHDKAJLFNBVJIUADBIUHAFUIHEAU\nSJDAHGSAHGJSADJHGKLJASJHIODGHASPOIGHADSIOFGHADOIU");

                                //preparar para a receção do estado
                                ValueReply<T> s = (ValueReply<T>) ValueReply.fromByteArray(info.getData());
                                msize = (Integer) s.getValue();
                                int bsize = msize;
                                int copiado = 0;
                                int copy = 500;
                                System.out.println("MSIZE " + msize + " BSIZE " + bsize);
                                buf = new byte[bsize];
                                msize = (int) Math.ceil(bsize/500);
                                for(int j = 0; j <= msize; j++){
                                    //receber fragmentos
                                    //Thread.sleep(5000);
                                    info = connection.receive();
                                    s = (ValueReply<T>) ValueReply.fromByteArray(info.getData());
                                    System.out.println("RECECAO " + bsize + " copy " + copy + " copiado " + copiado + " msize " + msize
                                            + " i " + j + " bl " + buf.length + " s.get " + ((byte[]) s.getValue()).length);
                                    //buf = Arrays.copyOf((byte[]) s.getValue(),copy);
                                    System.arraycopy((byte[]) s.getValue(),0,buf, copiado,copy);
                                    bsize = bsize - 500;
                                    copiado += copy;
                                    if(bsize < 500)
                                        copy = bsize;
                                }
                                //replicar o estado que recebeu
                                logger.info("Received State");
                                //O que está guardado no buf é um ValueReply
                                ((Controlo) state).setGeral((Controlo) ((ValueReply) ValueReply.fromByteArray(buf)).getValue());

                                //System.out.println("O Saldo clonado é " + ((BankImpl) state).getBalance());
                                genesis = true;
                                for (int j = 0; j < fifo.size(); j++) {
                                    //Aplicar os pedidos que ficaram pendentes
                                    SpreadMessage temp = fifo.get(j);
                                    m = Message.fromByteArray(temp.getData());
                                    Request<T,?> treq = (Request<T,?>) m;
                                    rep = new ValueReply<>(treq.apply(state), order);
                                    //responder aos clientes que tinham pedidos pendentes
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
                                //mensagem sem significado
                                logger.info("I was able to discard it");
                                return;
                            }
                        }
                        else {
                            System.out.println("AKJHSKUGFHSDUAGVKJLASDGIOAO\nSJKHFOIASDHJGIOASHSKDGFHASID\n ------->A enviar estado <--------\n ,MAJSJFKQHSADUHASODKSGLAS");
                            //Estado a ser replicado está a ser preparado para enviar para quem pediu o estado
                            rep = new ValueReply<>(((Controlo)state).clone());

                            //prepara a mensagem de resposta
                            buf = rep.toByteArray();
                            msize = (int) Math.ceil(buf.length/500);
                            //mandar o número de mensagens que vai receber
                            SpreadGroup group = message.getSender();
                            SpreadMessage resp = new SpreadMessage();
                            resp.addGroup(group);
                            resp.setSafe();
                            rep = new ValueReply<>(buf.length);
                            resp.setData(rep.toByteArray());
                            try {
                                connection.multicast(resp);
                            } catch (SpreadException e) {
                                e.printStackTrace();
                            }
                            //mandar os fragmentos da mensagem
                            for(int j = 0; j <= msize; j++) {
                                group = message.getSender();
                                resp = new SpreadMessage();
                                resp.addGroup(group);
                                resp.setSafe();
                                //Arrays.copyOfRange(oldArray, startIndex, endIndex);
                                if( j == msize ) {
                                    System.out.println("Envio " + ((j) * 500 + (buf.length - (j * 500)) - j * 500));
                                    ValueReply<byte[]> msg = new ValueReply<>(Arrays.copyOfRange(buf, j * 500, ((j) * 500) + (buf.length - (j * 500))));
                                    resp.setData(msg.toByteArray());
                                }
                                else {
                                    System.out.println("Envio " + ((j + 1) * 500 - (j) * 500));
                                    ValueReply<byte[]> msg = new ValueReply<>(Arrays.copyOfRange(buf, j * 500, (j+1) * 500));
                                    resp.setData(msg.toByteArray());
                                }
                                try {
                                    connection.multicast(resp);
                                } catch (SpreadException e) {
                                    e.printStackTrace();
                                }
                            }
                            System.out.println("AKJHSKUGFHSDUAGVKJLASDGIOAO\nSJKHFOIASDHJGIOASHSKDGFHASID\n ------->Enviei estado   <--------\n ,MAJSJFKQHSADUHASODKSGLAS");
                            return;

                        }
                    }
                }
                else {

                    //Logica do programa

                    if(message.getSender().equals(connection.getPrivateGroup()))
                    {
                        //Mensagem dele próprio de confirmação de receção
                        logger.info("Message got discarded");
                        return;
                    }

                    //processamento da mensagem
                    m = Message.fromByteArray(message.getData());
                    if(m instanceof ValueReply) return;
                    //logger.info("client connected: {} {}", message.getSender(),connection.getPrivateGroup());

                    //logger.info("receiving request from message: {}",message);
                    Request<T, ?> req = (Request<T, ?>) m;
                    order  = req.getOrder();
                    logger.debug("received request: {}", req);
                    try {
                        //Isto cria a resposta para o cliente e altera o estado do controlo
                        rep = new ValueReply<>(req.apply(state), order);
                        logger.trace("current state: {}", state);
                    } catch (RemoteInvocationException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        logger.warn("unexpected application exception", e);
                        rep = new ErrorReply(new ServerSideException(e));
                    }
                }
                //logger.debug("sending reply: {}", rep);

                //prepara a mensagem de resposta
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



