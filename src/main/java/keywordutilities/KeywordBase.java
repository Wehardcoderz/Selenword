package keywordutilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;

import common.Constants;
import common.Parameters;
import common.WebdriverManager;

public class KeywordBase {
	Parameters parameters;
	public static Properties OR;
	private static Logger log = Logger.getLogger(KeywordBase.class);

	KeywordBase() {
		parameters = new Parameters();
	}

	public static void initOR() {
		String file = new File(Constants.OBJECT_REPOSITORY_PATH)
				.getAbsolutePath();
		try (FileInputStream fis = new FileInputStream(file)) {
			OR = new Properties();
			OR.load(fis);
		} catch (IOException e1) {
			System.out.println(e1.getMessage());
			log.error("Error in initilizing the OR.properties file", e1);
		}
	}

	public static By getBy(String locator) {
		By by = null;
		String[] a = locator.split(">");
		String locType = a[0];
		String locValue = a[1];
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
		}
		log.info("for :"+locType+" value :"+locValue);
		return by;
	}

	public static WebElement getElement(String locator) {
		initOR();
		String loc = OR.getProperty(locator);
		WebElement element = null;
		By by = getBy(loc);
		element = WebdriverManager.getDriverInstance().findElement(by);
		return element;
	}

	public static void startDriver(String browser) {
		WebdriverManager.setupDriver(browser);
	 WebdriverManager.getDriverInstance();
	}

	public static void click(String s) {
		getElement(s).click();
	}

	public static void type(String locator, String val) {
		WebElement e = getElement(locator);
		e.clear();
		e.sendKeys(val);
	}

	public static void clear(String locator) {
		getElement(locator).clear();
	}

	public static void selectIndex(String locator, int i) {
		WebElement e = getElement(locator);
		Select select = new Select(e);
		select.selectByIndex(i);
	}

	public static void selectValue(String locator, String value) {
		WebElement e = getElement(locator);
		Select select = new Select(e);
		select.selectByValue(value);
	}

	public static void selectText(String locator, String value) {
		WebElement e = getElement(locator);
		Select select = new Select(e);
		select.selectByVisibleText(value);
	}

	public static void stopDriver() {
	WebdriverManager.stopDriver();
	}
	
	public static void main(String[] args) throws IOException {
		startDriver("firefox");
		type("searchBox", "hehe");
	}

}