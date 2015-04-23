/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import common.Constants;
import common.Parameters;
import common.WebdriverManager;

/**
 * Keyword base which has all basic keywords required for automation
 * 
 * @author Vishshady
 *
 */
public class KeywordBase implements Constants {
	private static int expTime = 0;
	private static Properties OR;
	private static Logger log = Logger.getLogger(KeywordBase.class);
	private static RemoteWebDriver d;
	private static WebDriverWait wait;

	/**
	 * @param d
	 *            driver
	 */
	public KeywordBase(Parameters p) {
		initOR();
		startDriver();
		d = WebdriverManager.getDriverInstance();
		wait = new WebDriverWait(d, 30);
		if (p.getExplicitWait() == 0)
			expTime = 30;
		else
			expTime = p.getExplicitWait();
	}

	private static void startDriver() {
		WebdriverManager.startDriver();
		WebdriverManager.loadHomePage();
	}

	/**
	 * 
	 */
	private static void initOR() {
		String file = new File(OBJECT_REPOSITORY_PATH).getAbsolutePath();

		try {
			FileInputStream fis = new FileInputStream(file);
			OR = new Properties();
			OR.load(fis);
		} catch (IOException e1) {
			log.error("Error in initilizing the OR.properties file", e1);
		}
	}

	/**
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @return
	 */
	private static By getBy(String locator) {
		By by = null;
		String locType = null;
		String[] a = null;
		String locValue = null;
		String var = null;
		if (locator.contains("$")) {
			if (locator.contains("|")) {
				String[] getVar = locator.substring(1).split("\\|");
				var = VariableStorage.getVar(getVar[0]);
				locator = getVar[1].trim();
				locator = OR.getProperty(locator);
				a = locator.split(">");
				locType = a[0];
				locValue = a[1].replace("$", var);
			}

		} else {
			locator = OR.getProperty(locator);
			a = locator.split(">");
			locType = a[0];
			locValue = a[1];
		}

		if (locType.equals("NAME")) {
			by = By.name(locValue);
		} else if (locType.equals("ID")) {
			by = By.id(locValue);
		} else if (locType.equals("XPATH")) {
			by = By.xpath(locValue);
		} else if (locType.equals("CSS")) {
			by = By.cssSelector(locValue);
		} else if (locType.equals("LINKTEXT")) {
			by = By.linkText(locValue);
		} else if (locType.equals("TEXT")) {
			// not implemented
		}

		log.info("Fetching element for :" + locType + " value :" + locValue);
		return by;
	}

	/**
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 * @return
	 */
	private static WebElement getElement(String locator, String... index) {
		WebElement element = null;

		try {
			By by = getBy(locator);
			if (index.length != 0)
				element = d.findElements(by).get(Integer.parseInt(index[0]));
			else
				element = d.findElement(by);
		} catch (NullPointerException n) {
			log.error("NullPointerException for : " + locator);
		}

		return element;
	}

	/**
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @return
	 */
	private static By getElementBy(String locator) {
		By by = null;

		try {
			by = getBy(locator);
		} catch (NullPointerException n) {
			log.error("NullPointerException for : " + locator);
		}

		return by;
	}

	/**
	 * Selenium click
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void click(String locator, String... index) {
		log.info("Clicking on " + locator);
		getElement(locator, index).click();
	}

	/**
	 * Javascript click
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void clickj(String locator, String... index) {
		log.info("Clicking on " + locator);
		executeScript("arguments[0].click();", locator, index);
	}

	/**
	 * Type into text box or upload files if element type = file
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param value
	 *            Value
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void type(String locator, String value, String... index) {
		log.info("Typing " + value);
		WebElement e = getElement(locator, index);
		e.clear();
		e.sendKeys(getString(value));
	}

	/**
	 * Clear the text box
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void clear(String locator, String... index) {
		getElement(locator, index).clear();
	}

	/**
	 * Select drop down by Index in the drop down element.
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param i
	 *            Drop downvalue index.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void selectIndex(String locator, int i, String... index) {
		log.info("Select by index " + locator);
		WebElement e = getElement(locator, index);
		Select select = new Select(e);
		select.selectByIndex(i);
	}

	/**
	 * Select drop down by value in the drop down element.
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param value
	 *            Value
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void selectValue(String locator, String value,
			String... index) {
		log.info("Select by value " + locator + " value " + value);
		WebElement e = getElement(locator, index);
		Select select = new Select(e);
		select.selectByValue(getString(value));
	}

	/**
	 * Select drop down by text in the drop down element.
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param value
	 *            Value
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void selectText(String locator, String value, String... index) {
		log.info("Select by text " + locator + " value " + value);
		WebElement e = getElement(locator, index);
		Select select = new Select(e);
		select.selectByVisibleText(getString(value));
	}

	/**
	 * Close browser window
	 */
	public static void close() {
		log.info("Close browser window");
		d.close();
	}

	/**
	 * To stop the driver
	 */
	@SuppressWarnings("unused")
	private static void stopDriver() {
		WebdriverManager.stopDriver();
	}

