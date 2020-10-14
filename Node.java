import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Authors: Lisa Fjellstroem (ens15lfm), Amanda Eriksson (oi16aen)
 * Created: 09092020
 */
class Node {
    enum State {
        Q1,
        Q2,
        Q3,
        Q4,
        Q5,
        Q6,
        Q7,
        Q8,
        Q9,
    }

    private String nodeAddress;
    private boolean running;
    private State currentState;

    private InetSocketAddress tracker;
    private Selector selector;
    private DatagramChannel UDPChannel;

    private ServerSocketChannel acceptChannel;
    private int acceptPort;

    private SocketChannel predecessor, successor;

    private InetSocketAddress nodeInNetwork;
    private InetSocketAddress successorAddress;

    private int minHash, maxHash;
    private HashMap<SSN,Entry> hashMap;
    private int entries;

    /**
     * Starts node
     * @param args - trackers IP-address, trackers port number
     */
    public static void main(String[] args) throws IOException {
        new Node(args[0], Integer.parseInt(args[1]));
    }

    /**
     * Node constructor
     * Saves trackers address and opens selector and an UDP channel, registers channel to selector
     * and binds to free port
     * @param trackerIP   - the IP address to the tracker
     * @param trackerPort - the port number to the tracker
     * @throws IOException
     */
    public Node(String trackerIP, int trackerPort) throws IOException {
        tracker = new InetSocketAddress(trackerIP, trackerPort);
        selector = Selector.open();

        createUDPChannel();

        acceptChannel = ServerSocketChannel.open();

        currentState = State.Q1;
        handleCurrentState();
    }

    /**
     * Handles states in node
     */
    private void handleCurrentState() throws IOException {
        while(true){
            switch (currentState) {
                case Q1:
                    q1();
                    break;
                case Q2:
                    q2();
                    break;
                case Q3:
                    q3();
                    break;
                case Q4:
                    q4();
                    break;
                case Q5:
                    q5();
                    break;
                case Q6:
                    q6();
                    break;
                case Q7:
                    q7();
                    break;
                case Q8:
                    q8();
                    break;
                default:
                    System.err.println("Current state is not implemented!");
                    System.exit(1);
                    break;
            }
        }
    }

    /**
     * Handles Q1 state: Sends STUN_LOOKUP to tracker
     * @throws IOException
     */
    private void q1() throws IOException {
        System.out.println("[Q1]");
        System.out.format("Node started, sending STUN_LOOKUP to tracker: %s\n", tracker);

        PDU stun_lookup = new STUN_LOOKUP();
        stun_lookup.sendTo(UDPChannel, tracker);

        currentState = State.Q2;
    }

    /**
     * Handles Q2 state: Handling PDU responses in search for a STUN_RESPONSE
     * @throws IOException
     */
    private void q2() throws IOException {
        System.out.println("[Q2]");
        System.out.println("Node waiting for STUN_RESPONSE");
        getResponse();

        currentState = State.Q3;
    }

    /**
     * Bind acceptChannel
     * Send NET_GET_NODE to tracker, init self address
     * @throws IOException
     */
    private void q3() throws IOException {
        System.out.println("[Q3]");

        PDU net_get_node = new NET_GET_NODE();
        net_get_node.sendTo(UDPChannel, tracker);

        getResponse();
    }

    /**
     * Initialize hashmap to network size
     */
    private void q4() {
        System.out.println("[Q4]");
        System.out.println("Initializing Hashmap");
        minHash = 0;
        maxHash = 255;
        hashMap = new HashMap<>();
        entries = 0;

        currentState = State.Q6;
    }

    /**
     * Connect to successor, send NET_JOIN_RESPONSE, accept predecessor
     * @throws IOException
     */
    private void q5() throws IOException {
        System.out.println("[Q5]");

        connectToSuccessor();

        // Update maxHash limit
        maxHash = 127;

        // Hash limits for successor
        int rangeStart = 128;
        int rangeEnd = 255;

        sendNetJoinResponse(rangeStart, rangeEnd);

        System.out.println("Transferring upper half of entry-range to successor");

        for(Map.Entry<SSN,Entry> e: hashMap.entrySet()){
            int hashNumber = e.getKey().getHash();
            if(hashNumber > maxHash){
                Entry entry = hashMap.get(e.getKey());
                PDU val_insert = new VAL_INSERT(entry.getSSN().getSSN(),
                        entry.getName().length(), entry.getName(),
                        entry.getEmail().length(), entry.getEmail());
                val_insert.send(successor);
                entries--;
            }
        }

        acceptPredecessor();

        currentState = State.Q6;
    }

