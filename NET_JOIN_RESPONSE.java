import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;

class NET_JOIN_RESPONSE extends PDU{
    private String nextAddress;
    private int nextPort;
    private int rangeStart, rangeEnd;

    public NET_JOIN_RESPONSE(String nextAddress, int nextPort, int rangeStart, int rangeEnd) {
        super(PDU.NET_JOIN_RESPONSE);
        this.nextAddress = nextAddress;
        this.nextPort = nextPort;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public NET_JOIN_RESPONSE(ByteBuffer buf, ByteChannel src) {
        super(PDU.NET_JOIN_RESPONSE);
        int a = buf.getInt();
        short p = buf.getShort();
        byte s = buf.get();
        byte e = buf.get();

        // Cast input to correct format
        nextAddress = Convert.addressToString(a);
        nextPort = Short.toUnsignedInt(p);
        rangeStart = Byte.toUnsignedInt(s);
        rangeEnd = Byte.toUnsignedInt(e);
    }

    private ByteBuffer getBuffer() {
        var buf = ByteBuffer.allocate(9);

        buf.put((byte) type);
        buf.putInt(Convert.stringToIntAddress(nextAddress));
        buf.putShort((short)nextPort);
        buf.put((byte)rangeStart);
        buf.put((byte)rangeEnd);
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

    public int getRangeStart() {
        return rangeStart;
    }

    public int getRangeEnd(){
        return rangeEnd;
    }

    public String getNextAddress() {
        return nextAddress;
    }

    public int getNextPort() {
        return nextPort;
    }
}
