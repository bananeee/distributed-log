
# Research
## Blog <https://bravenewgeek.com/building-a-distributed-log-from-scratch-part-1-storage-mechanics/>
- storage mechanism: sequential read/write etc
- data replication: gossip/multicast protocols and consensus protocols - prefer the later 

## Network
- Kafka uses java NIO (non-blocking IO) for network communication.
- KafkaServer has 1 acceptor thread, and multiple processor threads to handle network requests.


# Design
## Prototype
- Implement a simple message broker (pub-sub model)
- Network layer: java NIO vs traditional io with virtual threads
- Storage layer: file based storage with sequential read/write
- 


# Resources
- <https://rohithsankepally.github.io/Kafka-Storage-Internals/>
- <https://bravenewgeek.com/building-a-distributed-log-from-scratch-part-1-storage-mechanics/>***
- <https://cefboud.com/posts/exploring-kafka-internals/>
- <https://engineering.linkedin.com/distributed-systems/log-what-every-software-engineer-should-know-about-real-time-datas-unifying>**
- <https://www.automq.com/blog/understand-kafka-network-communication-and-thread-model>