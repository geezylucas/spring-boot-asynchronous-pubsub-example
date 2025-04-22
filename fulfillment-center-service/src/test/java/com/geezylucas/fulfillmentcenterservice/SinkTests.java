package com.geezylucas.fulfillmentcenterservice;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.geezylucas.fulfillmentcenterservice.model.UserMessage;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@FunctionalSpringBootTest
@Testcontainers
class SinkTests {

    private static final String PROJECT_ID = "test-project";

    @Container
    private static final PubSubEmulatorContainer pubsubEmulator = new PubSubEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/google-cloud-cli:441.0.0-emulators")
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gcp.pubsub.emulator-host", pubsubEmulator::getEmulatorEndpoint);
    }

    @BeforeAll
    static void setup() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("dns:///" + pubsubEmulator.getEmulatorEndpoint())
                .usePlaintext()
                .build();

        TransportChannelProvider channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

        TopicAdminClient topicAdminClient = TopicAdminClient.create(TopicAdminSettings.newBuilder()
                .setCredentialsProvider(NoCredentialsProvider.create())
                .setTransportChannelProvider(channelProvider)
                .build());

        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(NoCredentialsProvider.create())
                .build());

        PubSubAdmin admin = new PubSubAdmin(() -> PROJECT_ID, topicAdminClient, subscriptionAdminClient);

        admin.createTopic("test-topic");
        admin.createSubscription("test-subscription", "test-topic");

        admin.close();
        channel.shutdown();

    }

    // By default, autoconfiguration will initialize application default credentials.
    // For testing purposes, don't use any credentials. Bootstrap w/ NoCredentialsProvider.
    @TestConfiguration
    static class PubSubEmulatorConfiguration {
        @Bean
        CredentialsProvider googleCredentials() {
            return NoCredentialsProvider.create();
        }
    }

    @Autowired
    private FunctionCatalog catalog;

    @Test
    void logUserMessageTest() {
        ListAppender<ILoggingEvent> logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(Sink.class)).addAppender(logWatcher);

        Consumer<UserMessage> consumer = catalog.lookup(Consumer.class, "logUserMessage");
        consumer.accept(UserMessage.builder()
                .shipmentTrackingId(1)
                .username("test-user")
                .createdAt(LocalDateTime.of(2021, 1, 1, 12, 0, 0))
                .build());

        assertThat(logWatcher.list.get(0).getFormattedMessage()).contains("New shipment tracking id received 1, username test-user at 2021-01-01T12:00");

        ((Logger) LoggerFactory.getLogger(Sink.class)).detachAndStopAllAppenders();
    }

}
