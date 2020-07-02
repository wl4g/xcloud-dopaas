package com.wl4g.devops.coss.client.channel.netty.parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.wl4g.devops.components.tools.common.io.ByteStreamUtils;
import com.wl4g.devops.coss.client.channel.netty.MediaType;

/**
 * Implementation of {@link HttpMessageParser} that can read and write
 * strings.
 *
 * <p>
 * By default, this converter supports all media types
 * ({@code &#42;&#47;&#42;}), and writes with a {@code Content-Type} of
 * {@code text/plain}. This can be overridden by setting the
 * {@link #setSupportedMediaTypes supportedMediaTypes} property.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 */
public class StringHttpMessageParser extends AbstractHttpMessageParser<String> {

	public static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

	private volatile List<Charset> availableCharsets;

	private boolean writeAcceptCharset = true;

	/**
	 * A default constructor that uses {@code "ISO-8859-1"} as the default
	 * charset.
	 * 
	 * @see #StringHttpMessageConverter(Charset)
	 */
	public StringHttpMessageParser() {
		this(DEFAULT_CHARSET);
	}

	/**
	 * A constructor accepting a default charset to use if the requested content
	 * type does not specify one.
	 */
	public StringHttpMessageParser(Charset defaultCharset) {
		super(defaultCharset, MediaType.TEXT_PLAIN, MediaType.ALL);
	}

	/**
	 * Indicates whether the {@code Accept-Charset} should be written to any
	 * outgoing request.
	 * <p>
	 * Default is {@code true}.
	 */
	public void setWriteAcceptCharset(boolean writeAcceptCharset) {
		this.writeAcceptCharset = writeAcceptCharset;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return String.class == clazz;
	}

	@Override
	protected String readInternal(Class<? extends String> clazz, HttpInputMessage inputMessage) throws IOException {
		Charset charset = getContentTypeCharset(inputMessage.getHeaders().getContentType());
		return ByteStreamUtils.copyToString(inputMessage.getBody(), charset);
	}

	@Override
	protected Long getContentLength(String str, MediaType contentType) {
		Charset charset = getContentTypeCharset(contentType);
		try {
			return (long) str.getBytes(charset.name()).length;
		} catch (UnsupportedEncodingException ex) {
			// should not occur
			throw new IllegalStateException(ex);
		}
	}

	@Override
	protected void writeInternal(String str, HttpOutputMessage outputMessage) throws IOException {
		if (this.writeAcceptCharset) {
			outputMessage.getHeaders().setAcceptCharset(getAcceptedCharsets());
		}
		Charset charset = getContentTypeCharset(outputMessage.getHeaders().getContentType());
		ByteStreamUtils.copy(str, charset, outputMessage.getBody());
	}

	/**
	 * Return the list of supported {@link Charset}s.
	 * <p>
	 * By default, returns {@link Charset#availableCharsets()}. Can be
	 * overridden in subclasses.
	 * 
	 * @return the list of accepted charsets
	 */
	protected List<Charset> getAcceptedCharsets() {
		if (this.availableCharsets == null) {
			this.availableCharsets = new ArrayList<Charset>(Charset.availableCharsets().values());
		}
		return this.availableCharsets;
	}

	private Charset getContentTypeCharset(MediaType contentType) {
		if (contentType != null && contentType.getCharset() != null) {
			return contentType.getCharset();
		} else {
			return getDefaultCharset();
		}
	}

}