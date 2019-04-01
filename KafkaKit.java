package com.bobsystem.exercise.commons;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.AlterConfigsResult;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;

public class KafkaKit {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaKit.class);

    //region CONSTANT
    private static final String HOST =
        "192.168.10.190:59092,192.168.10.191:59092,192.168.10.192:59092";

    private static final Properties PROPS_PRODUCER = new Properties();
    private static final Properties PROPS_CONSUMER = new Properties();
    private static final Properties PROPS_ADMIN_CLIENT = new Properties();

    static {
        //region producer properties
        // kafka-console-producer --broker-list localhost:9092
        //                        --topic       first-topic
        PROPS_PRODUCER.setProperty("bootstrap.servers", HOST);
        PROPS_PRODUCER.setProperty("acks", "all");
        PROPS_PRODUCER.setProperty("retries", "0");
        PROPS_PRODUCER.setProperty("batch.size", "16384");
        PROPS_PRODUCER.setProperty("linger.ms", "1");
        PROPS_PRODUCER.setProperty("buffer.memory", "33554432");
        PROPS_PRODUCER.setProperty("key.serializer",
            "org.apache.kafka.common.serialization.StringSerializer");
        //endregion
        //region consumer properties
        // kafka-console-consumer --bootstrap-server localhost:9092
        //                        --topic            first-topic
        PROPS_CONSUMER.setProperty("bootstrap.servers", HOST);
        PROPS_CONSUMER.setProperty("retries", "0");
        PROPS_CONSUMER.setProperty("enable.auto.commit", "true");
        PROPS_CONSUMER.setProperty("auto.commit.interval.ms", "1000");
        PROPS_CONSUMER.setProperty("auto.offset.rest", "earliest");
        PROPS_CONSUMER.setProperty("session.timeout.ms", "30000");
        PROPS_CONSUMER.setProperty("key.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
        //endregion
        //region admin client props
        PROPS_ADMIN_CLIENT.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, HOST);
        //endregion
    }
    //endregion

    //region Producer
    public static class Producer<K, V>
        extends KafkaProducer<K, V> {

        //region constructors
        private Producer() {
            super(PROPS_PRODUCER);
        }
        //endregion

        //region static methods
        public static <K, V> Producer<K, V> client(Class valueType) {
            if (String.class == valueType) {
                PROPS_PRODUCER.setProperty(
                    "value.serializer",
                    "org.apache.kafka.common.serialization.StringSerializer");
            }
            return new Producer<>();
        }
        //endregion

        //region member methods
        public Future<RecordMetadata> send(String topic, V value) {
            return send(topic, null, value);
        }

        public Future<RecordMetadata> send(String topic, K key, V value) {
            return send(topic, key, value, null);
        }

        public Future<RecordMetadata> send(String topic, K key, V value, Callback callback) {
            return super.send(new ProducerRecord<>(topic, key, value), callback);
        }
        //endregion
    }
    //endregion

    //region Consumer
    public static class Consumer<K, V>
        extends KafkaConsumer<K, V> {

        //region constructors
        private Consumer() {
            super(PROPS_CONSUMER);
        }
        //endregion

        //region static methods
        public static <K, V> Consumer<K, V> client(String groupId,
                                                   String topic,
                                                   Class valueType) {
            PROPS_CONSUMER.setProperty("group.id", groupId);
            if (String.class == valueType) {
                PROPS_CONSUMER.setProperty(
                    "value.deserializer",
                    "org.apache.kafka.common.serialization.StringDeserializer");
            }
            Consumer<K, V> result = new Consumer<>();
            result.subscribe(Arrays.asList(topic));
            return result;
        }
        //endregion
    }
    //endregion

    //region ConsumerGroup
    public static class ConsumerGroup {

        //region static methods
        public static <K, V> List<ConsumeTask<K, V>> create(String groupId,
                                                            String topic,
                                                            Class valueType,
                                                            int quantity,
                                                            ConsumeCallback<K, V> callback) {
            return create(
                groupId,
                topic,
                valueType,
                quantity,
                1000 * 2, // 等待 producer 提交消息
                callback);
        }

        public static <K, V> List<ConsumeTask<K, V>> create(String groupId,
                                                            String topic,
                                                            Class valueType,
                                                            int quantity,
                                                            long timeout,
                                                            ConsumeCallback<K, V> callback) {
            if (callback == null)
                throw new IllegalArgumentException("callback is null");
            if (quantity <= 0) quantity = 1;
            if (timeout <= 0) timeout = 1000 * 2; // 等待 producer 提交消息
            List<ConsumeTask<K, V>> result = new LinkedList<>();
            for (int i = 0; i < quantity; ++i) {
                Consumer<K, V> consumer = Consumer.client(groupId, topic, valueType);
                result.add(new ConsumeTask<>(consumer, callback, timeout));
            }
            return result;
        }
        //endregion
    }
    //endregion

    //region ConsumeTask
    public static class ConsumeTask<K, V>
        implements Runnable {

        //region property fields
        private Consumer<K, V> consumer;
        private ConsumeCallback<K, V> callback;
        private long timeout;
        //endregion

        //region constructors
        private ConsumeTask(Consumer<K, V> consumer,
                            ConsumeCallback<K, V> callback) {
            this(consumer, callback, 1000 * 2); // 等待 producer 提交消息
        }

        private ConsumeTask(Consumer<K, V> consumer,
                            ConsumeCallback<K, V> callback,
                            long timeout) {
            this.consumer = consumer;
            this.callback = callback;
            this.timeout = timeout;
        }
        //endregion

        //region member methods
        @Override
        public void run() {
            ConsumerRecords<K, V> records = this.consumer.poll(this.timeout);
            for (ConsumerRecord<K, V> record : records) {
                if (this.callback != null) {
                    this.callback.consume(record);
                }
                //LOGGER.debug("///// thread={}, offset={}, value={}",
                //    Thread.currentThread().getName(), record.offset(), record.value());
            }
            this.consumer.close();
        }
        //endregion
    }
    //endregion

    //region ConsumeCallback
    public interface ConsumeCallback<K, V> {

        void consume(ConsumerRecord<K, V> record);
    }
    //endregion

    //region Topic
    public static class Topics {

        private static final Logger LOGGER = LoggerFactory.getLogger(Topics.class);

        /*
         * kafka-topics --create
         *              --zookeeper localhost:2081
         *              --replication-factor 1
         *              --partitions 3
         *              --topic first-topic
         */
        public static CreateTopicsResult create(String name,
                                                int numPartitions,
                                                short replicationFactor) {
            try (AdminClient client = AdminClient.create(PROPS_ADMIN_CLIENT)) {
                NewTopic topic = new NewTopic(name, numPartitions, replicationFactor);
                return client.createTopics(Arrays.asList(topic));
            }
            catch (Exception ex) {
                LOGGER.error(ex.getLocalizedMessage(), ex);
            }
            return null;
        }

        public static DeleteTopicsResult delete(String...topics) {
            try (AdminClient client = AdminClient.create(PROPS_ADMIN_CLIENT)) {
                return client.deleteTopics(Arrays.asList(topics));
            }
            catch (Exception ex) {
                LOGGER.error(ex.getLocalizedMessage(), ex);
            }
            return null;
        }

        /**
         * @param listInternal includes internal topics such as __consumer_offsets
         */
        public static Set<String> list(boolean listInternal) {
            try (AdminClient client = AdminClient.create(PROPS_ADMIN_CLIENT)) {
                ListTopicsOptions options = new ListTopicsOptions();
                options.listInternal(listInternal);
                ListTopicsResult list = client.listTopics(options);
                return list.names().get();
            }
            catch (Exception ex) {
                LOGGER.error(ex.getLocalizedMessage(), ex);
            }
            return null;
        }
    }
    //endregion

    //region static methods
    /*
     * kafka-config --alert
     *              --zookeeper   localhost:2081
     *              --entity-type topics 表示配置的是 topics
     *              --entity-name first-topic
     *              --add-config  max.message.bytes=12800
     *
     * e.g.
     *     alertConfig(
     *         ConfigResource.Type.Topic,
     *         "first-topic",
     *         "max.message.bytes",
     *         "12800");
     */
    public static boolean alertConfig(ConfigResource.Type entityType,
                                      String entityName,
                                      String configName,
                                      String configValue) {
        try (AdminClient client = AdminClient.create(PROPS_ADMIN_CLIENT)) {
            ConfigEntry entry = new ConfigEntry(configName, configValue);
            Config configonfig = new Config(Arrays.asList(entry));
            ConfigResource resource = new ConfigResource(entityType, entityName);
            Map<ConfigResource, Config> mapConfig =
                Collections.singletonMap(resource, configonfig);
            client.alterConfigs(mapConfig).all().get();
            return true;
        }
        catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return false;
    }
    //endregion

    //region constructors
    private KafkaKit() { }
    //endregion
}