    /**
     * Send NET_ALIVE to tracker
     * @throws IOException
     */
    private void q6() throws IOException {
        System.out.print("[Q6]");
        System.out.format(" (%d entries stored)\n", entries);

        PDU net_alive = new NET_ALIVE();
        net_alive.sendTo(UDPChannel, tracker);

        getResponse();
    }

    /**
     * Send NET_JOIN to other node in NET_GET_NODE_RESPONSE
     */
    private void q7() throws IOException{
        System.out.println("[Q7]");
        System.out.println("Sending NET_JOIN to node");

        PDU net_join = new NET_JOIN(nodeAddress, acceptPort);
        net_join.sendTo(UDPChannel, nodeInNetwork);

        acceptPredecessor();

        getResponse();

        currentState = State.Q8;
    }

    private void q8() throws IOException {
        System.out.println("[Q8]");
        System.out.println("Initializing Hashmap");
        hashMap = new HashMap<>();

        connectToSuccessor();

        currentState = State.Q6;
    }

    private void handleValInsert(VAL_INSERT pdu) throws IOException{
        System.out.println("[Q9]");
        if(pdu.validSSN(pdu.getSSN()) &&
                pdu.validNameEmail(pdu.getName()) &&
                pdu.validNameEmail(pdu.getEmail())){

            Short hashNumber = Hasher.hashSSN(pdu.getSSN());
            if ((minHash <= hashNumber) && (hashNumber <= maxHash)) {
                SSN ssn = new SSN(pdu.getSSN());
                Entry entry = new Entry(ssn, pdu.getName(), pdu.getEmail());

                System.out.format("Inserting ssn Entry { ssn: \"%s\", name: \"%s\", email: \"%s\" \n",
                        entry.getSSN().getSSN(), entry.getName(), entry.getEmail());
                System.out.format("Hashcode: %d\n", hashNumber);
                hashMap.put(ssn, entry);

                entries++;
            } else {
                System.out.println("Forwarding VAL_INSERT to successor");
                pdu.send(successor);
            }
        }
    }

    private void handleValLookUp(VAL_LOOKUP pdu) throws IOException {
        System.out.println("[Q9]");
        if (pdu.validSSN(pdu.getSSN())) {
            Short hashNumber = Hasher.hashSSN(pdu.getSSN());

            if ((minHash <= hashNumber) && (hashNumber <= maxHash)) {
                for(Map.Entry<SSN,Entry> e: hashMap.entrySet()){
                    SSN ssn = e.getKey();
                    String ssnString = ssn.getSSN();
                    if(pdu.getSSN().equals(ssnString)){
                        Entry entry = hashMap.get(ssn);

                        System.out.format("Value found (ssn: %s)\n", entry.getSSN().getSSN());
                        System.out.format("Hashcode: %d\n", hashNumber);

                        PDU val_lookup_response = new VAL_LOOKUP_RESPONSE(entry.getSSN().getSSN(),
                                entry.getName().length(), entry.getName(), entry.getEmail().length(),
                                entry.getEmail());
                        val_lookup_response.sendTo(UDPChannel,
                                new InetSocketAddress(pdu.getSenderAddress(),pdu.getSenderPort()));
                    }
                }
            } else {
                System.out.println("Forwarding VAL_LOOKUP to successor");
                pdu.send(successor);
            }
        }

    }

    private void createUDPChannel() throws IOException {
        UDPChannel = DatagramChannel.open();
        UDPChannel.configureBlocking(false);
        UDPChannel.register(selector, SelectionKey.OP_READ);
        UDPChannel.bind(new InetSocketAddress("0.0.0.0", 0));
    }

