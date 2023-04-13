package org.example;

import com.google.gson.Gson;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws IOException {


        Gson gson = new Gson();


        File file = new File("./config.json");
        String jsonString = FileUtils.readFileToString(file, "UTF-8");

        HashMap<String, String> params = gson.fromJson(jsonString, HashMap.class);
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");


        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        driver.manage().window().maximize();
        String link = params.get("link").replaceAll("\\?.*", "") + "?active_tab=roster";
        driver.get(link);
        driver.findElement(By.xpath("//*[text()='Login']")).click();
        driver.findElement(By.xpath("//*[@autocomplete=\"email\"]")).sendKeys(params.get("login"));
        driver.findElement(By.xpath("//*[@autocomplete=\"password\"]")).sendKeys(params.get("password"));
        driver.findElement(By.xpath("//*[@type=\"checkbox\"]")).click();
        driver.findElement(By.xpath("//*[text()='Sign In']")).click();
        try {
            Thread.sleep(4_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        driver.findElement(By.xpath("//*[text()='I Accept']")).click();

        driver.findElement(By.xpath("//*[@aria-haspopup=\"listbox\"]")).click();
        driver.findElement(By.xpath("//*[text()='All']")).click();
        int size = driver.findElements(By.xpath("//*[contains(@class,'MuiGrid-root MuiGrid-container')][.//*[contains(text(),'CHECKED IN')]]//*[contains(@class,'MuiButtonBase-root MuiIconButton-root MuiIconButton-sizeMedium')]")).size();
        try {


            driver.findElement(By.xpath("//*[@data-testid='StarBorderIcon']/../../..//*[contains(@class,'MuiTypography-root MuiTypography')]")).click();
        } catch (WebDriverException e) {

        }

        String fName = "";
        try {
            fName = driver.findElement(By.xpath("//*[@data-testid='StarBorderIcon']/../../..//*[contains(@class,'MuiTypography-root MuiTypography')]")).getText();
        } catch (WebDriverException e) {

        }
        fName = "./output" + (!fName.equals("") ? "_" : "") + fName.replaceAll("[',\"]?", "");
        File directory = new File(fName);

        if (!directory.exists()) {
            directory.mkdir();
        }
        for (int i = 0; i < size; i++) {
            try {
                ArrayList<String> tabs2 = new ArrayList<String>(driver.getWindowHandles());
                driver.switchTo().window(tabs2.get(0));
                String name = driver.findElements(By.xpath("//*[contains(@class,'MuiGrid-root MuiGrid-container')][.//*[contains(text(),'CHECKED IN')]]//*[contains(@class,'MuiButtonBase-root MuiIconButton-root MuiIconButton-sizeMedium')]/../..//*[contains(@class,'MuiTypography-root MuiTypography-body1')]")).get(i).getText() + "_";
                driver.findElements(By.xpath("//*[contains(@class,'MuiGrid-root MuiGrid-container')][.//*[contains(text(),'CHECKED IN')]]//*[contains(@class,'MuiButtonBase-root MuiIconButton-root MuiIconButton-sizeMedium')]")).get(i).click();
                tabs2 = new ArrayList<String>(driver.getWindowHandles());
                driver.switchTo().window(tabs2.get(1));


                String list = driver.findElement(By.xpath("//div[@class='list']")).getText();
                driver.close();
                driver.switchTo().window(tabs2.get(0));
                Matcher matcher = Pattern.compile("(?:Army Faction:|Allegiance:) (.*)\\n").matcher(list);

                if (matcher.find()) {
                    name += matcher.group(1);
                } else {
                    name += "na";
                }
                name = name.replaceAll("[',\"]?", "");
                File myObj = new File("./" + fName + "/" + name + ".txt");
                try {
                    myObj.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                FileWriter myWriter = null;
                try {
                    myWriter = new FileWriter("./" + fName + "/" + name + ".txt");
                    myWriter.write(list);
                    myWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            } catch (Exception e) {
            }
        }

        driver.quit();
    }
}