package Functions;

import StepDefinitions.Hooks;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SeleniumFunctions {

    WebDriver driver;

    public SeleniumFunctions() {
        driver = Hooks.driver;
    }

    /******** Page path ********/
    public static String FileName = "";
    public static String PagesFilePath = "src/test/resources/Pages/";

    /******** Scenario Test Data ********/
    public static Map<String, String> ScenarioData = new HashMap<>();
    public static String Environment = "";
    public String ElementText = "";
    public static Map<String, String> HandleMyWindows = new HashMap<>();

    /******** Test Properties Config ********/
    public static Properties prop = new Properties();
    public static InputStream in = SeleniumFunctions.class.getResourceAsStream("../test.properties");

    /******** Log Attribute ********/
    private static Logger log = Logger.getLogger(SeleniumFunctions.class);

    private static String GetFieldBy = "";
    private static String ValueToFind = "";
    public static final int EXPLICIT_TIMEOUT = 5;
    public static boolean isDisplayed = Boolean.parseBoolean(null);

    public static Object readJson() throws Exception {
        FileReader reader = new FileReader(PagesFilePath + FileName);
        try {
            if (reader != null) {
                JSONParser jsonParser = new JSONParser();
                return jsonParser.parse(reader);
            } else {
                return null;
            }
        } catch (FileNotFoundException | NullPointerException e) {
            log.error("ReadEntity: No existe el archivo " + FileName);
            throw new IllegalStateException("ReadEntity: No existe el archivo " + FileName, e);
        }
    }

    public static JSONObject ReadEntity(String element) throws Exception {
        JSONObject Entity = null;
        JSONObject jsonObject = (JSONObject) readJson();
        Entity = (JSONObject) jsonObject.get(element);
        log.info(Entity.toJSONString());
        return Entity;

    }

    public static By getCompleteElement(String element) throws Exception {
        By result = null;
        JSONObject Entity = ReadEntity(element);

        GetFieldBy = (String) Entity.get("GetFieldBy");
        ValueToFind = (String) Entity.get("ValueToFind");

        if ("className".equalsIgnoreCase(GetFieldBy)) {
            result = By.className(ValueToFind);
        } else if ("cssSelector".equalsIgnoreCase(GetFieldBy)) {
            result = By.cssSelector(ValueToFind);
        } else if ("id".equalsIgnoreCase(GetFieldBy)) {
            result = By.id(ValueToFind);
        } else if ("linkText".equalsIgnoreCase(GetFieldBy)) {
            result = By.linkText(ValueToFind);
        } else if ("name".equalsIgnoreCase(GetFieldBy)) {
            result = By.name(ValueToFind);
        } else if ("link".equalsIgnoreCase(GetFieldBy)) {
            result = By.partialLinkText(ValueToFind);
        } else if ("tagName".equalsIgnoreCase(GetFieldBy)) {
            result = By.tagName(ValueToFind);
        } else if ("xpath".equalsIgnoreCase(GetFieldBy)) {
            result = By.xpath(ValueToFind);
        }
        return result;
    }

    public String readProperties(String property) throws IOException {
        prop.load(in);
        return prop.getProperty(property);
    }

    public void SaveInScenario(String key, String text) {
        if (!this.ScenarioData.containsKey(key)) {
            this.ScenarioData.put(key,text);
            log.info(String.format("Save as Scenario Context key: %s with value: %s ", key,text));
            System.out.println((String.format("Save as Scenario Context key: %s with value: %s ", key,text)));
        } else {
            this.ScenarioData.replace(key,text);
            log.info(String.format("Update Scenario Context key: %s with value: %s ", key,text));
        }
    }

    public void RetrieveTestData(String parameter) throws IOException {
        Environment = readProperties("Environment");
        try {
            SaveInScenario(parameter, readProperties(parameter + "." + Environment));
            System.out.println(parameter + ": " + this.ScenarioData.get(parameter));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void iSetElementWithKeyValue(String element, String key) throws Exception {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        boolean exist = this.ScenarioData.containsKey(key);
        if (exist){
            String text = this.ScenarioData.get(key);
            driver.findElement(SeleniumElement).sendKeys(text);
            log.info(String.format("Set on element %s with text %s", element, text));
            System.out.println((String.format("Set on element %s with text %s", element, text)));
        }else{
            Assert.assertTrue(String.format("The given key %s do not exist in Context", key), this.ScenarioData.containsKey(key));
        }
    }

    public Select selectOption(String element) throws Exception {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        log.info(String.format("Waiting Element: %s", element));
        Select opt = new Select(driver.findElement(SeleniumElement));
        return opt;
    }

    public void waitForElementPresent(String element) throws Exception {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        WebDriverWait w = new WebDriverWait(driver, EXPLICIT_TIMEOUT);
        log.info("Waiting for the element: " + element + " to be present");
        w.until(ExpectedConditions.presenceOfElementLocated(SeleniumElement));
    }

    public void waitForElementVisible(String element) throws Exception {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        WebDriverWait w = new WebDriverWait(driver, EXPLICIT_TIMEOUT);
        log.info("Waiting for the element: " + element+ " to be visible");
        w.until(ExpectedConditions.visibilityOfElementLocated(SeleniumElement));
    }

    public void waitForElementClickable(String element) throws Exception {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        WebDriverWait w = new WebDriverWait(driver, EXPLICIT_TIMEOUT);
        log.info("Waiting for the element: " + element+ " to be visible");
        w.until(ExpectedConditions.elementToBeClickable(SeleniumElement));
    }

    public boolean isElementDisplayed(String element) throws Exception {
        try {
            By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
            log.info(String.format("Waiting Element: %s", element));
            WebDriverWait wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT);
            isDisplayed = wait.until(ExpectedConditions.presenceOfElementLocated(SeleniumElement)).isDisplayed();
        } catch (NoSuchElementException | TimeoutException e){
            isDisplayed = false;
            log.info(e);
        }
        log.info(String.format("%s visibility is: %s", element, isDisplayed));
        return isDisplayed;
    }

    public void switchToFrame(String Frame) throws Exception {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(Frame);
        log.info("Switching to frame: " + Frame);
        driver.switchTo().frame(driver.findElement(SeleniumElement));
    }

    public void switchToParentFrame() {
        log.info("Switching to parent frame");
        driver.switchTo().parentFrame();
    }

    public void checkCheckbox(String element) throws Exception
    {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        boolean isChecked = driver.findElement(SeleniumElement).isSelected();
        if(!isChecked){
            log.info("Clicking on the checkbox to select: " + element);
            driver.findElement(SeleniumElement).click();
        }
    }

    public void UncheckCheckbox(String element) throws Exception
    {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        boolean isChecked = driver.findElement(SeleniumElement).isSelected();
        if(isChecked){
            log.info("Clicking on the checkbox to select: " + element);
            driver.findElement(SeleniumElement).click();
        }
    }

    public void clickJSElement(String element) throws Exception {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        log.info("Scrolling to element: " + element);
        jse.executeScript("arguments[0].click()", driver.findElement(SeleniumElement));
    }

    public void scrollPage(String to) throws Exception {
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        if(to.equals("top")){
            log.info("Scrolling to the top of the page");
            jse.executeScript("scroll(0, -250);");

        }
        else if(to.equals("bottom")){
            log.info("Scrolling to bottom of the page");
            jse.executeScript("scroll(0, 250);");
        }
    }

    public void pageHasLoaded (){
        String GetActual = driver.getCurrentUrl();
        System.out.println(String.format("Checking if %s page is loaded.", GetActual));
        log.info(String.format("Checking if %s page is loaded.", GetActual));
        new WebDriverWait(driver, EXPLICIT_TIMEOUT).until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }

    public void OpenNewTabWithURL(String URL) {
        log.info("Open New tab with URL: " + URL);
        System.out.println("Open New tab with URL: " + URL);
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        jse.executeScript(String.format("window.open('%s','_blank');", URL));
    }

    public void WindowsHandle(String WindowName){
        if (this.HandleMyWindows.containsKey(WindowName)) {
            driver.switchTo().window(this.HandleMyWindows.get(WindowName));
            log.info(String.format("I go to Windows: %s with value: %s ", WindowName ,this.HandleMyWindows.get(WindowName)));
        } else {
            for(String winHandle : driver.getWindowHandles()){
                this.HandleMyWindows.put(WindowName, winHandle);
                System.out.println("The New window " + WindowName + " is saved in scenario with value " + this.HandleMyWindows.get(WindowName));
                log.info("The New window "+ WindowName + " is saved in scenario with value " + this.HandleMyWindows.get(WindowName));
                driver.switchTo().window(this.HandleMyWindows.get(WindowName));
            }
        }
    }

    public void handleAlert(String req) {
        try{
            WebDriverWait wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT);
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            ElementText = alert.getText();
            System.out.println("Alert Text Content: " + ElementText);
            if (req == "Accept") {
                alert.accept();
                System.out.println("Alert accepted");
            } else {
                alert.dismiss();
                System.out.println("Alert dismissed");
            }
            log.info("The alert was accepted successfully.");
        }catch(Throwable e){
            log.error("Error came while waiting for the alert popup. "+ e.getMessage());
        }
    }

    public void ScreenShot(String TestCaptura) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");
        String screenShotName = readProperties("ScreenShotPath") + "\\" + readProperties("browser") + "\\" + TestCaptura + "_(" + dateFormat.format(GregorianCalendar.getInstance().getTime()) + ")";
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        log.info("Screenshot saved as:" + screenShotName);
        FileUtils.copyFile(scrFile, new File(String.format("%s.png", screenShotName)));
    }

    public String GetTextElement(String element) throws Exception {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        WebDriverWait wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT);
        wait.until(ExpectedConditions.presenceOfElementLocated(SeleniumElement));
        log.info(String.format("Esperando el elemento: %s", element));
        ElementText = driver.findElement(SeleniumElement).getText();
        return ElementText;
    }

    public void checkPartialTextElementNotPresent(String element, String text) throws Exception {
        ElementText = GetTextElement(element);
        boolean isFoundFalse = ElementText.indexOf(text) !=-1? true: false;
        Assert.assertFalse("Text is present in element: " + element + " current text is: " + ElementText, isFoundFalse);

    }

    public void checkPartialTextElementPresent(String element, String text) throws Exception {
        ElementText = GetTextElement(element);
        boolean isFound = ElementText.indexOf(text) !=-1? true: false;
        Assert.assertTrue("Text is not present in element: " + element + " current text is: " + ElementText, isFound);
    }

    public void checkTextElementEqualTo(String element, String text) throws Exception {
        ElementText = GetTextElement(element);
        Assert.assertEquals("Text is not present in element: " + element + " current text is: " + ElementText, text, ElementText);
    }

}