	/**
	 * Will perform javascript actions
	 * 
	 * @param value
	 */
	public static void executeScript(String value) {
		JavascriptExecutor js = (JavascriptExecutor) d;
		js.executeScript(getString(value));
	}

	/**
	 * Will perform javascript actions for an element
	 * 
	 * @param value
	 *            Value
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void executeScript(String value, String locator,
			String... index) {
		JavascriptExecutor js = (JavascriptExecutor) d;
		js.executeScript(value, getElement(locator, index));
	}

	/**
	 * To print message in report
	 * 
	 * @param message
	 */
	public static void log(String message) {
		MyTestContext.setMessage(getString(message));
	}

	/**
	 * Get text for an element and store in variable
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * 
	 * @param var
	 *            - Variable to store. For ex. <$a|getText> is the command.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void getText(String locator, String var, String... index) {
		String text = getElement(locator, index).getText();
		VariableStorage.setVar(var, text);
	}

	private static String getString(String key) {
		String s = key;
		if (key.substring(0, 1).contains("$")) {
			s = key.substring(1);
			return VariableStorage.getVar(s.trim());
		}
		return s;
	}

	/******************** iFrame, Window and Alert usage ********************/

	/**
	 * Switch to iFrame
	 * 
	 * @param frameIndex
	 */
	public static void switchFrameByIndex(String frameIndex) {
		log.info("Switching to frame " + frameIndex);
		d.switchTo().defaultContent();
		d.switchTo().frame(Integer.parseInt(frameIndex));
	}

	/**
	 * Switch to iFrame
	 * 
	 * @param frameValue
	 */
	public static void switchFrameByValue(String frameValue) {
		log.info("Switching to frame " + frameValue);
		d.switchTo().defaultContent();
		d.switchTo().frame(getString(frameValue));
	}

	/**
	 * Switch to iFrame
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 */
	public static void switchFrame(String locator) {
		log.info("Switching to frame " + locator);
		d.switchTo().defaultContent();
		d.switchTo().frame(getElement(locator));
	}

	/**
	 * Switch to new window opened
	 */
	public static void switchWindow() {
		for (String window : d.getWindowHandles()) {
			log.info("switching to window " + window);
			d.switchTo().window(window);
		}
	}

	/**
	 * Switch to default iFrame or Window
	 */
	public static void SwitchDefaultContent() {
		log.info("Switching to defaultContent");
		d.switchTo().defaultContent();
	}

	private static Alert isAlertPresent() {
		Alert a = null;
		try {
			a = d.switchTo().alert();
		} catch (NoAlertPresentException e) {
			log.error(e);
			Assert.fail(e + " Assert not present");
		}
		return a;
	}

	/**
	 * Accept if alert is present
	 */
	public static void acceptAlert() {
		Alert a = isAlertPresent();
		log.info("Accepting an alert");
		a.accept();
	}

	/**
	 * Dismiss if alert is present
	 */
	public static void declineAlert() {
		Alert a = isAlertPresent();
		log.info("Dismissing an alert");
		a.dismiss();
	}

	/********************* Custom Waits ************************/

	/**
	 * Wait until element is enabled
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param time
	 *            Maximum waiting time. Test fails beyond this time.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void waitForElementEnabled(final String locator, String time,
			final String... index) {
		log.info("customWaitForElementEnabled for time " + time);

		(new WebDriverWait(d, Integer.parseInt(time)))
				.until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver d) {
						return (getElement(locator, index)).isEnabled();
					}
				});
	}

	/**
	 * Wait until element is enabled
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void waitForElementEnabled(final String locator,
			final String... index) {
		log.info("customWaitForElementEnabled for time " + expTime);

		(new WebDriverWait(d, expTime)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return (getElement(locator, index)).isEnabled();
			}
		});
	}

	/**
	 * Wait until element present in DOM
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param time
	 *            Maximum waiting time. Test fails beyond this time.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void waitForElementBy(final String locator, String time,
			final String... index) {
		log.info("waitForElement for time " + time);

		(new WebDriverWait(d, Integer.parseInt(time)))
				.until(new ExpectedCondition<WebElement>() {
					public WebElement apply(WebDriver d) {
						return getElement(locator, index);
					}
				});
	}

	/**
	 * Wait until element present in DOM
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void waitForElement(final String locator,
			final String... index) {
		log.info("waitForElement for time " + expTime);

		(new WebDriverWait(d, expTime))
				.until(new ExpectedCondition<WebElement>() {
					public WebElement apply(WebDriver d) {
						return getElement(locator, index);
					}
				});

	}

	/**
	 * Wait until element is displayed
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param time
	 *            Maximum waiting time. Test fails beyond this time.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void waitForElementDisplayedBy(final String locator,
			String time, final String... index) {
		log.info("waitForElementByDisplayed " + time);

		(new WebDriverWait(d, Integer.parseInt(time)))
				.until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver d) {
						return getElement(locator, index).isDisplayed();
					}
				});
	}

	/**
	 * Wait until element is displayed
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void waitForElementDisplayed(final String locator,
			final String... index) {
		log.info("waitForElementByDisplayed " + expTime);

		(new WebDriverWait(d, expTime)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return getElement(locator, index).isDisplayed();
			}
		});
	}

	/**
	 * Wait until title is displayed
	 * 
	 * @param title
	 *            Title of the page.
	 */
	public static void waitForElementByTitle(String title) {
		wait.until(ExpectedConditions.titleContains(getString(title)));
	}

