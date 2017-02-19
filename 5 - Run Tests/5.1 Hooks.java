import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Hooks {
    private ThisRun thisRun = ThisRun.getInstance();
    private Properties config = null;
    private Logger logger = LogManager.getLogger(Hooks.class.getName());
    private DriverUtils driverUtils;

    @Before("@android")
    public void androidSetup(Scenario scenario) throws IOException, InvalidInputException {
        try {
            logNewScenario(scenario.getName());
            info(logger, "Started - '@android' Before Hook");
            thisRun.resetScenario(scenario.getName());
            thisRun.add(KEYS.TEST_TYPE, "android");
            thisRun.add(KEYS.CURRENT_SCENARIO, scenario);
            thisRun.add(KEYS.CURRENT_SCENARIO_NAME, scenario.getName());
            thisRun.dump();

            info(logger, "PROJECT_PATH - " + thisRun.getAsString(KEYS.PROJECT_PATH));

            loadScenarioSpecificProperties();
            debug(logger, "** Config properties - " + config.toString());

            DesiredCapabilities capabilities = getCommonDesiredCapabilities();
            driverUtils = new DriverUtils(config, capabilities);
            AndroidDriver driver = driverUtils.startAndroidDriver();
            thisRun.add(KEYS.ANDROID_DRIVER, driver);
            thisRun.dump();
        } catch (IOException e) {
            error(logger, "IOException", e);
            ScreenShotUtils.embedScreenShotInReport(scenario, scenario.getName(), "Device ID: " + config.getProperty(KEYS.ID.name()) + " - IOException - " + e.getMessage());
            throw e;
        } catch (InvalidInputException e) {
            error(logger, "InvalidInputException", e);
            ScreenShotUtils.embedScreenShotInReport(scenario, scenario.getName(), "Device ID: " + config.getProperty(KEYS.ID.name()) + " - InvalidInputException - " + e.getMessage());
            throw e;
        } catch (Exception e) {
            error(logger, "Exception", e);
            ScreenShotUtils.embedScreenShotInReport(scenario, scenario.getName(), "Device ID: " + config.getProperty(KEYS.ID.name()) + " - Exception - " + e.getMessage());
            throw e;
        } finally {
            info(logger, "Finished - '@android' Before Hook");
        }
    }

    @After("@android")
    public void androidTearDown(Scenario scenario) {
        info(logger, "Started - '@android' After Hook");
        ScreenShotUtils.embedScreenShotInReport(scenario, scenario.getName(), "Device ID: " + config.getProperty(KEYS.ID.name()) + " - Finished executing scenario - " + scenario.getName());
        if (null != driverUtils) {
            driverUtils.stopAndroidDriver(scenario);
        }
        info(logger, "Finished - '@android' After Hook");
        logScenarioComplete(scenario);
    }

    private void logScenarioComplete(Scenario scenario) {
        String scenarioFinished = "\n" +
                "\n--------------------------------------------------------------------------------------------\n" +
                "\tScenario: '" + scenario.getName() + "'\n" +
                "\tRunning on Device ID: '" + config.getProperty(KEYS.ID.name()) + "'\n" +
                "\t\tStatus: '" + scenario.getStatus() + "'" +
                "\n--------------------------------------------------------------------------------------------\n" +
                "\n";
        info(logger, scenarioFinished);
    }

    private void logNewScenario(String scenarioName) {
        String newScenarioLog = "\n" +
                "\n--------------------------------------------------------------------------------------------\n" +
                "\tScenario: '" + scenarioName + "'\n" +
                "\tRunning on Device ID: '" + System.getProperty(KEYS.ID.name()) + "'" +
                "\n--------------------------------------------------------------------------------------------\n" +
                "\n";
        info(logger, newScenarioLog);
    }

    private DesiredCapabilities getCommonDesiredCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, config.getProperty(KEYS.PLATFORM_NAME.name()));
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, config.getProperty(KEYS.PLATFORM_VERSION.name()));
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, config.getProperty(KEYS.DEVICE_MAKE.name()));
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
        capabilities.setCapability(MobileCapabilityType.UDID, config.getProperty(KEYS.ID.name()));
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, config.getProperty(KEYS.FULL_RESET.name()));
        capabilities.setCapability(MobileCapabilityType.NO_RESET, config.getProperty(KEYS.NO_RESET.name()));
        capabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, config.getProperty(KEYS.APP_PACKAGE.name()));
        capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, config.getProperty(KEYS.APP_ACTIVITY.name()));
        capabilities.setCapability(AndroidMobileCapabilityType.APP_WAIT_ACTIVITY, config.getProperty(KEYS.APP_WAIT_ACTIVITY.name()));
        capabilities.setCapability(AndroidMobileCapabilityType.APP_WAIT_DURATION,60000);
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 600);
        capabilities.setCapability("systemPort", Integer.parseInt(config.getProperty(KEYS.APPIUM_PORT.name())));
        return capabilities;
    }

    private void loadScenarioSpecificProperties() throws InvalidInputException, IOException {
        if (null == config) {
            config = new Properties();
            FileInputStream ip = new FileInputStream(new File(thisRun.getAsString(KEYS.PROJECT_PATH) + "/src/test/resources/appium.properties"));
            config.load(ip);
            thisRun.add(KEYS.CONFIG, config);
        }
        addPropertyWithDefaultIfValueNotAvailable(KEYS.ANDROID_APP_PATH, System.getProperty(KEYS.ANDROID_APP_PATH.name()));
        addProperty(KEYS.APP_TYPE, System.getProperty(KEYS.APP_TYPE.name()));
        addProperty(KEYS.API_LEVEL, System.getProperty(KEYS.API_LEVEL.name()));
        addProperty(KEYS.DEVICE_MAKE, System.getProperty(KEYS.DEVICE_MAKE.name()));
        addProperty(KEYS.PLATFORM_VERSION, System.getProperty(KEYS.PLATFORM_VERSION.name()));
        addProperty(KEYS.APPIUM_PORT, System.getProperty(KEYS.APPIUM_PORT.name()));
        addProperty(KEYS.NO_RESET, System.getProperty(KEYS.NO_RESET.name()));
        addProperty(KEYS.FULL_RESET, System.getProperty(KEYS.FULL_RESET.name()));
        addProperty(KEYS.ID, System.getProperty(KEYS.ID.name()));
    }

    private void addProperty(KEYS key, String value) throws InvalidInputException {
        if ((null == value) || (value.isEmpty())) {
            throw new InvalidInputException("Environment variable - '" + key.name() + "' expected, but NOT FOUND");
        } else {
            config.setProperty(key.name(), value);
        }
    }

    private void addPropertyWithDefaultIfValueNotAvailable(KEYS key, String value) {
        if (null == value) {
            value = "";
        }
        config.setProperty(key.name(), value);
    }
}
