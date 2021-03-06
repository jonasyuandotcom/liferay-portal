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
public class ViewPage2DLFolder3Test extends BaseTestCase {
	public void testViewPage2DLFolder3() throws Exception {
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
		assertFalse(selenium.isTextPresent("DL Folder3 Name"));
		selenium.open("/web/document-library-page-scope-community/");

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible("link=DL Page2 Name")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		selenium.clickAt("link=DL Page2 Name",
			RuntimeVariables.replace("DL Page2 Name"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (RuntimeVariables.replace(
							"Documents and Media (DL Page2 Name)")
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
		assertEquals(RuntimeVariables.replace(
				"Documents and Media (DL Page2 Name)"),
			selenium.getText("//span[@class='portlet-title-text']"));
		selenium.clickAt("//a[contains(@class,'aui-paginator-next-link')]",
			RuntimeVariables.replace("Next"));

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (RuntimeVariables.replace("DL Folder3 Name")
										.equals(selenium.getText(
								"//span[@class='document-title']"))) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		assertTrue(selenium.isVisible(
				"//img[@src='/html/themes/classic/images/file_system/large/folder_full_document.png']"));
		assertEquals(RuntimeVariables.replace("DL Folder3 Name"),
			selenium.getText("//span[@class='document-title']"));
		selenium.clickAt("//span[@class='document-title']",
			RuntimeVariables.replace("DL Folder3 Name"));

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (RuntimeVariables.replace("Up")
										.equals(selenium.getText(
								"xPath=(//a[@class='browse-folder'])[1]"))) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace("Up"),
			selenium.getText("xPath=(//a[@class='browse-folder'])[1]"));
		assertEquals(RuntimeVariables.replace("DL Folder3 Name"),
			selenium.getText("xPath=(//a[@class='browse-folder'])[3]"));
		assertEquals(RuntimeVariables.replace("Documents Home"),
			selenium.getText("//span[@id='_20_breadcrumb']/ul/li[1]/span/a"));
		assertEquals(RuntimeVariables.replace("DL Folder3 Name"),
			selenium.getText("//span[@id='_20_breadcrumb']/ul/li[2]/span/a"));
		selenium.open("/web/document-library-page-scope-community/");

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible("link=DL Page3 Name")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		selenium.clickAt("link=DL Page3 Name",
			RuntimeVariables.replace("DL Page3 Name"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (RuntimeVariables.replace(
							"Documents and Media (DL Page2 Name)")
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
		assertEquals(RuntimeVariables.replace(
				"Documents and Media (DL Page2 Name)"),
			selenium.getText("//span[@class='portlet-title-text']"));
		assertTrue(selenium.isVisible(
				"xPath=(//img[@src='/html/themes/classic/images/file_system/large/folder_full_document.png'])[2]"));
		assertEquals(RuntimeVariables.replace("DL Folder3 Name"),
			selenium.getText("xPath=(//span[@class='document-title'])[2]"));
		selenium.clickAt("xPath=(//span[@class='document-title'])[2]",
			RuntimeVariables.replace("DL Folder3 Name"));

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (RuntimeVariables.replace("Up")
										.equals(selenium.getText(
								"xPath=(//a[@class='browse-folder'])[1]"))) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace("Up"),
			selenium.getText("xPath=(//a[@class='browse-folder'])[1]"));
		assertEquals(RuntimeVariables.replace("DL Folder3 Name"),
			selenium.getText("xPath=(//a[@class='browse-folder'])[3]"));
		assertEquals(RuntimeVariables.replace("Documents Home"),
			selenium.getText("//span[@id='_20_breadcrumb']/ul/li[1]/span/a"));
		assertEquals(RuntimeVariables.replace("DL Folder3 Name"),
			selenium.getText("//span[@id='_20_breadcrumb']/ul/li[2]/span/a"));
	}
}