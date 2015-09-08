/**
 * Licensed to the Camel Labs under one or more
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
package io.rhiot.steroids

import com.google.common.truth.Truth
import org.junit.Assert
import org.junit.Test

import static Steroids.bean
import static Steroids.beans

class ClasspathDependencyInjectionTest extends Assert {

    @Test
    void shouldCallCallback() {
        bean(ConfigurationCallback.class).get().configureMe()
    }

    @Test
    void shouldFindAllCallbacks() {
        Truth.assertThat(beans(ConfigurationCallback.class)).hasSize(1)
    }

    @Test
    void shouldNotFindBean() {
        Truth.assertThat(bean(NotImplementedCallback.class).isPresent()).isFalse()
    }

}