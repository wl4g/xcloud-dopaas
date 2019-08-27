/*
 * Copyright 2017 ~ 2025 the original author or authors.
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
package com.wl4g.devops.iam.config;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import static com.wl4g.devops.common.constants.IAMDevOpsConstants.BEAN_DELEGATE_MSG_SOURCE;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.wl4g.devops.common.bean.iam.ApplicationInfo;
import com.wl4g.devops.common.bean.iam.IamAccountInfo;
import com.wl4g.devops.common.bean.iam.IamAccountInfo.Parameter;
import com.wl4g.devops.common.bean.iam.SocialConnectInfo;
import com.wl4g.devops.common.exception.iam.BindingConstraintsException;
import com.wl4g.devops.common.exception.iam.IamException;
import com.wl4g.devops.iam.common.i18n.SessionDelegateMessageBundle;
import com.wl4g.devops.iam.configure.ServerSecurityConfigurer;

/**
 * Based context configuration
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年3月24日
 * @since
 */
public class BasedContextConfiguration {

	//
	// Locale i18n configuration.
	//

	/**
	 * Build a proxy message resourcer. Note that this bean can instantiate
	 * multiple different 'basenames', so the name must be unique
	 * 
	 * @param beanFactory
	 * @return
	 */
	@Bean(BEAN_DELEGATE_MSG_SOURCE)
	@ConditionalOnMissingBean
	public SessionDelegateMessageBundle sessionDelegateMessageBundle() {
		return new SessionDelegateMessageBundle(getClass());
	}

	//
	// Context's configuration
	//

	@Bean
	public ServerSecurityConfigurer serverSecurityConfigurer() {
		return new CheckImpledServerSecurityConfigurer();
	}

	/**
	 * Check whether ServerSecurityConfigurer has been implemented.
	 * 
	 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0 2019-07-27
	 * @since
	 */
	public static class CheckImpledServerSecurityConfigurer implements ServerSecurityConfigurer, InitializingBean {

		@Override
		public String determineLoginSuccessUrl(String successUrl, AuthenticationToken token, Subject subject,
				ServletRequest request, ServletResponse response) {
			return null;
		}

		@Override
		public String determineLoginFailureUrl(String loginUrl, AuthenticationToken token, AuthenticationException ae,
				ServletRequest request, ServletResponse response) {
			return null;
		}

		@Override
		public ApplicationInfo getApplicationInfo(String applicationName) {
			return null;
		}

		@Override
		public List<ApplicationInfo> findApplicationInfo(String... applicationNames) {
			return null;
		}

		@Override
		public IamAccountInfo getIamAccount(Parameter parameter) {
			return null;
		}

		@Override
		public boolean isApplicationAccessAuthorized(String principal, String application) {
			return false;
		}

		@Override
		public String findRoles(String principal, String application) {
			return null;
		}

		@Override
		public String findPermissions(String principal, String application) {
			return null;
		}

		@Override
		public <T extends SocialConnectInfo> List<T> findSocialConnections(String principal, String provider) {
			return null;
		}

		@Override
		public void bindSocialConnection(SocialConnectInfo social) throws BindingConstraintsException {
		}

		@Override
		public void unbindSocialConnection(SocialConnectInfo social) throws BindingConstraintsException {
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			String errmsg = "\n\n==>>\tWhen you rely on Iam security as a plug-in, you must implement the '"
					+ ServerSecurityConfigurer.class.getName() + "' interface yourself !\n";
			throw new IamException(errmsg);
		}

	}

}