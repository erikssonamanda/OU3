import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.DatagramChannel;
import java.net.InetSocketAddress;
import java.io.IOException;

class STUN_RESPONSE extends PDU {
    private String address;

    public STUN_RESPONSE(ByteBuffer buf, ByteChannel src) {
        super(PDU.STUN_RESPONSE);
        int a = buf.getInt();
        address = addressToString(a);
    }

    @Override
    public void sendTo(DatagramChannel channel, InetSocketAddress r) throws IOException {
    }

    @Override
    public void send(ByteChannel channel) {

    }

    public String getAddress() {
        return address;
    }

    /**
     * Converts an integer representation of an Ipv4 address
     * to its string counterpart.
     * @param a The address to convert.
     * @return The converted address.
     */
    private String addressToString(int a) {
        String res = "";
        int[] mask = { 0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000 };
        for(int i = 0; i < 4; i++) {
            int b = (a & mask[i]) >> (i * 8);
            if (i != 3) {
            }
            res = b + (i != 0 ? "." : "") + res;
        }

        return res;
    }
}
