
# Research
## Blog[^2][^4]
- storage mechanism: sequential read/write etc
- data replication: gossip/multicast protocols and consensus protocols - prefer the later 

## Network 
### Model
#### NATS
- Core NATS implements a publish-subscribe message distribution model for one-to-many communication (fan-out):
  - A publisher sends a message on a subject and any active subscribers to that subject receive the message.
- Core NATS also supports request-reply pattern through it core mechanism pub-sub model.

### Implementation
#### Kafka 
- Kafka uses java NIO (non-blocking IO) for network communication.[^5][^3]
- KafkaServer has 1 acceptor thread, and multiple processor threads to handle network requests.

#### NATS 
- NATS Jetstream using 

#### Virtual Thread
- Using blocking IO with virtual thread has much more benefits than java NIO with worker pool:
  - Don't need to manage extra logic for handling non-blocking IO (selector, channel, buffer etc)
  - Virtual threads are lightweight and can scale to a large number of concurrent connections, so we can use a thread per connection model.
- However, it is inefficient to keep virtual thread long live. And message queue/distributed log often keep the connection alive.
- Virtual thread is good when handling I/O bound tasks.

### Protocol
#### NATS
- NATS protocol is a simple, text-based publish/subscribe style protocol.
- NATS protocol is parsed through a zero allocation byte parser[^6]

#### Kafka[^9]
- Kafka uses a custom binary protocol for communication between clients and brokers.[^7][^11]

## Storage[^1]

## Concept
### NATS
Core NATS uses pub-sub model with subject/topic-based addressing. Can be enhanced with JetStream for persistence.

### Kafka
Event stream/distributed log model with pull based comsumer
- Event: Record/Message, the atomic unit of data in Kafka.
- Topic: Events are categorized into topics. Like folder and file.
- Partition: A way to distribute topic events across multiple brokers. Events in a partition are ordered.
- Partition leader: Each partition has a leader broker that handles all read and write requests for that partition.
- Consumer group: A group of consumers that work together to consume events from a topic. Each consumer in the group is assigned a subset of partitions to read from.

### Java NIO [^12]
- Selector: A multiplexor of SelectableChannel objects.
- Channel: A bi-directional communication channel that can be non-blocking. Can be used with a Selector. Once the channel is registered with a selector, it can be monitored for events such as read, write, connect, and accept.
- Buffer: A container for data that is being read from or written to a channel.

## Implementation
### Kafka
#### Broker[^13]

#### Consumer
##### Poll mechanism[^14]


# Design
## Prototype
- Implement a simple message broker (pub-sub model)
- Network layer: long polling + java NIO/traditional io with virtual threads -> java NIO
- Storage layer: file based storage with sequential read/write
- Note on Spring Kafka on virtual thread[^8]

## Protocol
- Op to connect client to broker (able to extend to auth later)
- Op to send message 
- Op to request message 
- Ping/pong 
- Use Protobuf for message serialization

# Questions
- FetchRequest in Kafka protocol: why there is maxBytes? it should be max records right?
- Kafka rebalancing strategy?


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
[^11]: <https://cwiki.apache.org/confluence/display/KAFKA/Writing+a+Driver+for+Kafka>
[^12]: <https://jenkov.com/tutorials/java-nio/index.html>
[^13]: <https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Internals>
[^14]: <https://glasp.co/hatch/xktzo0ahoa54x4v4/p/hwyQEQwXvdRsxu9xzzT2>