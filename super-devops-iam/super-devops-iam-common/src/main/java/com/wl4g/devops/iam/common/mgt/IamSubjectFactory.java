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
package com.wl4g.devops.iam.common.mgt;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.devops.common.constants.IAMDevOpsConstants.KEY_AUTHC_TOKEN;
import static com.wl4g.devops.common.constants.IAMDevOpsConstants.KEY_DATA_CIPHER_KEY;
import static com.wl4g.devops.iam.common.utils.IamSecurityHolder.getSessionId;
import static com.wl4g.devops.tool.common.lang.Assert2.hasText;
import static com.wl4g.devops.tool.common.lang.Assert2.hasTextOf;
import static com.wl4g.devops.tool.common.lang.Assert2.notNullOf;
import static com.wl4g.devops.tool.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.security.MessageDigest.isEqual;
import static java.util.Objects.isNull;

import javax.servlet.http.HttpServletRequest;

import static org.apache.commons.codec.digest.HmacUtils.getHmacSha1;
import static org.apache.shiro.web.util.WebUtils.getCleanParam;
import static org.apache.shiro.web.util.WebUtils.toHttp;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.RememberMeAuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;
import org.apache.shiro.web.subject.WebSubjectContext;

import com.wl4g.devops.common.exception.iam.UnauthenticatedException;
import com.wl4g.devops.iam.common.authc.IamAuthenticationToken;
import com.wl4g.devops.iam.common.config.AbstractIamProperties;
import com.wl4g.devops.iam.common.config.AbstractIamProperties.ParamProperties;
import com.wl4g.devops.tool.common.log.SmartLogger;

/**
 * {@link org.apache.shiro.mgt.SubjectFactory Subject} implementation to be used
 * in CAS-enabled applications.
 *
 * @since 1.2
 */
public class IamSubjectFactory extends DefaultWebSubjectFactory {

	final protected SmartLogger log = getLogger(getClass());

	final protected AbstractIamProperties<? extends ParamProperties> config;

	public IamSubjectFactory(AbstractIamProperties<? extends ParamProperties> config) {
		notNullOf(config, "config");
		this.config = config;
	}

	@Override
	public Subject createSubject(SubjectContext context) {
		// the authenticated flag is only set by the SecurityManager after a
		// successful authentication attempt.
		//
		// although the SecurityManager 'sees' the submission as a successful
		// authentication, in reality, the
		// login might have been just a CAS rememberMe login. If so, set the
		// authenticated flag appropriately:
		if (context.isAuthenticated()) {
			AuthenticationToken token = context.getAuthenticationToken();
			if (!isNull(token) && token instanceof RememberMeAuthenticationToken) {
				RememberMeAuthenticationToken iamCasToken = (RememberMeAuthenticationToken) token;
				// set the authenticated flag of the context to true only if the
				// CAS subject is not in a remember me mode
				if (iamCasToken.isRememberMe()) {
					context.setAuthenticated(false);
				}
			}
		}

		/**
		 * Validation of enhanced sessionid additional signature.
		 * 
		 * @see {@link }
		 */
		if (context.isAuthenticated()) {
			try {
				assertRequestSignTokenValidity(context);
			} catch (UnauthenticatedException e) {
				// #Forced sets notauthenticated
				context.setAuthenticated(false);
				if (log.isDebugEnabled())
					log.debug(e.getMessage(), e);
				else
					log.warn(e.getMessage());
			}
		}

		return super.createSubject(context);
	}

	/**
	 * Assertion request signature validity.
	 * 
	 * @param context
	 * @throws UnauthenticatedException
	 * @see {@link AbstractIamAuthenticationFilter#makeLoggedResponse}
	 */
	final private void assertRequestSignTokenValidity(SubjectContext context) throws UnauthenticatedException {
		// Additional signature verification will only be performed on those
		// who have logged in successful.
		// e.g: Authentication requests or internal API requests does not
		// require signature verification.
		if (context.isAuthenticated() || isNull(context.getSession())) {
			return;
		}

		WebSubjectContext wsc = (WebSubjectContext) context;
		HttpServletRequest request = toHttp(wsc.resolveServletRequest());

		String sessionId = (String) getSessionId();
		String clientSign = getCleanParam(request, config.getParam().getClientSignName());
		String clientSecretKey = (String) wsc.getSession().getAttribute(KEY_DATA_CIPHER_KEY);
		IamAuthenticationToken authcToken = (IamAuthenticationToken) wsc.getSession().getAttribute(KEY_AUTHC_TOKEN);
		log.debug("Asserting session signature, sessionId:{}, clientSign:{}, clientSecretKey:{}, authcToken:{}", sessionId,
				clientSign, clientSecretKey, authcToken);

		// Only the password authentication is verified.
		// if (authcToken instanceof ClientSecretIamAuthenticationToken) {
		// hasText(clientSign, UnauthenticatedException.class, "client sign is
		// required");
		// hasText(sessionId, UnauthenticatedException.class, "sessionId is
		// required");
		// hasTextOf(clientSecretKey, "clientSecretKey"); // Shouldn't here
		//
		// // Calculate signature
		// final byte[] validSign =
		// getHmacSha1(clientSecretKey.getBytes(UTF_8)).doFinal(sessionId.getBytes(UTF_8));
		// log.debug("Asserted signatur, sessionId:{}, clientSign:{},
		// clientSecretKey:{}, validSign:{}, authcToken:{}",
		// clientSign, sessionId, clientSecretKey, validSign, authcToken);
		//
		// // Compare signature's
		// if (!isEqual(clientSign.getBytes(UTF_8), validSign)) {
		// throw new UnauthenticatedException(
		// format("Illegal authentication credentials signature. clientSign: {},
		// clientSecretKey: {}", clientSign,
		// clientSecretKey));
		// }
		// }

	}

}