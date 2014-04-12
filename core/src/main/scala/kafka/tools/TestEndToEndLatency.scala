/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.tools

import java.util.Properties
import kafka.consumer._
import org.apache.kafka.clients.producer.{ProducerRecord, KafkaProducer}

object TestEndToEndLatency {
  def main(args: Array[String]) {
    if (args.length != 4) {
      System.err.println("USAGE: java " + getClass().getName + " broker_list zookeeper_connect topic num_messages")
      System.exit(1)
    }

    val brokerList = args(0)
    val zkConnect = args(1)
    val topic = args(2)
    val numMessages = args(3).toInt

    val consumerProps = new Properties()
    consumerProps.put("group.id", topic)
    consumerProps.put("auto.commit.enable", "true")
    consumerProps.put("auto.offset.reset", "largest")
    consumerProps.put("zookeeper.connect", zkConnect)
    consumerProps.put("socket.timeout.ms", 1201000.toString)

    val config = new ConsumerConfig(consumerProps)
    val connector = Consumer.create(config)
    var stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).head.head
    val iter = stream.iterator

    val producerProps = new Properties()
    producerProps.put("metadata.broker.list", brokerList)
    producerProps.put("linger.ms", "0")
    producerProps.put("block.on.buffer.full", "true")
    val producer = new KafkaProducer(producerProps)

    val message = "hello there beautiful".getBytes
    var totalTime = 0.0
    for (i <- 0 until numMessages) {
      var begin = System.nanoTime
      val response = producer.send(new ProducerRecord(topic, message))
      response.get()
      val received = iter.next
      val elapsed = System.nanoTime - begin
      // poor man's progress bar
      if (i % 1000 == 0)
        println(i + "\t" + elapsed / 1000.0 / 1000.0)
      totalTime += elapsed
    }
    println("Avg latency: " + (totalTime / numMessages / 1000.0 / 1000.0) + "ms")
    producer.close()
    connector.shutdown()
    System.exit(0)
  }
}