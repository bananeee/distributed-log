# NATS Protocol
## OP 

| OP      | Sent by | Description                                                |
|---------|---------|------------------------------------------------------------|
| INFO    | server  | Sent to client after init TPC/IP                           |
| CONNECT | client  | Sent to server to specify connection information           |
| PUB     | client  | Publish a message to a subject with (or not reply subject) |
| HPUB    | client  | Same with PUB but including NATS header                    |
| SUB     | client  | Subcribe to a subject                                      |
| UNSUB   | client  | Unsubcribe (or auto-unsubcribe) subject                    |
| MSG     | server  | Deliver a msg to a subcriber                               |
| HMSG    | server  | Same with MSG but including NATS header                    |
| PING    | both    | PING keep-alive message                                    |
| PONG    | both    | PONG keep-alive message                                    |
| +OK     | server  | Acknowledges well-formed protocol message in verbose mode  |
| -ERR    | server  | Indicates a protocol error. May cause client disconnect    |

    
## References
- <https://docs.nats.io/reference/reference-protocols/nats-protocol#info>