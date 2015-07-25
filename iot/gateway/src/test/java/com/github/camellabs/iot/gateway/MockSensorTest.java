/**
 * Licensed to the Camel Labs under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.camellabs.iot.gateway;

import com.github.camellabs.iot.vertx.camel.CamelContextFactories;
import org.apache.activemq.broker.BrokerService;
import org.apache.camel.EndpointInject;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.UnknownHostException;

import static java.lang.System.setProperty;
import static org.springframework.util.SocketUtils.findAvailableTcpPort;

public class MockSensorTest extends Assert {

    // Collaborators fixtures

    static int mqttPort = findAvailableTcpPort();

    @BeforeClass
    public static void beforeClass() throws Exception {
        BrokerService broker = new BrokerService();
        broker.setBrokerName(MockSensorTest.class.getName());
        broker.setPersistent(false);
        broker.addConnector("mqtt://localhost:" + mqttPort);
        broker.start();

        CamelContextFactories.camelContext().addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("paho:mock?brokerUrl=tcp://localhost:" + mqttPort).
                        to("mock:test");
            }
        });

        setProperty("camellabs_iot_gateway_mock_sensor", "true");
        setProperty("camellabs_iot_gateway_mock_sensor_consumer", "true");
        setProperty("camellabs_iot_gateway_mock_sensor_consumer_mqtt_broker_url", "tcp://localhost:" + mqttPort);

        new VertxGateway().start();
    }

    // Tests

    @Test
    public void shouldSendMockEventsToTheMqttServer() throws InterruptedException {
        MockEndpoint mockEndpoint = CamelContextFactories.camelContext().getEndpoint("mock:test", MockEndpoint.class);
        mockEndpoint.setMinimumExpectedMessageCount(1000);
        mockEndpoint.assertIsSatisfied();
    }

}