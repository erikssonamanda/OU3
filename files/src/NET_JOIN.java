import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;

class NET_JOIN extends PDU {
    private String srcAddress;
    private int srcPort;

    private int maxSpan;
    private String maxAddress;
    private int maxPort;

    private Convert convert;

    public NET_JOIN(String srcAddress, int srcPort) {
        super(PDU.NET_JOIN);
        this.srcAddress = srcAddress;
        this.srcPort = srcPort;
    }

    public NET_JOIN(ByteBuffer buf, ByteChannel src) {
        super(PDU.NET_JOIN);
        int a = buf.getInt();
        short p = buf.getShort();
        byte maxS = buf.get();
        int maxA = buf.getInt();
        short maxP = buf.getShort();

        srcAddress = convert.addressToString(a);
        srcPort = Short.toUnsignedInt(p);
        maxSpan = Byte.toUnsignedInt(maxS);
        maxAddress = convert.addressToString(maxA);
        maxPort = Short.toUnsignedInt(maxP);

    }

    @Override
    public void send(ByteChannel channel) {

    }

    private ByteBuffer getBuffer() {
        var buf = ByteBuffer.allocate(14);

        buf.put((byte) type);
        buf.putInt(convert.stringToIntAddress(srcAddress));
        buf.putShort((short)srcPort);
        //Make pretty later
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.flip();

        return buf;
    }

    @Override
    public void sendTo(DatagramChannel channel, InetSocketAddress r) throws IOException {
        channel.send(getBuffer(), r);
    }

    public void setMaxSpan(int maxSpan) {
        this.maxSpan = maxSpan;
    }

    public void setMaxAddress(String maxAddress) {
        this.maxAddress = maxAddress;
    }

    public void setMaxPort(int maxPort) {
        this.maxPort = maxPort;
    }

    public String getSrcAddress() {
        return srcAddress;
    }

    public int getSrcPort() {
        return srcPort;
    }
}
