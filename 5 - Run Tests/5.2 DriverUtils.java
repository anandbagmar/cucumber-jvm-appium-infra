import cucumber.api.Scenario;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DriverUtils {
    private Properties config;
    private DesiredCapabilities capabilities;
    private ThisRun thisRun = ThisRun.getInstance();
    private Logger logger = LogManager.getLogger(DriverUtils.class.getName());

    public DriverUtils(Properties config, DesiredCapabilities capabilities) {
        this.config = config;
        this.capabilities = capabilities;
    }

    public AndroidDriver startAndroidDriver() {
        info(logger,"LocalSetup - config - " + this.config.toString());
        AndroidDriver<MobileElement> driver = instantiateAndroidDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        return driver;
    }

    private AndroidDriver<MobileElement> instantiateAndroidDriver() {
        info(logger,"Instantiating Android driver");
        URL serverUrl = getAppiumServerURL();
        info(logger,"\twith capabilities - " + capabilities.toString());
        return new AndroidDriver<>(serverUrl, capabilities);
    }

    public void stopAndroidDriver(Scenario scenario) {
        stopAndroidDriver();
    }

    protected void turnOffWiFi() {
        ((AndroidDriver) thisRun.driver()).setConnection(Connection.WIFI);
    }

    private URL getAppiumServerURL() {
        URL serverUrl = null;
        try {
            serverUrl = new URL("http://0.0.0.0:" + config.getProperty(KEYS.APPIUM_PORT.name()) + "/wd/hub");
            info(logger,"Appium server url - " + serverUrl);
        } catch (MalformedURLException e) {
            error(logger,"MalformedURLException", e);
        }
        return serverUrl;
    }

    private void stopAndroidDriver() {
        info(logger,"Stop Appium's Android Driver");
        AppiumDriver driver = thisRun.driver();
        thisRun.dump();
        debug(logger,"Got driver from thisRun.driver");
        if (null == driver) {
            debug(logger,"Driver has ALREADY terminated. Nothing to 'quit'");
        } else {
            debug(logger,"Stop the driver");
            driver.quit();
        }
    }
}
