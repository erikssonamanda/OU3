import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

abstract class PDU {
    int type;
    
    public PDU(int type) {
        this.type = type;
    }

    public abstract void send(ByteChannel channel) throws IOException;
    public abstract void sendTo(DatagramChannel channel, InetSocketAddress r) throws IOException;

    public static PDU create(ByteBuffer buffer, ByteChannel src) {
        int t = Byte.toUnsignedInt(buffer.get());
        PDU p = null;
        switch(t) {
            case STUN_RESPONSE:
                p = new STUN_RESPONSE(buffer, src);
                break;
        }
        
        return p;
    }

    public static final int NET_ALIVE = 0;
    public static final int NET_GET_NODE = 1;
    public static final int NET_GET_NODE_RESPONSE = 2;
    public static final int NET_JOIN = 3;
    public static final int NET_JOIN_RESPONSE = 4;
    public static final int NET_CLOSE_CONNECTION = 5;
    public static final int NET_NEW_RANGE = 6;
    public static final int NET_LEAVING = 7;
    public static final int NET_NEW_RANGE_RESPONSE = 8;
    public static final int VAL_INSERT = 100;
    public static final int VAL_REMOVE = 101;
    public static final int VAL_LOOKUP = 102;
    public static final int VAL_LOOKUP_RESPONSE = 103;
    public static final int STUN_LOOKUP  = 200;
    public static final int STUN_RESPONSE  = 201;
}
