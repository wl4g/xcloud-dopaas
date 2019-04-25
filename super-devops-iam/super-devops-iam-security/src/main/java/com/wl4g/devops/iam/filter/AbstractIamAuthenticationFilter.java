/*
 * Copyright 2015 the original author or authors.
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
package com.wl4g.devops.iam.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Assert;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.wl4g.devops.iam.common.config.AbstractIamProperties.StrategyProperties.DEFAULT_AUTHC_STATUS;
import static com.wl4g.devops.iam.common.config.AbstractIamProperties.StrategyProperties.DEFAULT_UNAUTHC_STATUS;
//import static com.wl4g.devops.common.constants.IAMDevOpsConstants.KEY_AUTHC_TOKEN;
import static com.wl4g.devops.common.constants.IAMDevOpsConstants.KEY_ERR_SESSION_SAVED;
import static com.wl4g.devops.common.constants.IAMDevOpsConstants.URI_LOGIN_SUBMISSION_BASE;

import com.wl4g.devops.common.exception.iam.AccessRejectedException;
import com.wl4g.devops.common.utils.Exceptions;
import com.wl4g.devops.common.utils.web.WebUtils2;
import com.wl4g.devops.common.utils.web.WebUtils2.ResponseType;
import com.wl4g.devops.common.web.RespBase.RetCode;
import com.wl4g.devops.iam.common.authc.IamAuthenticationToken;
import com.wl4g.devops.iam.common.cache.EnhancedCacheManager;
import com.wl4g.devops.iam.common.context.SecurityCoprocessor;
import com.wl4g.devops.iam.common.filter.IamAuthenticationFilter;
import com.wl4g.devops.iam.common.utils.SessionBindings;
import com.wl4g.devops.iam.config.BasedContextConfiguration.IamContextManager;
import com.wl4g.devops.iam.config.IamProperties;
import com.wl4g.devops.iam.context.ServerSecurityContext;
import com.wl4g.devops.iam.handler.AuthenticationHandler;

/**
 * Multiple channel login authentication submitted processing based filter
 * 
 * {@link org.apache.shiro.web.filter.mgt.DefaultFilterChainManager#proxy()}
 * {@link org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver#pathMatches()}
 * {@link org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver#getChain()}
 * {@link org.apache.shiro.web.servlet.AbstractShiroFilter#getExecutionChain()}
 * {@link org.apache.shiro.web.servlet.AbstractShiroFilter#executeChain()}
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月27日
 * @since
 */
