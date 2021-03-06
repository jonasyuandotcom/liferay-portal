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

package com.liferay.portalweb.portal.permissions.webcontent.assertactions.entry.expire;

import com.liferay.portalweb.portal.BaseTestCase;
import com.liferay.portalweb.portal.util.RuntimeVariables;

/**
 * @author Brian Wing Shun Chan
 */
public class Member_AssertCannotExpireEntryTest extends BaseTestCase {
	public void testMember_AssertCannotExpireEntry() throws Exception {
		selenium.open("/web/guest/home/");

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (selenium.isElementPresent("link=Control Panel")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		selenium.clickAt("link=Control Panel",
			RuntimeVariables.replace("Control Panel"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();
		selenium.clickAt("link=Web Content",
			RuntimeVariables.replace("Web Content"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace("Web Content Name"),
			selenium.getText("//tr[3]/td[3]/a"));
		assertEquals(RuntimeVariables.replace("Approved"),
			selenium.getText("//tr[3]/td[4]/a"));
		assertNotEquals(RuntimeVariables.replace("Expired"),
			selenium.getText("//tr[3]/td[4]/a"));
		selenium.clickAt("//tr[3]/td/input",
			RuntimeVariables.replace("Web Content Name"));
		selenium.clickAt("//input[@value='Expire']",
			RuntimeVariables.replace("Expire"));
		selenium.waitForPageToLoad("30000");
		assertTrue(selenium.getConfirmation()
						   .matches("^Are you sure you want to expire the selected web content[\\s\\S]$"));
		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace(
				"You do not have the required permissions."),
			selenium.getText("//div[@class='portlet-msg-error']"));
		selenium.open("/web/guest/home/");

		for (int second = 0;; second++) {
			if (second >= 60) {
				fail("timeout");
			}

			try {
				if (selenium.isElementPresent("link=Control Panel")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.saveScreenShotAndSource();
		selenium.clickAt("link=Control Panel",
			RuntimeVariables.replace("Control Panel"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();
		selenium.clickAt("link=Web Content",
			RuntimeVariables.replace("Web Content"));
		selenium.waitForPageToLoad("30000");
		selenium.saveScreenShotAndSource();
		assertEquals(RuntimeVariables.replace("Web Content Name"),
			selenium.getText("//tr[3]/td[3]/a"));
		assertEquals(RuntimeVariables.replace("Approved"),
			selenium.getText("//tr[3]/td[4]/a"));
		assertNotEquals(RuntimeVariables.replace("Expired"),
			selenium.getText("//tr[3]/td[4]/a"));
	}
}