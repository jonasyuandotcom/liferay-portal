/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.util.servlet;

import com.liferay.portal.kernel.util.UnsyncPrintWriterPool;

import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @author Brian Wing Shun Chan
 */
public class NullServletResponse extends HttpServletResponseWrapper {

	public NullServletResponse(HttpServletResponse response) {
		super(response);

		_servletOutputStream = new NullServletOutputStream();
		_printWriter = UnsyncPrintWriterPool.borrow(_servletOutputStream);
	}

	@Override
	public ServletOutputStream getOutputStream() {
		return _servletOutputStream;
	}

	@Override
	public PrintWriter getWriter() {
		return _printWriter;
	}

	/*public void sendError(int status) throws IOException {
	}

	public void sendError(int status, String msg) throws IOException {
	}

	public void sendRedirect(String location) throws IOException {
	}*/

	private PrintWriter _printWriter;
	private ServletOutputStream _servletOutputStream;

}