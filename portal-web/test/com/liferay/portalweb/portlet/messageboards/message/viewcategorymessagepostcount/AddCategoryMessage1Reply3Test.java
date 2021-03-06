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

package com.liferay.portalweb.portlet.messageboards.message.viewcategorymessagepostcount;

import com.liferay.portalweb.portal.BaseTestCase;
import com.liferay.portalweb.portal.util.RuntimeVariables;

/**
 * @author Brian Wing Shun Chan
 */
public class AddCategoryMessage1Reply3Test extends BaseTestCase {
	public void testAddCategoryMessage1Reply3() throws Exception {
		selenium.open("/web/guest/home/");

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible("link=Message Boards Test Page")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		selenium.clickAt("link=Message Boards Test Page",
			RuntimeVariables.replace("Message Boards Test Page"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace("MB Category Name"),
			selenium.getText("//td[1]/a/strong"));
		selenium.clickAt("//td[1]/a/strong",
			RuntimeVariables.replace("MB Category Name"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();
		selenium.clickAt("link=MB Category Thread1 Message Subject",
			RuntimeVariables.replace("MB Category Thread1 Message Subject"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace("Reply"),
			selenium.getText(
				"//div[6]/table/tbody/tr[1]/td[2]/div[1]/ul/li[2]/span/a/span"));
		selenium.clickAt("//div[6]/table/tbody/tr[1]/td[2]/div[1]/ul/li[2]/span/a/span",
			RuntimeVariables.replace("Reply"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();
		assertEquals("RE: MB Category Thread1 Message Subject",
			selenium.getValue("//input[@id='_19_subject']"));
		Thread.sleep(5000);

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible(
							"//td[@id='cke_contents__19_editor']/iframe")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		selenium.selectFrame("//td[@id='cke_contents__19_editor']/iframe");
		selenium.type("//body",
			RuntimeVariables.replace("MB Category Thread1 Message Reply3 Body"));
		selenium.selectFrame("relative=top");
		selenium.saveScreenShotAndSource();
		selenium.clickAt("//input[@value='Publish']",
			RuntimeVariables.replace("Publish"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace(
				"Your request completed successfully."),
			selenium.getText("//div[@class='portlet-msg-success']"));
		assertEquals(RuntimeVariables.replace(
				"RE: MB Category Thread1 Message Subject"),
			selenium.getText("//h1[@class='header-title']"));
		assertEquals(RuntimeVariables.replace(
				"RE: MB Category Thread1 Message Subject"),
			selenium.getText("//a/strong"));
		assertEquals(RuntimeVariables.replace(
				"RE: MB Category Thread1 Message Subject"),
			selenium.getText("xPath=(//div[@class='subject']/a/strong)[2]"));
		assertEquals(RuntimeVariables.replace(
				"MB Category Thread1 Message Reply1 Body"),
			selenium.getText("xPath=(//div[@class='thread-body'])[2]"));
		assertEquals(RuntimeVariables.replace(
				"RE: MB Category Thread1 Message Subject"),
			selenium.getText("xPath=(//div[@class='subject']/a/strong)[3]"));
		assertEquals(RuntimeVariables.replace(
				"MB Category Thread1 Message Reply2 Body"),
			selenium.getText("xPath=(//div[@class='thread-body'])[3]"));
		assertEquals(RuntimeVariables.replace(
				"RE: MB Category Thread1 Message Subject"),
			selenium.getText("xPath=(//div[@class='subject']/a/strong)[4]"));
		assertEquals(RuntimeVariables.replace(
				"MB Category Thread1 Message Reply3 Body"),
			selenium.getText("xPath=(//div[@class='thread-body'])[4]"));
	}
}