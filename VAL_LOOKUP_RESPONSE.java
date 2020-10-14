import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;

public class VAL_LOOKUP_RESPONSE extends PDU {
    private String ssn;
    private int nameLength;
    private String name;
    private int emailLength;
    private String email;

    public VAL_LOOKUP_RESPONSE(String ssn, int nameLength, String name, int emailLength, String email){
        super(PDU.VAL_LOOKUP_RESPONSE);

        this.ssn = ssn;
        this.nameLength = nameLength;
        this.name = name;
        this.emailLength = emailLength;
        this.email = email;
    }

    public VAL_LOOKUP_RESPONSE(ByteBuffer buf, ByteChannel src) {
        super(PDU.VAL_LOOKUP_RESPONSE);

        this.ssn = getSSN(buf);

        byte nl = buf.get();
        this.nameLength = Byte.toUnsignedInt(nl);
        this.name = getNameEmail(buf, nameLength);

        byte el = buf.get();
        emailLength = Byte.toUnsignedInt(el);
        this.email = getNameEmail(buf, emailLength);

    }

    private ByteBuffer getBuffer() {
        var buf = ByteBuffer.allocate(15 + nameLength + emailLength);

        buf.put((byte)type);
        buf.put(ssn.getBytes());
        buf.put((byte)nameLength);
        buf.put(name.getBytes());
        buf.put((byte)emailLength);
        buf.put(email.getBytes());
        buf.flip();

        return buf;
    }

    @Override
    public void send(ByteChannel channel) throws IOException {

    }

    @Override
    public void sendTo(DatagramChannel channel, InetSocketAddress r) throws IOException {
        channel.send(getBuffer(), r);
    }

    private String getSSN(ByteBuffer buf) {
        byte[] byteArray = new byte[12];
        for (int i = 0; i < 12; i++) {
            byteArray[i] = buf.get();
        }

        return new String(byteArray);
    }

    private String getNameEmail(ByteBuffer buf, int stringLength) {
        byte[] byteArray = new byte[stringLength];
        for (int i = 0; i < stringLength; i++) {
            byteArray[i] = buf.get();
        }

        return new String(byteArray);
    }
}
