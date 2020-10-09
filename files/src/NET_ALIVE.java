import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;

class NET_ALIVE extends PDU {

    public NET_ALIVE() {
        super(PDU.NET_ALIVE);

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
