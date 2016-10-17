package youtube.automation;

import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import parameters.Parameters;

public class SetTimeQC {
	
	static String file_driver = "geckodriver/chromedriver.exe";
	
	static WebDriver driver;
	static WebDriverWait wait;
	
	static String username;
	static String password;
	
	static ArrayList<String> listVideosNeedQC = new ArrayList<>();
	
	static String time;
	static ArrayList<String> listKeywords = new ArrayList<>();
	
	static void init() throws IOException, InterruptedException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Parameters.file_info)));
		String line;
		while ((line = br.readLine()) != null) {
			String[] tempArray = line.split("=");
			if (tempArray[0].trim().compareToIgnoreCase("username") == 0) {
				username = tempArray[1].trim();
				System.out.println("username = " + username);
			} else if (tempArray[0].trim().compareToIgnoreCase("password") == 0) {
				password = tempArray[1].trim();
			} else if (tempArray[0].trim().compareToIgnoreCase("time") == 0) {
				time = tempArray[1].trim();
				System.out.println("time = " + time);
			} else if (tempArray[0].trim().compareToIgnoreCase("keywords") == 0) {
				String keywords = tempArray[1].trim();
				System.out.println("keywords = " + keywords);
				String [] arrayKeywords = keywords.toLowerCase().split(",");
				for(int i=0; i<arrayKeywords.length; i++)
				{
					listKeywords.add(arrayKeywords[i].trim());
				}
			}
		}
		br.close();
		
		// driver
		System.setProperty("webdriver.chrome.driver", Parameters.file_driver);
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(100, TimeUnit.SECONDS);
		wait = new WebDriverWait(driver, 100);

		// login
		driver.get("https://accounts.google.com/ServiceLogin?passive=true&continue=https%3A%2F%2Fwww.youtube.com%2Fsignin%3Faction_handle_signin%3Dtrue%26app%3Ddesktop%26feature%3Dsign_in_button%26next%3D%252F%26hl%3Den&service=youtube&uilel=3&hl=en#identifier");
		driver.findElement(By.id("Email")).sendKeys(username);
		driver.findElement(By.id("next")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Passwd")));

		driver.findElement(By.id("Passwd")).sendKeys(password);
		driver.findElement(By.id("signIn")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("yt-picker-language-button")));
		
		// change language --> English (in case it was not in English)
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("yt-picker-language-button")));
		Thread.sleep(1000);
		driver.findElement(By.id("yt-picker-language-button")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//strong[@class=\"yt-picker-item\"]")));
		System.out.println(driver.findElement(By.xpath("//strong[@class=\"yt-picker-item\"]")).getText());
		Thread.sleep(1000);
		if(driver.findElement(By.xpath("//strong[@class=\"yt-picker-item\"]")).getText().compareTo("English (US)")!=0)
		{
			driver.findElement(By.xpath("//button[@value=\"en\"]")).click();
		}
		// wait until change done
		while(true)
		{
			System.out.println("Wait until change to English done....");
			if(driver.findElement(By.xpath("//link[@rel=\"search\"]")).getAttribute("href").toString().contains("locale=en_US"))
			{
				break;
			}
			Thread.sleep(500);
		}
		
		System.out.println("Done init!");
	}
	
	private static boolean checkCandidateTitle(String title)
	{
		for(int i=0; i<listKeywords.size(); i++)
		{
			if(title.toLowerCase().contains(listKeywords.get(i)))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static void getListVideosNeedQC() 
	{
		int page_number = 0;
		while (true) 
		{
			page_number++;
			System.out.println("Page " + page_number);
			driver.get("https://www.youtube.com/my_videos?o=U&pi=" + page_number);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@class=\"vm-video-title-content yt-uix-sessionlink\"]")));
			List<WebElement> listElementVideos = driver
					.findElements(By.xpath("//a[@class=\"vm-video-title-content yt-uix-sessionlink\"]"));
			
			for (WebElement video : listElementVideos) 
			{
				String link = video.getAttribute("href");
				String title = video.getText();
				if(checkCandidateTitle(title))
				{
					System.out.println(link);
					System.out.println(title);
					System.out.println();
					listVideosNeedQC.add(link);
				}
			}
			
			if(!driver.getPageSource().contains("Next »"))
			{
				break;
			}
		}
	}
	
	private static void setTime(String id) throws InterruptedException, AWTException 
	{
		driver.get("https://www.youtube.com/edit?o=U&video_id=" + id);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//form[@class=\"video-settings-form ng-valid ng-valid-parse ng-valid-maxlength ng-pristine\"]")));

		if(driver.findElement(By.xpath("//select[@class=\"yt-uix-form-input-select-element metadata-privacy-input\"]")).getAttribute("data-initial-value").toString().compareTo("public")==0)
		{
			System.out.println("Status video: public");
		} else 
		{
			// chuyen sang tab kiem tien
			driver.findElement(By.xpath("//ul[@class=\"tabs\"]/li[3]")).click();
			Thread.sleep(500);
			
			// bat che do kiem tien
			if(!driver.findElement(By.id("monetize-with-ads")).isSelected())
			{
				try {
					driver.findElement(By.id("monetize-with-ads")).click();
				} catch (Exception e) {
					System.out.println("Khong cho bat che do kiem tien!");
					return;
				}
			} else {
				System.out.println("Da bat kiem tien!");
			}
			
			Thread.sleep(500);
			boolean existSettime = true;
			try {
				driver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);
				driver.findElement(By.xpath("//*[@id=\"metadata-editor-pane\"]/div/div[1]/div[3]/form/div[3]/div/div/div/div[2]/ng-form/div[2]/div[7]/span[1]/input"));
				driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
			} catch (Exception e) {
				existSettime = false;
			}
			
			if(existSettime)
			{
				if(!driver.findElement(By.xpath("//*[@id=\"metadata-editor-pane\"]/div/div[1]/div[3]/form/div[3]/div/div/div/div[2]/ng-form/div[2]/div[7]/span[1]/input")).isSelected())
				{
					driver.findElement(By.xpath("//*[@id=\"metadata-editor-pane\"]/div/div[1]/div[3]/form/div[3]/div/div/div/div[2]/ng-form/div[2]/div[7]/span[1]/input")).click();
					while(true)
					{
						System.out.println("wait until selected done...");
						if(driver.findElement(By.xpath("//*[@id=\"metadata-editor-pane\"]/div/div[1]/div[3]/form/div[3]/div/div/div/div[2]/ng-form/div[2]/div[7]/span[1]/input")).isSelected())
						{
							break;
						}
						Thread.sleep(200);
					}
					System.out.println("selected done.");
				} else {
					System.out.println("Da check after video");
				}
				
				// open text area to set time break ads
				System.out.println("Open text area ...");
				Thread.sleep(500);
				driver.findElement(By.xpath("//*[@id=\"metadata-editor-pane\"]/div/div[1]/div[3]/form/div[3]/div/div/div/div[2]/ng-form/div[4]/button/span")).click();
				Thread.sleep(500);
				
				wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@id=\"body\"]/div[9]/div/div/div[1]/div[3]/span/textarea")));
				
				driver.findElement(By.xpath("//*[@id=\"body\"]/div[9]/div/div/div[1]/div[3]/span/textarea")).clear();
				Thread.sleep(200);
				driver.findElement(By.xpath("//*[@id=\"body\"]/div[9]/div/div/div[1]/div[3]/span/textarea")).sendKeys(time);
				
				Thread.sleep(500);
				driver.findElement(By.xpath("//*[@id=\"body\"]/div[9]/div/div/div[1]/div[3]/div/button[2]/span")).click();
			} else {
				System.out.println("Khong cho phep set time break ads!");
			}
			
			// save
			Thread.sleep(500);
			if(driver.findElement(By.xpath("//*[@id=\"metadata-editor-pane\"]/div/div[2]/div[1]/div/button[2]")).isEnabled())
			{
				driver.findElement(By.xpath("//*[@id=\"metadata-editor-pane\"]/div/div[2]/div[1]/div/button[2]")).click();
				
				while(true)
				{
					System.out.println(driver.findElement(By.xpath("//*[@id=\"metadata-editor-pane\"]/div/div[2]/div[1]/span[1]")).getText());
					if(driver.findElement(By.xpath("//*[@id=\"metadata-editor-pane\"]/div/div[2]/div[1]/span[1]")).getText().compareToIgnoreCase("all changes saved.")==0)
					{
						break;
					}
					Thread.sleep(200);
				}
			} else {
				System.out.println("Khong co gi thay doi de luu.");
			}
		}
	}
	
	public static void main(String [] args) throws IOException, InterruptedException, AWTException
	{
		init();
		getListVideosNeedQC();
		// listVideosNeedQC.add("https://www.youtube.com/watch?v=aujZEISKjbA");
		for (int i = 0; i < listVideosNeedQC.size(); i++) 
		{
			System.out.println("Link " + (i + 1));
			String id = listVideosNeedQC.get(i).replace("https://www.youtube.com/watch?v=", "");
			setTime(id);
		}
		
		driver.get("https://www.youtube.com/my_videos?o=U");
	}
}
