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
package io.rhiot.cloudplatform.service.camera.api;

import java.util.List;

/**
 * Service for storing and analysing images taken by video cameras.
 */
public interface CameraService {

    /**
     * Analyzes image against presence of license plates.
     *
     * @param country expected country of the plate. Can be 'us' or 'eu'.
     * @param imageData binary data of an image to analyze
     * @return list of possible recognition matches. The best matches come first.
     */
    List<PlateMatch> recognizePlate(String country, byte[] imageData);

    void process(String deviceId, String country, byte[] imageData);

}
