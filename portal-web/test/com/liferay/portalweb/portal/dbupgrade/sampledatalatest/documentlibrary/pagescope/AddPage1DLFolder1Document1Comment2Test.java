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

package com.liferay.portalweb.portal.dbupgrade.sampledatalatest.documentlibrary.pagescope;

import com.liferay.portalweb.portal.BaseTestCase;
import com.liferay.portalweb.portal.util.RuntimeVariables;

/**
 * @author Brian Wing Shun Chan
 */
public class AddPage1DLFolder1Document1Comment2Test extends BaseTestCase {
	public void testAddPage1DLFolder1Document1Comment2()
		throws Exception {
		selenium.open("/web/document-library-page-scope-community/");

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible("link=DL Page1 Name")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		selenium.clickAt("link=DL Page1 Name",
			RuntimeVariables.replace("DL Page1 Name"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (RuntimeVariables.replace("Documents and Media")
										.equals(selenium.getText(
								"//span[@class='portlet-title-text']"))) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace("Documents and Media"),
			selenium.getText("//span[@class='portlet-title-text']"));
		assertEquals(RuntimeVariables.replace("DL Folder1 Name"),
			selenium.getText("//span[@class='document-title']"));
		selenium.clickAt("//span[@class='document-title']",
			RuntimeVariables.replace("DL Folder1 Name"));

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible(
							"//span[@id='_20_breadcrumb']/ul/li[2]/span/a")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace("DL Folder1 Name"),
			selenium.getText("//span[@id='_20_breadcrumb']/ul/li[2]/span/a"));
		assertEquals(RuntimeVariables.replace("DL Folder1 Document1 Title.doc"),
			selenium.getText("//span[@class='document-title']"));
		selenium.clickAt("//span[@class='document-title']",
			RuntimeVariables.replace("DL Folder1 Document1 Title.doc"));

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible(
							"//fieldset[contains(@class,'add-comment')]/div/span/a")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace("Add Comment"),
			selenium.getText(
				"//fieldset[contains(@class,'add-comment')]/div/span/a"));
		selenium.clickAt("//fieldset[contains(@class,'add-comment')]/div/span/a",
			RuntimeVariables.replace("Add Comment"));

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible("//textarea[@name='_20_postReplyBody0']")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		selenium.type("//textarea[@name='_20_postReplyBody0']",
			RuntimeVariables.replace("DL Folder1 Document1 Comment2 Body"));
		selenium.saveScreenShotAndSource();
		selenium.clickAt("//input[@value='Reply']",
			RuntimeVariables.replace("Reply"));

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible(
							"//div[@id='_20_discussion-status-messages']")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace(
				"Your request processed successfully."),
			selenium.getText("//div[@id='_20_discussion-status-messages']"));
		assertEquals(RuntimeVariables.replace(
				"DL Folder1 Document1 Comment1 Body"),
			selenium.getText(
				"xPath=(//div[@class='lfr-discussion-message'])[1]"));
		assertEquals(RuntimeVariables.replace(
				"DL Folder1 Document1 Comment2 Body"),
			selenium.getText(
				"xPath=(//div[@class='lfr-discussion-message'])[2]"));
	}
}