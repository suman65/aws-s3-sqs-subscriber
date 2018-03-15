package com.greenark.amr.prepaid.energy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import javax.jms.Session;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.greenark.amr.prepaid.energy.listeners.SQSListener;

@Configuration
public class JMSSQSConfig {

	@Value("${aws.accesskey}")
	private String accessKey;

	@Value("${aws.secretaccesskey}")
	private String secretAccessKey;

	@Value("${task.queue.endpoint}")
	private String endpoint;

	// @Value("${task.queue.name}")
	private String queueName = "task-queuetask-queuedf06a432-d32b-4afb-83bc-3530633c25f8";

	@Autowired
	private SQSListener sqsListener;

	@Bean
	public DefaultMessageListenerContainer jmsListenerContainer() {

		SQSConnectionFactory sqsConnectionFactory = SQSConnectionFactory.builder()
				.withAWSCredentialsProvider(new DefaultAWSCredentialsProviderChain()).withEndpoint(endpoint)
				.withAWSCredentialsProvider(awsCredentialsProvider).withNumberOfMessagesToPrefetch(10).build();

		DefaultMessageListenerContainer factory = new DefaultMessageListenerContainer();
		factory.setConnectionFactory(sqsConnectionFactory);
		factory.setDestinationName(queueName);

		factory.setDestinationResolver(new DynamicDestinationResolver());
		factory.setConcurrency("1-5");
		factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);

		factory.setMessageListener(sqsListener);

		return factory;
	}

	@Bean
	public JmsTemplate createJMSTemplate() {

		SQSConnectionFactory sqsConnectionFactory = SQSConnectionFactory.builder()
				.withAWSCredentialsProvider(awsCredentialsProvider).withEndpoint(endpoint)
				.withNumberOfMessagesToPrefetch(10).build();

		JmsTemplate jmsTemplate = new JmsTemplate(sqsConnectionFactory);
		jmsTemplate.setDefaultDestinationName(queueName);
		jmsTemplate.setDeliveryPersistent(false);

		return jmsTemplate;
	}

	@Bean
	public AWSCredentials awsCredentials() {
		return new BasicAWSCredentials(accessKey, secretAccessKey);
	}

	/**
	 * AWS Connection Factory
	 */
	/*
	 * @Bean public SQSConnectionFactory connectionFactory() {
	 * SQSConnectionFactory.Builder factoryBuilder = new
	 * SQSConnectionFactory.Builder( Region.getRegion(Regions.AP_SOUTH_1));
	 * factoryBuilder.setAwsCredentialsProvider(new AWSCredentialsProvider() {
	 * 
	 * @Override public AWSCredentials getCredentials() { return
	 * awsCredentials(); }
	 * 
	 * @Override public void refresh() { }
	 * 
	 * }); return factoryBuilder.build(); }
	 * 
	 */

	private final AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
		public AWSCredentials getCredentials() {
			return new BasicAWSCredentials(accessKey, secretAccessKey);
		}

		public void refresh() {

		}
	};

}
