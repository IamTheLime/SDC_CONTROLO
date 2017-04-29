package pt.uminho.sdc.cs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

public abstract class Message implements Serializable {

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this);
        return baos.toByteArray();
    }

    public ByteBuffer toByteBuffer() throws IOException {
        return ByteBuffer.wrap(toByteArray());
    }

    public static Message fromByteArray(byte[] array) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (Message)ois.readObject();
    }

    public static Message fromByteBuffer(ByteBuffer buffer) throws IOException, ClassNotFoundException {
        byte[] array = new byte[buffer.remaining()];
        buffer.get(array);
        return fromByteArray(array);
    }
}
