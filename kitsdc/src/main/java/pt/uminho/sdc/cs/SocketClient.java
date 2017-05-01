package pt.uminho.sdc.cs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.io.IOException;
import java.net.InetAddress;

public class SocketClient<T> implements Client<T> {
    private static Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private String server;
    private int port;
    private SpreadConnection connection;
    private SpreadGroup group;

    public SocketClient(String server, int port, String group) throws IOException, SpreadException {
        this.server = server;
        this.port = port;
        connection = new SpreadConnection();
        byte[] ipAddr = new byte[]{127, 0, 0, 1};

        logger.debug("Client connecting to: {}:{}",server,port);
        connection.connect(/*InetAddress.getByName(server)*/ InetAddress.getByAddress(ipAddr), port, "",false,false);
        logger.info("Client connected to: {}:{}:{}",server,port,connection.getPrivateGroup());

        logger.debug("Creating group: {}",group);
        this.group = new SpreadGroup();
        logger.debug("Creating joining: {}",group);
    }

    public <V> V request(Request<T,V> req) throws RemoteInvocationException, IOException, SpreadException, ClassNotFoundException {

        SpreadMessage m = new SpreadMessage();

        try {
            m.setData(req.toByteArray());
            m.setSafe();
            m.addGroup("Controlo");
            if (connection != null && m != null){
                connection.multicast(m);
            }
        } catch (IOException e) {
                e.printStackTrace();
        }
        catch(NullPointerException e) {
            e.printStackTrace();
            return null;
        }

        SpreadMessage reply = null;
        ValueReply<V> resp;
        while(true) {
            reply = connection.receive();
            resp = ((ValueReply<V>) ValueReply.fromByteArray(reply.getData()));
            if(resp.getOrder()==req.getOrder())
                return resp.getValue();
        }
    }

    public void close() throws IOException {
        return;
    }
}
