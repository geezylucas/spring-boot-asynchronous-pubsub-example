package com.geezylucas.orderservice;

import com.geezylucas.orderservice.dto.UserMessageDTO;
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
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ConfirmControllerTests {

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
    private WebTestClient webClient;

    @Autowired
    private PubSubSubscriberTemplate subscriberTemplate;

    @Test
    void testConfirm() {
        webClient.post()
                .uri("/v1/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UserMessageDTO.builder()
                        .shipmentTrackingId(1)
                        .username("geezylucas")
                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        List<AcknowledgeablePubsubMessage> messages = await().until(() -> subscriberTemplate.pull("test-subscription", 10, true), not(empty()));

        assertEquals(1, messages.size());
        assertThat(messages.get(0).getPubsubMessage().getData().toStringUtf8()).contains("geezylucas");

        for (AcknowledgeablePubsubMessage msg : messages) {
            msg.ack();
        }
    }

    @AfterEach
    void teardown() {
        // Drain any messages that are still in the subscription so that they don't interfere with
        // subsequent tests.
        await().until(() -> subscriberTemplate.pullAndAck("test-subscription", 1000, true), hasSize(0));
    }

}
