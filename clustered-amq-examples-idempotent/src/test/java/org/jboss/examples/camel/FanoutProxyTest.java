/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.examples.camel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/spring/applicationContext.xml")
@DisableJmx(true)
public class FanoutProxyTest {

  private static final Logger log = LoggerFactory.getLogger(FanoutProxyTest.class);
  
  private static final String MESSAGE_ID_HEADER = "UniqueMessageID";
  private static final String BROKER_URL = "tcp://localhost:61616";
  private static final String REST_URL = "http://localhost:8080/rest/record";

  @Autowired
  private CamelContext camelContext;
  
  @Before
  public void init() throws Exception {
    camelContext.addComponent("amq", ActiveMQComponent.activeMQComponent(BROKER_URL));
  }

  @Test
  public void testFanoutMessage() throws Exception {
    String body = "Hello world!";
    String id = UUID.randomUUID().toString();
    ProducerTemplate producer = camelContext.createProducerTemplate();
    
    Map<String, Object> jmsHeaders = new HashMap<>();
    jmsHeaders.put(MESSAGE_ID_HEADER, id);
    producer.sendBodyAndHeaders("amq:queue:org.jboss.examples.MessageQueue", body, jmsHeaders);
    
    try { Thread.sleep(5000L); } catch (InterruptedException e) {}
    
    Map<String, Object> httpHeaders = new HashMap<>();
    httpHeaders.put(Exchange.HTTP_QUERY, "GET");
    httpHeaders.put(Exchange.HTTP_PATH, id);
    Object response = producer.requestBodyAndHeaders(String.format("jetty:%s", REST_URL), (Object) null, httpHeaders);
    response = new String((byte[]) response);
    log.info(String.format("Response: '%s'", response));
    Assert.assertEquals(String.format("[{\"DUP_ID\":\"%s\",\"MESSAGE\":\"%s\"}]", id, body), response);
  }
}
