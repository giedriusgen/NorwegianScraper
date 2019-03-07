package lt.giedrius.norwegian;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class NorwegianScraper {

	Document document;
	String date = "";

	// information about one flight is held in different classes. I am using one
	// method with different parameters which points to different classes names
	public void collectInfoToDB(String rowInfo1, String rowInfo2) throws SQLException {

		String cheapestPrice = "";

		// I am using this MySql version: 8.0.13. User: root, password: root
		Connection con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/testdb?useTimezone=true&serverTimezone=UTC", "root", "root");

		String query = " insert into norwegianflights (departure_date, departure_airport, arrival_airport, departure_time, arrival_time, cheapest_price, taxes)"
				+ " values (?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement preparedStmt = con.prepareStatement(query);

		Elements oddRowSelected1 = document.getElementsByClass(rowInfo1);

		for (Element odd : oddRowSelected1) {

			String departureTime = odd.getElementsByClass("depdest").text();
			String arrivalTime = odd.getElementsByClass("arrdest").text();

			if (rowInfo1.equals("oddrow selectedrow rowinfo1 ") || rowInfo1.equals("evenrow selectedrow rowinfo1 ")) {
				cheapestPrice = odd.getElementsByClass("fareselect standardlowfare selectedfare").text();
			} else {
				cheapestPrice = odd.getElementsByClass("fareselect standardlowfare").text();
			}
			String taxes = document.getElementsByClass("rightcell emphasize").last().text();

			preparedStmt.setString(4, departureTime);
			preparedStmt.setString(5, arrivalTime);
			preparedStmt.setString(6, cheapestPrice);
			preparedStmt.setString(7, taxes);
		}

		Elements oddRowSelected2 = document.getElementsByClass(rowInfo2);

		for (Element odd : oddRowSelected2) {

			String departureAirport = odd.getElementsByClass("depdest").text();
			String arrivalAirport = odd.getElementsByClass("arrdest").text();
			Elements e = document.select(".layoutcell");
			date = e.get(1).text();

			preparedStmt.setString(1, date);
			preparedStmt.setString(2, departureAirport);
			preparedStmt.setString(3, arrivalAirport);
			preparedStmt.execute();

		}
		con.close();

	}

	// I am passing how many days I want to check and url with selected first day
	public void extractInfo(int amountOfDays, String selectedUrl)
			throws IOException, InterruptedException, ClassNotFoundException, SQLException {

		String calendarBoxText = "This page requires Javascript to be enabled in your browser.";
		int intAfterNoFlights = 0;

		// create a mysql database connection
		String myDriver = "com.mysql.cj.jdbc.Driver";
		Class.forName(myDriver);

		// Select File path where your Chrome Driver is placed
		System.setProperty("webdriver.chrome.driver", "D:\\ChromeDriver\\chromedriver.exe");

		WebDriver driver = new ChromeDriver();
		driver.navigate().to(selectedUrl);

		for (int i = 0; i < amountOfDays; i++) {
			// I am sleeping my code in some places to avoid the situation when page is not
			// loaded
			TimeUnit.SECONDS.sleep(2);
			String url = driver.getCurrentUrl();
			document = Jsoup.connect(url).get();

			// it exist two types of pages. I am checking which page do I need to use
			String checker = document.getElementsByClass("box").text();

			if (!calendarBoxText.equals(checker)) {

				// extracting information about all the flights at one day
				collectInfoToDB("oddrow selectedrow rowinfo1 ", "oddrow selectedrow rowinfo2");
				collectInfoToDB("oddrow rowinfo1 ", "oddrow rowinfo2");
				collectInfoToDB("evenrow selectedrow rowinfo1 ", "evenrow selectedrow rowinfo2");
				collectInfoToDB("evenrow rowinfo1 ", "evenrow rowinfo2");

				// scrolling down the webpage and clicking the button: Show next day
				JavascriptExecutor js = (JavascriptExecutor) driver;
				js.executeScript("scrollBy(0, 1000)");
				driver.findElement(By.xpath("//span[contains(text(),'Show next day > >')]")).click();

				TimeUnit.SECONDS.sleep(2);
				js.executeScript("scrollBy(0, 500)");

			} else {
				TimeUnit.SECONDS.sleep(2);

				// I need to find day before Saturday, because Norwegian do not provide these
				// flights on Saturday
				String dayBeforeNoFlights = date.substring(date.indexOf(".") - 2, date.indexOf("."));

				if (dayBeforeNoFlights.contains(" ")) {
					dayBeforeNoFlights = dayBeforeNoFlights.substring(1);
				}

				// finding day after Saturday and clicking on this button and Continue
				intAfterNoFlights = Integer.parseInt(dayBeforeNoFlights) + 2;
				String dayAfterNoFlights = String.valueOf(intAfterNoFlights);

				driver.findElement(By.xpath("//span[contains(text(),'" + dayAfterNoFlights + ".')]")).click();
				driver.findElement(By.xpath("//span[contains(text(),'Continue')]")).click();
			}
		}

	}
}