	/**
	 * Wait until element is invisible
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 */
	public static void waitForElementInvisible(final String locator) {
		wait.until(ExpectedConditions
				.invisibilityOfElementLocated(getElementBy(locator)));
	}

	/********************* Assert Statements ************************/
	/**
	 * @param s
	 * @return
	 */
	private static String[] splitValueMsg(String s) {
		String[] m = s.split("\\|");
		return m;
	}

	/**
	 * Assert text. Sequence should be expected value '|' message to print if it
	 * fails. For example : expectedValue|Expected value is not displayed.
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param text
	 *            Expected value and message.(Separated by pipe '|' if message
	 *            to be printed)
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void assertText(String locator, String text, String... index) {
		log.info("Assert by Text " + locator + " text " + text);

		if (splitValueMsg(getString(text)).length == 1)
			Assert.assertEquals(getElement(locator, index).getText(),
					getString(splitValueMsg(text)[0]));
		else
			Assert.assertEquals(getElement(locator, index).getText(),
					getString(splitValueMsg(text)[0]), splitValueMsg(text)[1]);
	}

	/**
	 * Assert by element value. Sequence should be expected value '|' message to
	 * print if it fails. For example : expectedValue|Expected value is not
	 * displayed.
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param value
	 *            Expected value and message.(Separated by pipe '|' if message
	 *            to be printed)
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void asserValue(String locator, String value, String... index) {
		log.info("Assert by value " + locator + " value " + value);

		if (splitValueMsg(value).length == 1)
			Assert.assertEquals(getElement(locator, index)
					.getAttribute("value"), getString(splitValueMsg(value)[0]));
		else
			Assert.assertEquals(getElement(locator, index).getText(),
					getString(splitValueMsg(value)[0]), splitValueMsg(value)[1]);

	}

	/**
	 * Assert title. Sequence should be expected value '|' message to print if
	 * it fails. For example : expectedValue|Expected value is not displayed.
	 * 
	 * @param title
	 *            Expected title of the page
	 */
	public static void assertTitle(String title) {
		log.info("Assert by title " + title);

		if (splitValueMsg(title).length == 1)
			Assert.assertEquals(d.getTitle(),
					getString(splitValueMsg(title)[0]));
		else
			Assert.assertEquals(d.getTitle(),
					getString(splitValueMsg(title)[0]), splitValueMsg(title)[1]);

	}

	/**
	 * Assert element is present. Sequence should be expected value '|' message
	 * to print if it fails. For example : expectedValue|Expected value is not
	 * displayed.
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param bool
	 *            Expected boolean value. (true or false)
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void assertElementPresent(String locator, String bool,
			String... index) {
		log.info("Assert by element " + locator + " bool " + bool);

		if (splitValueMsg(bool).length == 1)
			Assert.assertEquals(getElement(locator, index).isDisplayed(),
					Boolean.parseBoolean(getString(splitValueMsg(bool)[0])));
		else
			Assert.assertEquals(getElement(locator, index).isDisplayed(),
					Boolean.parseBoolean(getString(splitValueMsg(bool)[0])),
					splitValueMsg(bool)[1]);
	}

	/**
	 * Assert checkbox is selected. Sequence should be expected value '|'
	 * message to print if it fails. For example : expectedValue|Expected value
	 * is not displayed.
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param bool
	 *            Expected boolean value. (true or false)
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void assertCheckBoxSelected(String locator, String bool,
			String... index) {
		log.info("Assert by element checked" + locator + " boolean " + bool);

		if (splitValueMsg(bool).length == 1)
			Assert.assertEquals(getElement(locator, index).isSelected(),
					Boolean.parseBoolean(getString(splitValueMsg(bool)[0])));
		else
			Assert.assertEquals(getElement(locator, index).isSelected(),
					Boolean.parseBoolean(getString(splitValueMsg(bool)[0])),
					splitValueMsg(bool)[1]);

	}

	/**
	 * Assert selected drop down element option. Sequence should be expected
	 * value '|' message to print if it fails. For example :
	 * expectedValue|Expected value is not displayed.
	 * 
	 * @param locator
	 *            HTML element locator from Object Repository file.
	 * @param option
	 *            Expected selected option.
	 * @param index
	 *            (Optional) Index of an element. Applies only if more than 1
	 *            element present in HTML. 0 by default.
	 */
	public static void assertSelectedOption(String locator, String option,
			String... index) {
		log.info("Assert by SelectedOption " + locator + " option " + option);

		if (splitValueMsg(option).length == 1) {
			Select s = new Select(getElement(locator, index));
			Assert.assertEquals(s.getFirstSelectedOption().getText(),
					getString(splitValueMsg(option)[0]));
		} else
			Assert.assertEquals(getElement(locator, index).getText(),
					getString(splitValueMsg(option)[0]),
					splitValueMsg(option)[1]);
	}

}
