
# Research
## Blog <https://bravenewgeek.com/building-a-distributed-log-from-scratch-part-1-storage-mechanics/>
- storage mechanism: sequential read/write etc
- data replication: gossip/multicast protocols and consensus protocols - prefer the later 

## Network
- Kafka uses java NIO (non-blocking IO) for network communication.
- KafkaServer has 1 acceptor thread, and multiple processor threads to handle network requests.
- Using blocking IO with virtual thread has much more benefits than java NIO with worker pool:
  - Dont need to manage extra logic for handling non-blocking IO (selector, channel, buffer etc)
  - Virtual threads are lightweight and can scale to a large number of concurrent connections, so we can use a thread per connection model.


# Design
## Prototype
- Implement a simple message broker (pub-sub model)
- Network layer: long polling + java NIO/traditional io with virtual threads
- Storage layer: file based storage with sequential read/write
- Note on Spring Kafka on virtual thread: <https://github.com/spring-projects/spring-kafka/commit/ae775d804f82483f99d4cab2a16ef2b27649252a#diff-4a8e19bfb7db802a5ebe663b85bfbdcb4025b98532fc696f7f32d5b3228dee76>
 



# Resources
- <https://rohithsankepally.github.io/Kafka-Storage-Internals/>
- <https://bravenewgeek.com/building-a-distributed-log-from-scratch-part-1-storage-mechanics/>***
- <https://cefboud.com/posts/exploring-kafka-internals/>
- <https://engineering.linkedin.com/distributed-systems/log-what-every-software-engineer-should-know-about-real-time-datas-unifying>**
- <https://www.automq.com/blog/understand-kafka-network-communication-and-thread-model>