package com.airbnb.clients;

/**
 * Example of how ot use metrics reporter in a kafka consumer using high level API
 * consumer code is from kafka example
 */

import com.airbnb.kafka.KafkaMetricsDogstatsdReporter;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.metrics.KafkaMetricsReporter;
import kafka.metrics.KafkaMetricsReporterMBean;
import kafka.utils.VerifiableProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConsumerGroupExample {

    public class ConsumerTest implements Runnable {
        private KafkaStream m_stream;
        private int m_threadNumber;

        public ConsumerTest(KafkaStream a_stream, int a_threadNumber) {
            m_threadNumber = a_threadNumber;
            m_stream = a_stream;
        }

        public void run() {
            ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
            while (it.hasNext())
                System.out.println("Thread " + m_threadNumber + ": " + new String(it.next().message()));
            System.out.println("Shutting down Thread: " + m_threadNumber);
        }
    }

    private final ConsumerConnector consumer;
    private final String topic;
    private ExecutorService executor;

    public ConsumerGroupExample(String a_zookeeper, String a_groupId, String a_topic) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
                createConsumerConfig(a_zookeeper, a_groupId));
        this.topic = a_topic;
    }

    public void shutdown() {
        if (consumer != null) consumer.shutdown();
        if (executor != null) executor.shutdown();
        try {
            if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted during shutdown, exiting uncleanly");
        }
    }

    public void run(int a_numThreads) {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(a_numThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        // now launch all the threads
        executor = Executors.newFixedThreadPool(a_numThreads);

        // now create an object to consume the messages
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new ConsumerTest(stream, threadNumber));
            threadNumber++;
        }
    }

    private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.commit.enable", "false");
        props.put("auto.offset.reset", "smallest");

        return new ConsumerConfig(props);
    }

    public static void main(String[] args) {        //run it as 127.0.0.1:2181 group3 myTopic 2
        String zooKeeper = args.length > 0 ? args[0] : "127.0.0.1:2181";  //ZooKeeper connection string with port number
        String groupId = args.length > 1 ? args[1] : "group-7";             //Consumer Group name to use for this process
        String topic = args.length > 2 ? args[2] : "test";                  //Topic to consume messages from
        int threads = Integer.parseInt(args.length > 3 ? args[3] : "1");      //# of threads to launch to consume the messages

        ConsumerGroupExample example = new ConsumerGroupExample(zooKeeper, groupId, topic);
        example.run(threads);

        //In case that we init reporter before example.run, we won't any metric reading
        Properties props = new Properties();
        props.put("kafka.metrics.reporters", "com.airbnb.kafka.KafkaMetricsDogstatsdReporter");
        props.put("kafka.metrics.polling.interval.secs", "5");
        props.put("external.kafka.statsd.reporter.enabled", "true");
        VerifiableProperties verifiableProps = new VerifiableProperties(props);
        KafkaMetricsReporter reporter = new KafkaMetricsDogstatsdReporter();
        reporter.init(verifiableProps);

        try {
            Thread.sleep(7000);
        } catch (InterruptedException ie) {
        }

        example.shutdown();
        if (reporter instanceof KafkaMetricsReporterMBean) {
            ((KafkaMetricsReporterMBean) reporter).stopReporter();
        }
    }
}