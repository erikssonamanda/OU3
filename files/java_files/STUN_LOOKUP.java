import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.IOException; 
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

class STUN_LOOKUP extends PDU {

    public STUN_LOOKUP() {
        super(PDU.STUN_LOOKUP);
    }

    @Override
    public void send(ByteChannel channel) throws IOException{
    }

    private ByteBuffer getBuffer() {
        var buf = ByteBuffer.allocate(1);

        buf.put((byte)type);
        buf.flip();

        return buf;

    }
    @Override
    public void sendTo(DatagramChannel channel, InetSocketAddress r) throws IOException {
        channel.send(getBuffer(), r);
    }
}
