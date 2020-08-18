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
package com.wl4g.devops.scm.common.command;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link ReportCommand}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2018-08-18
 * @since
 */
public class ReportCommand extends WatchCommand {
	final private static long serialVersionUID = 2523769504519533902L;

	private Collection<ChangedRecord> changedRecords;

	public ReportCommand(Collection<ChangedRecord> changedRecords) {
		super();
		this.changedRecords = changedRecords;
	}

	public Collection<ChangedRecord> getChangedRecords() {
		return changedRecords;
	}

	public void setChangedRecords(Collection<ChangedRecord> changedRecords) {
		this.changedRecords = changedRecords;
	}

	@Override
	public String toString() {
		return "ReportInfo [changedRecords=" + changedRecords + "]";
	}

	/**
	 * {@link ChangedRecord}
	 *
	 * @since
	 */
	public static class ChangedRecord {

		private Set<String> changedKeys = new HashSet<>();
		private WatchCommandResult result = new WatchCommandResult();

		public ChangedRecord() {
			super();
		}

		public ChangedRecord(Set<String> changedKeys, WatchCommandResult result) {
			super();
			this.changedKeys = changedKeys;
			this.result = result;
		}

		public Set<String> getChangedKeys() {
			return changedKeys;
		}

		public void setChangedKeys(Set<String> changedKeys) {
			this.changedKeys = changedKeys;
		}

		public WatchCommandResult getConfigResult() {
			return result;
		}

		public void setresult(WatchCommandResult result) {
			this.result = result;
		}

		@Override
		public String toString() {
			return "ChangedInfo [changedKeys=" + changedKeys + ", result=" + result + "]";
		}

	}

}