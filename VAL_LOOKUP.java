import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;

public class VAL_LOOKUP extends PDU{
    private String ssn;
    private String senderAddress;
    private int senderPort;


    public VAL_LOOKUP(ByteBuffer buf, ByteChannel src) {
        super(PDU.VAL_LOOKUP);

        this.ssn = getSSN(buf);

        int a = buf.getInt();
        short p = buf.getShort();
        senderAddress = Convert.addressToString(a);
        senderPort = Short.toUnsignedInt(p);
    }

    private ByteBuffer getBuffer() {
        var buf = ByteBuffer.allocate(19);

        buf.put((byte)type);
        buf.put(ssn.getBytes());
        buf.putInt(Convert.stringToIntAddress(senderAddress));
        buf.putShort((short)senderPort);
        buf.flip();

        return buf;
    }

    @Override
    public void send(ByteChannel channel) throws IOException {
        channel.write(getBuffer());
    }

    @Override
    public void sendTo(DatagramChannel channel, InetSocketAddress r) throws IOException {

    }

    public String getSSN() {
        return ssn;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public int getSenderPort() {
        return senderPort;
    }

    private String getSSN(ByteBuffer buf) {
        byte[] byteArray = new byte[12];
        for (int i = 0; i < 12; i++) {
            byteArray[i] = buf.get();
        }

        ssn = new String(byteArray);

        if (validSSN(ssn)) {
            return ssn;
        } else {
            return "0";
        }
    }

    public boolean validSSN(String ssn) {
        try {
            Double.parseDouble(ssn);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
