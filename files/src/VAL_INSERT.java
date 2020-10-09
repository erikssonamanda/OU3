import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;

class VAL_INSERT extends PDU{
    private String ssn;
    private int nameLength;
    private String name;
    private int emailLength;
    private String email;

    public VAL_INSERT(ByteBuffer buf, ByteChannel src){
        super(PDU.VAL_INSERT);
        int s = buf.getInt();


        this.ssn = ssn;
        this.name = name;
        this.email = email;
    }

    @Override
    public void send(ByteChannel channel) throws IOException {

    }

    @Override
    public void sendTo(DatagramChannel channel, InetSocketAddress r) throws IOException {

    }
}
