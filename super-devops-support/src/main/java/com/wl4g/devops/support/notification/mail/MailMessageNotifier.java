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
package com.wl4g.devops.support.notification.mail;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.wl4g.devops.support.notification.AbstractMessageNotifier;

public class MailMessageNotifier extends AbstractMessageNotifier<MailNotifyProperties, MailMessageWrapper> {

	/**
	 * Java mail sender.
	 */
	@Autowired
	protected JavaMailSender mailSender;

	@Override
	public NotifierKind kind() {
		return NotifierKind.Mail;
	}

	/**
	 * Send mail messages.
	 * 
	 * @param simpleMessages
	 */
	@Override
	public void send(MailMessageWrapper message) {
		if (message.hasSimpleMessage()) {
			try {
				SimpleMailMessage msg = message.getSimpleMessage();
				// Add "<>" symbol to send out?
				/*
				 * Preset from account, otherwise it would be wrong: 501 mail
				 * from address must be same as authorization user.
				 */
				msg.setFrom(msg.getFrom() + "<" + config.getFromUser() + ">");
				mailSender.send(msg);
			} catch (Exception e) {
				log.error(String.format("Failed to sent mail. message - %s", message), ExceptionUtils.getRootCauseMessage(e));
			}
		} else {
			throw new UnsupportedOperationException();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Object sendForReply(MailMessageWrapper message) {
		throw new UnsupportedOperationException();
	}

}
