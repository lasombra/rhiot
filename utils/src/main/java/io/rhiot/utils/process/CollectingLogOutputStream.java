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

package io.rhiot.utils.process;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.exec.LogOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collect the output and error streams.
 */
public class CollectingLogOutputStream extends LogOutputStream {

    private static final Logger LOG = LoggerFactory.getLogger(CollectingLogOutputStream.class);
    
    private final List<String> lines = new LinkedList();
    
    @Override protected void processLine(String line, int level) {
        LOG.debug(line);
        lines.add(line);
    }
    
    public List<String> getLines() {
        return lines;
    }
}