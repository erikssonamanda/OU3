import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;

class NET_GET_NODE_RESPONSE extends PDU{
    private String address;
    private int port;

    private Convert convert;

    public NET_GET_NODE_RESPONSE(ByteBuffer buf, ByteChannel src){
        super(PDU.NET_GET_NODE_RESPONSE);
        int a = buf.getInt();
        short p = buf.getShort();
        address = convert.addressToString(a);
        port = Short.toUnsignedInt(p);
    }


    @Override
    public void send(ByteChannel channel) {

    }

    @Override
    public void sendTo(DatagramChannel channel, InetSocketAddress r) throws IOException {

    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
