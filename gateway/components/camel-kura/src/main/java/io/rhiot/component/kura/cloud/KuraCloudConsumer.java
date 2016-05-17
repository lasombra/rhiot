/**
 * Licensed to the Rhiot under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rhiot.component.kura.cloud;

import org.apache.camel.*;
import org.apache.camel.impl.DefaultConsumer;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudClientListener;
import org.eclipse.kura.message.KuraPayload;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.rhiot.component.kura.cloud.KuraCloudConstants.*;
import static org.apache.camel.builder.ExchangeBuilder.anExchange;

public class KuraCloudConsumer extends DefaultConsumer implements CloudClientListener {

    private ScheduledExecutorService executorService;

    private CloudClient cloudClient;

    public KuraCloudConsumer(Endpoint endpoint, Processor processor, CloudClient cloudClient) {
        super(endpoint, processor);
        this.cloudClient = cloudClient;
    }

    // Life-cycle

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        executorService = getEndpoint().getCamelContext().getExecutorServiceManager().newDefaultScheduledThreadPool(this, "executor");
        cloudClient.addCloudClientListener(this);
        getEndpoint().getCamelContext().addStartupListener(new StartupListener() {
            @Override
            public void onCamelContextStarted(CamelContext context, boolean alreadyStarted) throws Exception {
                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            cloudClient.subscribe(getEndpoint().getTopic(), 0);
                        } catch (KuraException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 5, TimeUnit.SECONDS);
            }
        });
        log.debug("Starting CloudClientListener.");
    }

    @Override
    protected void doStop() throws Exception {
        getEndpoint().getCamelContext().getExecutorServiceManager().shutdown(executorService);
        cloudClient.unsubscribe(getEndpoint().getTopic());
        cloudClient.removeCloudClientListener(this);
        log.debug("Stopping CloudClientListener.");
        super.doStop();
    }

    // CloudClientListener callbacks

    @Override
    public void onControlMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {
        onInternalMessageArrived(deviceId, appTopic, msg, qos, retain, true);
    }

    @Override
    public void onMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {
        onInternalMessageArrived(deviceId, appTopic, msg, qos, retain, false);
    }

    @Override
    public void onConnectionLost() {
        log.debug("Executing empty 'onConnectionLost' callback.");
    }

    @Override
    public void onConnectionEstablished() {
        log.debug("Executing empty 'onConnectionLost' callback.");
    }

    @Override
    public void onMessageConfirmed(int messageId, String appTopic) {
        log.debug("Executing empty 'onMessageConfirmed' callback with message ID {} and application topic {}.", messageId, appTopic);
    }

    @Override
    public void onMessagePublished(int messageId, String appTopic) {
        log.debug("Executing empty 'onMessagePublished' callback with message ID {} and application topic {}.", messageId, appTopic);
    }

    // Helpers

    private void onInternalMessageArrived(String deviceId, String appTopic, KuraPayload message, int qos, boolean retain,
                                          boolean control) {
        log.debug("Received message with deviceId {}, application topic {}.", deviceId, appTopic);
        Exchange exchange = anExchange(getEndpoint().getCamelContext()).withBody(message)
                .withHeader(CAMEL_KURA_CLOUD_TOPIC, appTopic)
                .withHeader(CAMEL_KURA_CLOUD_DEVICEID, deviceId)
                .withHeader(CAMEL_KURA_CLOUD_QOS, qos)
                .withHeader(CAMEL_KURA_CLOUD_CONTROL, control)
                .withHeader(CAMEL_KURA_CLOUD_RETAIN, retain).build();
        exchange.setFromEndpoint(getEndpoint());
        try {
            getProcessor().process(exchange);
        } catch (Exception e) {
            handleException("Error while processing an incoming message:", e);
        }
    }

    // Getters

    @Override
    public KuraCloudEndpoint getEndpoint() {
        return (KuraCloudEndpoint) super.getEndpoint();
    }

}
