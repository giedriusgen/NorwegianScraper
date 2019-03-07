package lt.giedrius.norwegian;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

	public static void main(String[] args) {
		
		NorwegianScraper scraper = new NorwegianScraper();
		
		try {
			scraper.extractInfo(30,
					"https://www.norwegian.com/en/ipc/availability/avaday?AdultCount=1&A_City=RIX&D_City=OSL&D_Month=201904&D_Day=01&IncludeTransit=false&TripType=1&CurrencyCode=EUR&dFare=37&mode=ab");
		} catch (ClassNotFoundException | IOException | InterruptedException | SQLException e) {
			e.printStackTrace();
		}
	}
}