public abstract class AbstractIamAuthenticationFilter<T extends IamAuthenticationToken> extends AuthenticatingFilter
		implements IamAuthenticationFilter {

	/**
	 * Login request parameters binding session key
	 */
	final public static String KEY_REQ_AUTH_PARAMS = AbstractIamAuthenticationFilter.class.getSimpleName() + ".REQ_AUTH_PARAMS";

	/**
	 * URI login submission base path for processing all shiro authentication
	 * filters submitted by login
	 */
	final public static String URI_BASE_MAPPING = URI_LOGIN_SUBMISSION_BASE;

	final protected Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * IAM security context handler
	 */
	final protected ServerSecurityContext context;

	/**
	 * IAM server configuration properties
	 */
	@Autowired
	protected IamProperties config;

	/**
	 * IAM authentication handler
	 */
	@Autowired
	protected AuthenticationHandler authHandler;

	/**
	 * IAM security coprocessor
	 */
	@Autowired
	protected SecurityCoprocessor coprocessor;

	/**
	 * Enhanced cache manager.
	 */
	@Autowired
	protected EnhancedCacheManager cacheManager;

	public AbstractIamAuthenticationFilter(IamContextManager manager) {
		Assert.notNull(manager, "'manager' is null, please check configure");
		this.context = manager.getServerSecurityContext();
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		/*
		 * The request path must be /login/general or /login/sms, etc. Just
		 * return to false directly, that is, let it execute
		 * this#onAccessDenied()
		 */
		return false;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Access denied url: {}", WebUtils.toHttp(request).getRequestURI());
		}

		Subject subject = SecurityUtils.getSubject();
		/*
		 * If logged-in, the login success logic is executed
		 */
		if (subject.isAuthenticated()) {
			try {
				onLoginSuccess(createToken(request, response), subject, request, response);
			} catch (Exception e) {
				log.error("Logged-in auto redirect to other applications failed", e);
			}
			// No need to continue, because onLoginSuccess has responded
			return false;
		}

		return executeLogin(request, response);
	}

	@Override
	protected IamAuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
		/*
		 * Pre-processing before authentication, For example, the implementation
		 * of restricting client IP white-list to prevent violent cracking of
		 * large number of submission login requests.
		 */
		if (!coprocessor.preAuthentication(this, request, response)) {
			throw new AccessRejectedException(
					String.format("Access rejected for remote IP:%s", WebUtils2.getHttpRemoteAddr(WebUtils.toHttp(request))));
		}

		// Client remote host
		String remoteHost = WebUtils2.getHttpRemoteAddr((HttpServletRequest) request);
		// From information
		String fromAppName = getFromAppName(request);
		String redirectUrl = getFromRedirectUrl(request);

		// Create authentication token
		return postCreateToken(remoteHost, fromAppName, redirectUrl, WebUtils.toHttp(request), WebUtils.toHttp(response));
	}

	protected abstract T postCreateToken(String remoteHost, String fromAppName, String redirectUrl, HttpServletRequest request,
			HttpServletResponse response) throws Exception;

	@SuppressWarnings({ "rawtypes" })
	@Override
	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response)
			throws Exception {
		IamAuthenticationToken tk = (IamAuthenticationToken) token;

		try {
			/*
			 * Save the token at the time of authentication, which can then be
			 * used for extended logic usage.
			 */
			// subject.getSession().setAttribute(KEY_AUTHC_TOKEN, tk);

			// From source application
			String fromAppName = getFromAppName(request);

			// Callback success redirect URI
			String successRedirectUrl = determineSuccessUrl(tk, subject, request, response); // prior
			Assert.hasText(successRedirectUrl, "Check the successful login redirection URL configure");

			// Granting ticket
			String grantTicket = null;
			if (StringUtils.hasText(fromAppName)) {
				grantTicket = authHandler.loggedin(fromAppName, subject).getGrantTicket();
			}

			// Response JSON response.
			if (isJSONResponse(request)) {
				try {
					// Make logged-in response message
					String logged = makeLoggedResponse(request, grantTicket, successRedirectUrl);
					if (log.isInfoEnabled()) {
						log.info("Login success response:{}", logged);
					}
					// Response to login success JSON message
					WebUtils2.writeJson(WebUtils.toHttp(response), logged);
				} catch (IOException e) {
					log.error("Login success response json error", e);
				}
			}
			// Redirect the login-page directly
			else {
				/*
				 * When the source application exists, the indication is that it
				 * needs to be redirected to the CAS client application, then
				 * grantTicket is required.
				 */
				Map params = null;
				if (StringUtils.hasText(grantTicket)) {
					params = Collections.singletonMap(config.getParam().getGrantTicket(), grantTicket);
					if (log.isInfoEnabled()) {
						log.info("Login success redirect to '{}', param:{}", successRedirectUrl, params);
					}
				}
				try {
					WebUtils.issueRedirect(request, response, successRedirectUrl, params, true);
				} catch (IOException e) {
					log.error("Login success redirection failed", e);
				}
			}

			// Post-handling of login success
			coprocessor.postAuthenticatingSuccess(tk, subject, request, response);

		} finally { // Clean-up
			cleanup(token, subject, request, response);
		}

		// Redirection has been responded and no further execution is required.
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException ae, ServletRequest request,
			ServletResponse response) {
		IamAuthenticationToken tk = (IamAuthenticationToken) token;

		Throwable thw = Exceptions.getRootCauses(ae);
		if (thw != null) {
			log.warn("On failure caused by: ", ae);
			/*
			 * See:i.w.DiabloExtraController#errReads()
			 */
			SessionBindings.bind(KEY_ERR_SESSION_SAVED, thw.getMessage());
		}

		// Callback failure redirect URI
		String failRedirectUrl = determineFailureUrl(tk, ae, request, response);

		// Get binding parameters
		Map queryParams = SessionBindings.getBindValue(KEY_REQ_AUTH_PARAMS);

		// Response JSON message
		if (isJSONResponse(request)) {
			try {
				final String failed = makeFailedResponse(failRedirectUrl, request, queryParams, thw);
				if (log.isInfoEnabled()) {
					log.info("Response unauthentication. {}", failed);
				}
				WebUtils2.writeJson(WebUtils.toHttp(response), failed);
			} catch (IOException e) {
				log.error("Response unauthentication json error", e);
			}
		}
		// Redirects the login page directly
		else {
			try {
				if (log.isInfoEnabled()) {
					log.info("Redirect to login: {}", failRedirectUrl);
				}
				WebUtils.issueRedirect(request, response, failRedirectUrl, queryParams, true);
			} catch (IOException e1) {
				log.error("Redirect to login failed.", e1);
			}
		}

		// Post-handling of login failure
		coprocessor.postAuthenticatingFailure(tk, ae, request, response);

		// Redirection has been responded and no further execution is required.
		return false;
	}

	/**
	 * Determine is the JSON interactive strategy, or get it from
	 * session(flexible API).
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isJSONResponse(ServletRequest request) {
		// Using dynamic parameter
		String respType = request.getParameter(config.getParam().getResponseType()); // Priority
		if (!StringUtils.hasText(respType)) {
			respType = SessionBindings.extParameterValue(KEY_REQ_AUTH_PARAMS, config.getParam().getResponseType());
		}
		if (log.isDebugEnabled()) {
			log.debug("Using response type:{}", respType);
		}

		return ResponseType.isJSONResponse(respType, WebUtils.toHttp(request));
	}

	/**
	 * Get the name from the application from the login request, or get it from
	 * session(flexible API).
	 * 
	 * @return
	 */
	protected String getFromAppName(ServletRequest request) {
		String fromAppName = WebUtils.getCleanParam(request, config.getParam().getApplication()); // Priority
		return StringUtils.hasText(fromAppName) ? fromAppName
				: SessionBindings.extParameterValue(KEY_REQ_AUTH_PARAMS, config.getParam().getApplication());
	}

	/**
	 * Get the name from the redirectUrl from the login request, or get it from
	 * session(flexible API).
	 * 
	 * @return
	 */
	protected String getFromRedirectUrl(ServletRequest request) {
		String redirectUrl = WebUtils.getCleanParam(request, config.getParam().getRedirectUrl()); // prerogative
		return StringUtils.hasText(redirectUrl) ? redirectUrl
				: SessionBindings.extParameterValue(KEY_REQ_AUTH_PARAMS, config.getParam().getRedirectUrl());
	}

	/**
	 * Cleaning-up.(e.g.Save request parameters)
	 * 
	 * @param token
	 * @param subject
	 * @param request
	 * @param response
	 */
	protected void cleanup(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) {
		/*
		 * Clean See:AuthenticatorAuthenticationFilter#bindRequestParameters()
		 */
		SessionBindings.unbind(KEY_REQ_AUTH_PARAMS);

		/*
		 * Clean error messages. See:i.w.DiabloExtraController#errReads()
		 */
		SessionBindings.unbind(KEY_ERR_SESSION_SAVED);
	}

	/**
	 * Make logged-in response message.
	 * 
	 * @param request
	 *            Servlet request
	 * @param grantTicket
	 *            logged information model
	 * @param redirectUrl
	 *            login success redirect URL
	 * @return
	 */
	private String makeLoggedResponse(ServletRequest request, String grantTicket, String redirectUrl) {
		Assert.notNull(redirectUrl, "'redirectUrl' must not be null");

		// Redirection URL
		StringBuffer uri = new StringBuffer(redirectUrl);
		if (StringUtils.hasText(grantTicket)) {
			if (uri.lastIndexOf("?") > 0) {
				uri.append("&");
			} else {
				uri.append("?");
			}
			uri.append(config.getParam().getGrantTicket()).append("=").append(grantTicket);
		}

		// Relative path processing
		String url = uri.toString();
		if (url.startsWith("/")) {
			url = WebUtils2.getRFCBaseURI(WebUtils.toHttp(request), true) + uri;
		}

		// Make message
		return config.getStrategy().makeResponse(RetCode.OK.getCode(), DEFAULT_AUTHC_STATUS, "Login successful", url);
	}

	/**
	 * Make login failed response message.
	 * 
	 * @param failureRedirectUrl
	 *            failure redirect URL
	 * @param request
	 *            Servlet request
	 * @param queryParams
	 *            Redirected query configuration parameters
	 * @param thw
	 *            Exception object
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private String makeFailedResponse(String failureRedirectUrl, ServletRequest request, Map queryParams, Throwable thw) {
		String errmsg = (thw != null && StringUtils.hasText(thw.getMessage())) ? thw.getMessage() : "No logged";
		// Make message
		return config.getStrategy().makeResponse(RetCode.UNAUTHC.getCode(), DEFAULT_UNAUTHC_STATUS, errmsg, failureRedirectUrl);
	}

	/**
	 * Determine the URL of the login success redirection, default: successURL,
	 * can support customization.
	 * 
	 * @param token
	 * @param subject
	 * @param request
	 * @param response
	 * @return
	 */
	private String determineSuccessUrl(IamAuthenticationToken token, Subject subject, ServletRequest request,
			ServletResponse response) {
		// Callback success redirect URI
		String successRedirectUrl = getFromRedirectUrl(request);
		if (!StringUtils.hasText(successRedirectUrl)) {
			successRedirectUrl = getSuccessUrl(); // fallback
		}
		// Determine success URL
		successRedirectUrl = context.determineLoginSuccessUrl(successRedirectUrl, token, subject, request, response);

		Assert.hasText(successRedirectUrl, "'successRedirectUrl' is empty, please check the configure");
		WebUtils2.cleanURI(successRedirectUrl); // symbol check
		return successRedirectUrl;
	}

	/**
	 * Determine the URL of the login failure redirection, default: loginURL,
	 * can support customization.
	 * 
	 * 
	 * @param token
	 * @param ae
	 * @param request
	 * @param response
	 * @return
	 */
	private String determineFailureUrl(IamAuthenticationToken token, AuthenticationException ae, ServletRequest request,
			ServletResponse response) {
		String loginUrl = context.determineLoginFailureUrl(getLoginUrl(), token, ae, request, response);
		Assert.hasText(loginUrl, "'loginUrl' is empty, please check the configure");
		WebUtils2.cleanURI(loginUrl); // check
		return loginUrl;
	}

	/**
	 * Get filter name
	 */
	public abstract String getName();

}