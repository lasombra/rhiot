/**
 * Licensed to the Rhiot under one or more
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
package io.rhiot.gateway.gps

import com.fasterxml.jackson.databind.ObjectMapper
import io.rhiot.cloudplatform.schema.gps.GpsCoordinates
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component;

import static io.rhiot.utils.Properties.stringProperty
import static io.rhiot.utils.Uuids.uuid
import static java.net.InetAddress.localHost;
import static org.apache.camel.model.dataformat.JsonLibrary.Jackson;

/**
 * Camel route reading the current position data from a GPSD-compatible device.
 */
@Component
@ConditionalOnProperty(name = 'gps_cloudlet_sync', havingValue = 'true')
class GpsDataStreamSyncRoutes extends RouteBuilder {

    def storeDirectory = stringProperty('gps_store_directory', '/var/rhiot/gps')

    def cloudletAddress = stringProperty('gps_cloudlet_address')

    def cloudletEndpoint = stringProperty('gps_cloudlet_endpoint', "netty4-http:http://${cloudletAddress}/api/document/save/GpsCoordinates")

    void configure() {
        from("file://${storeDirectory}?sortBy=file:modified").
                onException(Exception.class).maximumRedeliveries(100000).useExponentialBackOff().end().
                transform { Exchange exc ->
                    def clientCoordinates = new ObjectMapper().readValue(exc.in.getBody(String.class), GpsCoordinates.class)
                    def serverCoordinates = new GpsCoordinates()
                    serverCoordinates.client = getLocalHost().getHostName()
                    serverCoordinates.clientId = uuid()
                    serverCoordinates.timestamp = clientCoordinates.timestamp
                    serverCoordinates.lat = clientCoordinates.lat
                    serverCoordinates.lng = clientCoordinates.lng
                    if(serverCoordinates.enrich != null) {
                        serverCoordinates.enrich = clientCoordinates.enrich
                    }
                    [payload: serverCoordinates]
                }.
                marshal().json(Jackson).
                to(cloudletEndpoint);
    }

}
