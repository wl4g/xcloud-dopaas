/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
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
package com.wl4g.devops.support.notification;

import static com.wl4g.devops.tool.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.devops.support.notification.AbstractMessageNotifier.NotifyProperties;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link AbstractMessageNotifier}
 * 
 * @param <C>
 * @param <T>
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020年1月9日 v1.0.0
 * @see
 */
public abstract class AbstractMessageNotifier<C extends NotifyProperties, T extends NotifyMessage> implements MessageNotifier<T> {
	final protected Logger log = getLogger(getClass());

	@Autowired
	protected C config;

	/**
	 * Base message notify properties configuration.
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version 2020年1月8日 v1.0.0
	 * @see
	 */
	public abstract static class NotifyProperties {

		private String serverEndpoint;

		public String getServerEndpoint() {
			return serverEndpoint;
		}

		public void setServerEndpoint(String serverEndpoint) {
			this.serverEndpoint = serverEndpoint;
		}

	}

}
