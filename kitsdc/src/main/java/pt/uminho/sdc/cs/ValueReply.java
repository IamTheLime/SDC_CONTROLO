package pt.uminho.sdc.cs;

public class ValueReply<T> extends Reply {
    private final T value;
    private final int order;

    public ValueReply(T value) {
        this.value = value;
        this.order = -1;
    }

    public ValueReply(T value, int order) {
        this.value = value;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public T getValue() {
        return value;
    }

    public String toString() {
        return "Value: "+value;
    }
}
