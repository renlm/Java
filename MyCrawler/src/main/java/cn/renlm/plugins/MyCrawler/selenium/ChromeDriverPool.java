package cn.renlm.plugins.MyCrawler.selenium;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import cn.hutool.setting.Setting;
import lombok.extern.slf4j.Slf4j;

/**
 * 网页驱动
 * 
 * @author Renlm
 *
 */
@Slf4j
class ChromeDriverPool {
	private final static int STAT_RUNNING = 1;
	private final static int STAT_CLODED = 2;

	private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);
	private List<WebDriver> webDriverList = Collections.synchronizedList(new ArrayList<WebDriver>());
	private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>();

	private final Setting chromeSetting;
	private final int capacity;
	private WebDriver mDriver = null;

	public ChromeDriverPool(Setting chromeSetting, int capacity) {
		this.chromeSetting = chromeSetting;
		this.capacity = capacity;
	}

	public void configure() throws IOException {
		String driverPath = chromeSetting.getStr("driverPath");
		String windowSize = chromeSetting.getStr("windowSize", "1415,1000");

		ChromeOptions options = new ChromeOptions();
		options.setHeadless(true);
		options.addArguments("disable-infobars");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--window-size=" + windowSize);

		System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, driverPath);

		options.setHeadless(true);
		mDriver = new ChromeDriver(options);
	}

	public WebDriver get() throws InterruptedException {
		checkRunning();
		WebDriver poll = innerQueue.poll();
		if (poll != null) {
			return poll;
		}
		if (webDriverList.size() < capacity) {
			synchronized (webDriverList) {
				if (webDriverList.size() < capacity) {
					try {
						configure();
						innerQueue.add(mDriver);
						webDriverList.add(mDriver);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return innerQueue.take();
	}

	public void returnToPool(WebDriver webDriver) {
		checkRunning();
		innerQueue.add(webDriver);
	}

	protected void checkRunning() {
		if (!stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) {
			throw new IllegalStateException("Already closed!");
		}
	}

	public void closeAll() {
		boolean b = stat.compareAndSet(STAT_RUNNING, STAT_CLODED);
		if (!b) {
			throw new IllegalStateException("Already closed!");
		}
		for (WebDriver webDriver : webDriverList) {
			log.info("Quit webDriver" + webDriver);
			webDriver.quit();
			webDriver = null;
		}
	}
}