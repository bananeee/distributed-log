
# Research
## Blog[^2][^4]
- storage mechanism: sequential read/write etc
- data replication: gossip/multicast protocols and consensus protocols - prefer the later 

## Network
### Kafka 
- Kafka uses java NIO (non-blocking IO) for network communication.[^5][^3]
- KafkaServer has 1 acceptor thread, and multiple processor threads to handle network requests.

### Virtual Thread
- Using blocking IO with virtual thread has much more benefits than java NIO with worker pool:
  - Don't need to manage extra logic for handling non-blocking IO (selector, channel, buffer etc)
  - Virtual threads are lightweight and can scale to a large number of concurrent connections, so we can use a thread per connection model.
- However, it is inefficient to keep virtual thread long live. And message queue/distributed log often keep the connection alive.
- Virtual thread is good when handling I/O bound tasks.

## Protocol
### NATS
- NATS protocol is a simple, text-based publish/subscribe style protocol.
- NATS protocol is parsed through a zero allocation byte parser[^6]

### Kafka[^9]
- Kafka uses a custom binary protocol for communication between clients and brokers.[^7]

## Storage[^1] 

# Design
## Prototype
- Implement a simple message broker (pub-sub model)
- Network layer: long polling + java NIO/traditional io with virtual threads -> java NIO
- Storage layer: file based storage with sequential read/write
- Note on Spring Kafka on virtual thread[^8]
 



[//]: # (# Resources)
[^1]: <https://rohithsankepally.github.io/Kafka-Storage-Internals/>
[^2]: <https://bravenewgeek.com/building-a-distributed-log-from-scratch-part-1-storage-mechanics/>***
[^3]: <https://cefboud.com/posts/exploring-kafka-internals/>
[^4]: <https://engineering.linkedin.com/distributed-systems/log-what-every-software-engineer-should-know-about-real-time-datas-unifying>**
[^5]: <https://www.automq.com/blog/understand-kafka-network-communication-and-thread-model>
[^6]: <https://www.youtube.com/watch?v=ylRKac5kSOk&t=646s>
[^7]: <https://snehasishroy.com/understanding-the-kafka-communication-protocol-in-detail>
[^8]: <https://github.com/spring-projects/spring-kafka/commit/ae775d804f82483f99d4cab2a16ef2b27649252a#diff-4a8e19bfb7db802a5ebe663b85bfbdcb4025b98532fc696f7f32d5b3228dee76>
[^9]: <https://ossrs.net/lts/zh-cn/assets/files/kafka-160915-0553-82964-c24c2b2f5caacb605a0ccec44e4eb9db.pdf>
[^10]: <https://binspec.org/kafka-api-versions-request-v4>