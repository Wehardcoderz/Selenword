/*******************************************************************************
 * Licensed to the Software Freedom Conservancy (SFC) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The SFC licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package driver;

import org.apache.log4j.Logger;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import common.MyTestContext;
import common.WebdriverManager;

/**
 * 
 * @author Vishshady
 *
 */
@Listeners(common.MyListener.class)
public class TestDriver {
	private static Logger log = Logger.getLogger(TestDriver.class);

	public String d;
	public String t;
	public String e;

	@Factory(dataProvider = "prepareData")
	public TestDriver(String t, String d, String e) {
		this.d = d;
		this.t = t;
		this.e = e;
	}

	@DataProvider(parallel = true)
	public static Object[][] prepareData() {
		return MyTestContext.prepareData();
	}

	// @AfterClass(alwaysRun = true)
	@AfterMethod(alwaysRun = true)
	public void tearDown() {
		log.info("Stopping " + t);
		WebdriverManager.stopDriver();
	}

	@Test()
	public void test() {
		MyTestContext.setTestStats(t, d);
		if (e.equalsIgnoreCase("False"))
			throw new SkipException("Skipping this test as Enables == False");
		new KeywordLauncher(t, d);
	}

}