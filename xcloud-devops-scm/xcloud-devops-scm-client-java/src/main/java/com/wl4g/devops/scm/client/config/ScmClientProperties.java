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
package com.wl4g.devops.scm.client.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wl4g.devops.scm.common.config.BaseScmProperties;

import static com.wl4g.components.common.lang.Assert2.notEmpty;
import static com.wl4g.components.common.lang.Assert2.state;

/**
 * SCM client properties.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年6月3日
 * @since
 */
public class ScmClientProperties extends BaseScmProperties {
	private static final long serialVersionUID = -2133451846066162424L;

	/** SCM server based URI. */
	private String baseUri = "http://localhost:14043/scm-server";

	/**
	 * SCM client network-card name, By default, the InetAddress that represents
	 * the loopback address. If IPv6 stack is available, it will refer to
	 * {@link java.net.InetAddress#getLocalHost LOCALHOST4}
	 */
	private String netcard;

	/**
	 * Fetch timeout on waiting to read data from the SCM Server.
	 */
	private int fetchReadTimeout = 8 * 1000;

	/** Minimum waiting time for long polling failure. */
	private long longPollDelay = 2 * 1000L;

	/** Maximum waiting time for long polling failure. */
	private long longPollMaxDelay = 15 * 1000L;

	private long refreshProtectIntervalMs = 10_000L;

	/**
	 * Refresh name-space(configuration filename)</br>
	 * SCM server publishing must be consistent with this configuration or the
	 * refresh configuration will fail.(Accurate matching)
	 */
	private List<String> namespaces = new ArrayList<>();

	/**
	 * Additional headers used to create the client request.
	 */
	private Map<String, String> headers = new HashMap<>();

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public String getNetcard() {
		return netcard;
	}

	public void setNetcard(String netcard) {
		this.netcard = netcard;
	}

	public int getFetchReadTimeout() {
		return fetchReadTimeout;
	}

	public void setFetchReadTimeout(int fetchReadTimeout) {
		state(fetchReadTimeout > 0, String.format("Invalid value for fetch read timeout for %s", fetchReadTimeout));
		this.fetchReadTimeout = fetchReadTimeout;
	}

	public long getLongPollDelay() {
		return longPollDelay;
	}

	public void setLongPollDelay(long longPollDelay) {
		state(longPollDelay > 0, String.format("Invalid value for long poll delay for %s", longPollDelay));
		this.longPollDelay = longPollDelay;
	}

	public long getLongPollMaxDelay() {
		return longPollMaxDelay;
	}

	public void setLongPollMaxDelay(long longPollMaxDelay) {
		state(longPollMaxDelay > 0, String.format("Invalid value for long poll max delay for %s", longPollMaxDelay));
		this.longPollMaxDelay = longPollMaxDelay;
	}

	public List<String> getNamespaces() {
		notEmpty(namespaces, String.format("Invalid value for namespaces for %s", namespaces));
		return namespaces;
	}

	public void setNamespaces(List<String> namespaces) {
		notEmpty(namespaces, String.format("Invalid value for namespaces for %s", namespaces));
		this.namespaces = namespaces;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public long getRefreshProtectIntervalMs() {
		return refreshProtectIntervalMs;
	}

	public void setRefreshProtectIntervalMs(long refreshProtectIntervalMs) {
		this.refreshProtectIntervalMs = refreshProtectIntervalMs;
	}

	/**
	 * Prefix for SCM configuration properties.
	 */
	final public static String AUTHORIZATION = "authorization";

}