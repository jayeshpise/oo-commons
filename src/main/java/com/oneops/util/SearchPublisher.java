/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.util;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class SearchPublisher extends AbstractMessagePublisher {

	protected final String SEARCH_QUEUE = "search.stream";
	
	protected MessageProducer producer;

	private SearchJmsSender searchSender;
	
	protected boolean searchPublishAsync;

	protected ActiveMQConnectionFactory syncConnFactory;
	
	protected ActiveMQConnectionFactory asyncConnFactory;
	
	public void init() {
		setConnFactory();
		try {
			super.init();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		searchSender.initialize(session, producer);
	}

	private void setConnFactory() {
		if (searchPublishAsync && asyncConnFactory != null) {
			this.connFactory = asyncConnFactory;
		}
		if (!searchPublishAsync && syncConnFactory != null) {
			this.connFactory = syncConnFactory;
		}
	}
	
	@Override
	protected void createProducers(Session session) throws JMSException {
		Destination destination = session.createQueue(SEARCH_QUEUE);
		producer = session.createProducer(destination);
		setProducerProperties(producer);
	}
	
	public void publish(MessageData data) {
		if (searchPublishAsync) {
			searchSender.executeAsync(data);
		}
		else {
			searchSender.executeSync(data);
		}
	}
	
	@Override
	protected void closeProducers() throws JMSException {
		producer.close();
	}

	public void destroy() {
		super.destroy();
	}

	public void setSearchSender(SearchJmsSender searchSender) {
		this.searchSender = searchSender;
	}

	public void setSyncConnFactory(ActiveMQConnectionFactory syncConnFactory) {
		this.syncConnFactory = syncConnFactory;
	}

	public void setAsyncConnFactory(ActiveMQConnectionFactory asyncConnFactory) {
		this.asyncConnFactory = asyncConnFactory;
	}

	public void setSearchPublishAsync(boolean searchPublishAsync) {
		this.searchPublishAsync = searchPublishAsync;
	}
	
}
