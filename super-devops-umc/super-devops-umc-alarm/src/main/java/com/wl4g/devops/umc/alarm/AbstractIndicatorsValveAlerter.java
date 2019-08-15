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
package com.wl4g.devops.umc.alarm;

import com.wl4g.devops.common.bean.umc.model.MetricValue;
import com.wl4g.devops.support.cache.JedisService;
import com.wl4g.devops.support.lock.SimpleRedisLockManager;
import com.wl4g.devops.support.task.GenericTaskRunner;
import com.wl4g.devops.support.task.GenericTaskRunner.RunProperties;
import com.wl4g.devops.umc.config.AlarmProperties;

import static com.wl4g.devops.common.constants.UMCDevOpsConstants.KEY_CACHE_ALARM_METRIC_QUEUE;
import static com.wl4g.devops.common.utils.lang.Collections2.ensureList;
import static java.lang.Math.abs;
import static java.util.Collections.emptyList;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Abstract collection metric valve alerter.
 *
 * @author wangl.sir
 * @version v1.0 2019年7月5日
 */
public abstract class AbstractIndicatorsValveAlerter extends GenericTaskRunner<RunProperties> implements IndicatorsValveAlerter {

	final protected Logger log = LoggerFactory.getLogger(getClass());

	/** REDIS service */
	final protected JedisService jedisService;

	/**
	 * REDIS lock manager.
	 */
	final protected SimpleRedisLockManager lockManager;

	public AbstractIndicatorsValveAlerter(JedisService jedisService, SimpleRedisLockManager lockManager, AlarmProperties config) {
		super(config);
		Assert.notNull(jedisService, "JedisService is null, please check config.");
		Assert.notNull(lockManager, "LockManager is null, please check config.");
		this.jedisService = jedisService;
		this.lockManager = lockManager;
	}

	@Override
	public void run() {
		// Ignore
	}

	@Override
	public void alarm(MetricAggregateWrapper wrap) {
		getWorker().execute(() -> doHandleAlarm(wrap));
	}

	/**
	 * Do handling alarm.
	 * 
	 * @param agwrap
	 */
	protected abstract void doHandleAlarm(MetricAggregateWrapper agwrap);

	// --- Metric time queue. ---

	/**
	 * Offer metric values in time windows.
	 * 
	 * @param collectAddr
	 *            collector address
	 * @param value
	 *            metric value
	 * @param gatherTime
	 *            gather time-stamp.
	 * @param now
	 *            current date time-stamp.
	 * @param ttl
	 *            time-to-live
	 * @return
	 */
	protected List<MetricValue> offerTimeWindowQueue(String collectAddr, Double value, long gatherTime, long now, long ttl) {
		String timeWindowKey = getTimeWindowQueueCacheKey(collectAddr);
		// To solve the concurrency problem of metric window queue in
		// distributed environment.
		Lock lock = lockManager.getLock(timeWindowKey);

		List<MetricValue> metricVals = emptyList();
		try {
			if (lock.tryLock(10L, TimeUnit.SECONDS)) {
				metricVals = ensureList(doPeekMetricValueQueue(collectAddr));
				metricVals.add(new MetricValue(gatherTime, value));

				// Check & clean expired metrics.
				Iterator<MetricValue> it = metricVals.iterator();
				while (it.hasNext()) {
					if (abs(now - it.next().getGatherTime()) >= ttl) {
						it.remove();
					}
				}
				// Offer to queue.
				doOfferMetricValueQueue(collectAddr, ttl, metricVals);
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		} finally {
			lock.unlock();
		}
		return metricVals;
	}

	/**
	 * GET metric values queue by collect address.
	 * 
	 * @param collectAddr
	 * @return
	 */
	protected List<MetricValue> doPeekMetricValueQueue(String collectAddr) {
		String timeWindowKey = getTimeWindowQueueCacheKey(collectAddr);
		return jedisService.getObjectList(timeWindowKey, MetricValue.class);
	}

	/**
	 * Storage metric values to cache.
	 * 
	 * @param collectAddr
	 * @param ttl
	 * @param metricVals
	 */
	protected List<MetricValue> doOfferMetricValueQueue(String collectAddr, long ttl, List<MetricValue> metricVals) {
		String timeWindowKey = getTimeWindowQueueCacheKey(collectAddr);
		jedisService.setObjectList(timeWindowKey, metricVals, (int) ttl);
		return metricVals;
	}

	// --- Cache key. ---

	protected String getTimeWindowQueueCacheKey(String collectAddr) {
		Assert.hasText(collectAddr, "Collect addr must not be empty");
		return KEY_CACHE_ALARM_METRIC_QUEUE + collectAddr;
	}

}