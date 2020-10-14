import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

class VAL_INSERT extends PDU {
    private String ssn;
    private int nameLength;
    private String name;
    private int emailLength;
    private String email;

    public VAL_INSERT(String ssn, int nameLength, String name, int emailLength, String email) {
        super(PDU.VAL_INSERT);

        this.ssn = ssn;
        this.nameLength = nameLength;
        this.name = name;
        this.emailLength = emailLength;
        this.email = email;
    }

    public VAL_INSERT(ByteBuffer buf, ByteChannel src) {
        super(PDU.VAL_INSERT);

        this.ssn = getSSN(buf);

        // Get name
        byte nl = buf.get();
        this.nameLength = Byte.toUnsignedInt(nl);
        this.name = getNameEmail(buf, nameLength);

        // Get email
        byte el = buf.get();
        emailLength = Byte.toUnsignedInt(el);
        this.email = getNameEmail(buf, emailLength);
    }

    private ByteBuffer getBuffer() {
        var buf = ByteBuffer.allocate(15 + nameLength + emailLength);

        buf.put((byte) type);
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
        channel.write(getBuffer());
    }

    @Override
    public void sendTo(DatagramChannel channel, InetSocketAddress r) throws IOException {

    }

    public String getSSN() {
        return ssn;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
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
        if (ssn != null){
            try {
                Double.parseDouble(ssn);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        else {
            return false;
        }
    }

    /**
     * Reads bytes from buffer to get name or email
     * @param buf
     * @param stringLength
     * @return
     */
    private String getNameEmail(ByteBuffer buf, int stringLength) {
        byte[] byteArray = new byte[stringLength];
        for (int i = 0; i < stringLength; i++) {
            byteArray[i] = buf.get();
        }

        String input = new String(byteArray);

        if (validNameEmail(input)) {
            return input;
        } else {
            return "0";
        }
    }

    public boolean validNameEmail(String input) {
        if(input != null){
            if (input.length() <= 255) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }
}