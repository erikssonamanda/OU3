# Binaries
The directory contains pre-compiled binaries for the tracker, node and test-client.
The binaries are built for linux, if you can not run the files you can compile them
from source, instructions and source are located in the [src](files/src) directory.


All binaries have built in help describing the arguments for the program, which can be
shown by running `./{tracker/node/client} --help`

## Tracker

The tracker takes one mandatory argument, the port to listen for incoming UDP traffic.
Optional parameters are shown by running the tracker with the `--help` parameter.

## Node

The `node` binary is an implementation of the described node. Use the `--help` parameter
to see what the parameters are.


## Client

The `client` allows you to perform value operations to a node network, as well as bulk inserts
via a `csv` file. A sample `csv` is provided in [data.csv](data.csv). The client can either
connect to a specific node, or use the tracker to get a random node from the network.
Use the `--help` parameter to see the available parameters.
