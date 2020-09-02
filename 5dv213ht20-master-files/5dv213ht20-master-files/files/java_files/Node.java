import java.nio.channels.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.InetSocketAddress;
import java.util.HashMap;

class Node {

    enum State {
        Q1,
        Q2,
        Q3,
    }

    private DatagramChannel udpChannel;
    private SocketChannel predecessor, successor;

    private Selector selector;
    
    private InetSocketAddress tracker;

    private String selfAddress;

    private State currentState;

    //Not needed yet
    private ServerSocketChannel acceptChannel;
    private int minHash, maxHash;

    public static void main(String[] args) throws IOException {
        new Node(args[0], Integer.parseInt(args[1]));
    }

    public Node(String trackerIP, int trackerPort) throws IOException {

        // Save the tracker-address
        tracker = new InetSocketAddress(trackerIP, trackerPort);
        selector = Selector.open(); 

        // Create a UDP-cannel (DatagramChannel)
        udpChannel = DatagramChannel.open();
        udpChannel.configureBlocking(false);
        udpChannel.register(selector, SelectionKey.OP_READ);

        udpChannel.bind(new InetSocketAddress("0.0.0.0", 0));

        acceptChannel = ServerSocketChannel.open();
        acceptChannel.configureBlocking(false);

        currentState = State.Q1;

        runNode();
    }

    
    private void q1() throws IOException {
        System.out.println("[Q1]");
        PDU stun_lookup = new STUN_LOOKUP();
        // Send STUN_LOOKUP to tracker, via udpChannel
        stun_lookup.sendTo(udpChannel, tracker);

        var running = true;

        while(running) {

            // Await response from tracker
            selector.select();

            for(var key : selector.selectedKeys()) {
                if(key.isReadable()) {

                    // Allocate buffer to store incoming PDU
                    var buffer = ByteBuffer.allocate(30);
                    ((DatagramChannel)key.channel()).receive(buffer);
                    buffer.flip();

                    // Parse response as PDU
                    var response = PDU.create(buffer, (ByteChannel)key.channel());

                    if(response.type == PDU.STUN_RESPONSE) {
                        handleStunResponse((STUN_RESPONSE)response);
                    } else {
                        System.err.println("Expected a STUN_RESPONSE!");
                    }
                }
            }

            selector.selectedKeys().clear();
        }
    }

    private void handleStunResponse(STUN_RESPONSE pdu) {
        selfAddress = pdu.getAddress();        
        System.out.printf("Got STUN_RESPONSE, my address is %s\n", selfAddress);
    }


    private void runNode() throws IOException {
        PDU p = null;

        while(true) {
            switch (currentState) {
                case Q1:
                    q1();
                    break;
                case Q2:
                    break;
                case Q3:
                    break;
                default:
                    System.err.println("State not yet implemented");
                    break;
            }
       }
    }
}
