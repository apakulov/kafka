/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package org.apache.kafka.copycat.connector;

import org.apache.kafka.copycat.errors.CopycatException;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ConnectorReconfigurationTest {

    @Test
    public void testDefaultReconfigure() throws Exception {
        TestConnector conn = new TestConnector(false);
        conn.reconfigure(new Properties());
        assertEquals(conn.stopOrder, 0);
        assertEquals(conn.configureOrder, 1);
    }

    @Test(expected = CopycatException.class)
    public void testReconfigureStopException() throws Exception {
        TestConnector conn = new TestConnector(true);
        conn.reconfigure(new Properties());
    }

    private static class TestConnector extends Connector {
        private boolean stopException;
        private int order = 0;
        public int stopOrder = -1;
        public int configureOrder = -1;

        public TestConnector(boolean stopException) {
            this.stopException = stopException;
        }

        @Override
        public void start(Properties props) {
            configureOrder = order++;
        }

        @Override
        public Class<? extends Task> getTaskClass() {
            return null;
        }

        @Override
        public List<Properties> getTaskConfigs(int count) {
            return null;
        }

        @Override
        public void stop() {
            stopOrder = order++;
            if (stopException)
                throw new CopycatException("error");
        }
    }
}