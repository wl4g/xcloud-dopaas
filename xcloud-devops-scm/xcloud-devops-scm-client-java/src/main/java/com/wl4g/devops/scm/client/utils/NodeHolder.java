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
package com.wl4g.devops.scm.client.utils;

import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.devops.scm.client.config.ScmClientProperties;
import com.wl4g.devops.scm.common.command.GenericCommand.ConfigNode;

/**
 * Instance information.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年4月1日
 * @since
 */
public class NodeHolder {

	protected final SmartLogger log = getLogger(getClass());

	/** That application instance. */
	private final ConfigNode configNode;

	public NodeHolder(ScmClientProperties config) {
		this.configNode = new ConfigNode(config.getAvailableHostInfo().getIpAddress(), config.getServiceId());
	}

	/**
	 * Gets {@link ConfigNode}
	 * 
	 * @return
	 */
	public ConfigNode getConfigNode() {
		return configNode;
	}

}