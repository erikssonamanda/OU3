import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;

class NET_GET_NODE extends PDU{

    public NET_GET_NODE(){
        super(PDU.NET_GET_NODE);
    }

    @Override
    public void send(ByteChannel channel) {

    }

    private ByteBuffer getBuffer() {
        var buf = ByteBuffer.allocate(1);

        buf.put((byte) type);
        buf.flip();

        return buf;
    }

    @Override
    public void sendTo(DatagramChannel channel, InetSocketAddress r) throws IOException {
        channel.send(getBuffer(), r);
    }

}