    private void getResponse() throws IOException{
        running = true;
        while (running) {
            selector.select(3000);

            for (var key : selector.selectedKeys()) {
                // Check if key is in READ state
                if (key.isReadable()) {
                    var buffer = getBuffer(key);

                    var response = PDU.create(buffer, (ByteChannel)key.channel());
                    if(response != null) {

                        if(response.type == PDU.STUN_RESPONSE){
                            handleStunResponse((STUN_RESPONSE)response);
                            // Running = False to stop the while loop
                            running = false;
                        }
                        else if(response.type == PDU.NET_GET_NODE_RESPONSE){
                            handleNetGetNodeResponse((NET_GET_NODE_RESPONSE)response);
                            running = false;
                        }
                        else if(response.type == PDU.NET_JOIN){
                            handleNetJoin((NET_JOIN)response);
                            running = false;
                        }
                        else if(response.type == PDU.NET_JOIN_RESPONSE){
                            handleNetJoinResponse((NET_JOIN_RESPONSE)response);
                            running = false;
                        }
                        else if(response.type == PDU.VAL_INSERT){
                            handleValInsert((VAL_INSERT)response);
                            //currentState = State.Q6;
                        }
                        else if(response.type == PDU.VAL_LOOKUP) {
                            handleValLookUp((VAL_LOOKUP)response);
                        }
                        else {
                            System.err.println("Error in response");
                        }
                    }
                }
            }
            selector.selectedKeys().clear();
            running = false;
        }
    }

    private ByteBuffer getBuffer(SelectionKey key) throws IOException {
        var buffer = ByteBuffer.allocate(1000);

        if(key.channel() instanceof DatagramChannel){
            ((DatagramChannel)key.channel()).receive(buffer);
        }
        else {
            ((SocketChannel)key.channel()).read(buffer);
        }

        return buffer.flip();
    }

    /**
     * Save nodeAddress. Bind acceptChannel
     * @param pdu - Nodes own address
     */
    private void handleStunResponse(STUN_RESPONSE pdu) throws IOException {
        nodeAddress = pdu.getAddress();
        System.out.printf("Got STUN_RESPONSE, my address is %s\n", nodeAddress);

        acceptChannel.bind(new InetSocketAddress(nodeAddress,0));
        acceptPort = acceptChannel.socket().getLocalPort();
    }

    private void handleNetGetNodeResponse(NET_GET_NODE_RESPONSE pdu){
        if (pdu.getPort() == 0) {
            System.out.println("I am the first node to join the network");
            currentState = State.Q4;
        } else {
            System.out.println("I am not alone in the network!");
            nodeInNetwork = new InetSocketAddress(pdu.getAddress(), pdu.getPort());

            currentState = State.Q7;
        }
    }

    private void handleNetJoin(NET_JOIN pdu) {
        successorAddress = new InetSocketAddress(pdu.getSrcAddress(), pdu.getSrcPort());
        System.out.format("Got NET_JOIN from %s:%d\n",
                successorAddress.getAddress(), successorAddress.getPort());

        //Q12 updating max fields
        pdu.setMaxSpan(0);
        pdu.setMaxAddress(nodeAddress);
        pdu.setMaxPort(acceptPort);

        currentState = State.Q5;
    }

    private void handleNetJoinResponse(NET_JOIN_RESPONSE pdu) throws IOException {
        successorAddress = new InetSocketAddress(pdu.getNextAddress(), pdu.getNextPort());
        minHash = pdu.getRangeStart();
        maxHash = pdu.getRangeEnd();
        System.out.format("Got NET_JOIN_RESPONSE from %s, my range is (%d, %d)\n",
                predecessor.getLocalAddress(), minHash, maxHash);
    }

    private void sendNetJoinResponse(int rangeStart, int rangeEnd) throws IOException {
        System.out.format("Sending NET_JOIN_RESPONSE to %s\n", successor.getRemoteAddress());
        PDU net_join_response = new NET_JOIN_RESPONSE
                (nodeAddress, acceptPort, rangeStart, rangeEnd);
        net_join_response.send(successor);
    }

    private void connectToSuccessor() throws IOException {
        successor = SocketChannel.open();
        successor.configureBlocking(false);
        successor.register(selector, SelectionKey.OP_READ);
        successor.bind(new InetSocketAddress(nodeAddress, 0));
        successor.connect(successorAddress);

        if(successor.finishConnect()){
            System.out.format("Connected to new successor %s:%d\n",
                    successorAddress.getAddress(), successorAddress.getPort());
        } else {
            System.out.println("Failed to connect to successor!");
        }
    }

    private void acceptPredecessor() throws IOException {
        predecessor = acceptChannel.accept();
        System.out.format("Accepted new predecessor %s\n", predecessor.getLocalAddress());

        predecessor.configureBlocking(false);
        predecessor.register(selector, SelectionKey.OP_READ);
    }


}
