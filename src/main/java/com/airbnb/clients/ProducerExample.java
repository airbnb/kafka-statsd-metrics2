package com.airbnb.clients;

/**
 * Example of how to metrics reporter in a kafka producer using kafka API
 * producer code is from kafka example
 */

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class ProducerExample {
    public static void main(String[] args) {
        long events = args.length>0?Long.parseLong(args[0]):1;
        Random rnd = new Random();

        Properties props = new Properties();
        props.put("metadata.broker.list", "127.0.0.1:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");
        //props.put("client.id", "client1");    //this is not necessary
        //props.put("producer.type", "async");

        props.put("kafka.metrics.reporters", "com.airbnb.kafka.KafkaMetricsDogstatsdReporter");
        props.put("kafka.metrics.polling.interval.secs", "5");
        props.put("external.kafka.statsd.reporter.enabled", "true");

        ProducerConfig config = new ProducerConfig(props);

        Producer<String, String> producer = new Producer<>(config);

        for (long nEvents = 0; nEvents < events; nEvents++) {
            long runtime = new Date().getTime();
            String ip = "192.168.2." + rnd.nextInt(255);
            String msg = runtime + ",www.example.com," + ip;
            KeyedMessage<String, String> data = new KeyedMessage<>("test", ip, msg);
            producer.send(data);
        }

        try {
            Thread.sleep(7000);
        } catch (InterruptedException ie) {
        }

        producer.close();
    }
}
