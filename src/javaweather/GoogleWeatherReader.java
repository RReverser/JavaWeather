package javaweather;

import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.util.HashMap;

public class GoogleWeatherReader extends AbstractWeatherReader {

    private static final String WEATHER_URL = "http://www.google.com/ig/api?weather=";
    private static final String[] XPATHS = {
        "//humidity[@data]",
        "//temp_c[@data]",
        "//condition[@data]",
        "//wind_condition[@data]",
        "//icon[@data]"
    };
    private static final String DATA_ATTRIBUTE_NAME = "data";
    private static final String FORECAST_XPATH = "//forecast_conditions";
    private String LANGUAGE = null;
    private Document doc;

    public GoogleWeatherReader(HashMap<String, String> params) {
        super(params);
        LANGUAGE = params.get("lang");
        if (LANGUAGE == null) {
            LANGUAGE = "en";
        }
    }

    @Override
    public AbstractWeatherReader process() {
        try {
            String weatherData = new String(Utilities.readUrl(new URL(WEATHER_URL + location + "&hl=" + LANGUAGE + "&oe=utf-8")));

            doc = Utilities.documentFromString(weatherData, true);
            String humidity = executeXPath(doc, XPATHS[0], DATA_ATTRIBUTE_NAME);
            String wind = executeXPath(doc, XPATHS[3], DATA_ATTRIBUTE_NAME).split(",? ")[1];
            this.getWeatherModel()
                    .setHumidity(Utilities.getAllMatches(humidity, "[0-9]{1,3}").get(0))
                    .setTemperature(executeXPath(doc, XPATHS[1], DATA_ATTRIBUTE_NAME))
                    .setCondition(executeXPath(doc, XPATHS[2], DATA_ATTRIBUTE_NAME))
                    .setWind(WindDirection.getDirection(wind))
                    .setIconUrl("http://www.google.com" + executeXPath(doc, XPATHS[4], DATA_ATTRIBUTE_NAME));
            processForecast();
        } catch (Exception e) {
        }
        return this;
    }

    @Override
    public String toString() {
        return "GoogleWeatherReader for location: " + this.location;
    }

    private GoogleWeatherReader processForecast() {
        NodeList children = null;
        NodeList nl = Utilities.executeXPath(doc, FORECAST_XPATH);
        for (int i = 0; i < nl.getLength(); i++) {
            children = nl.item(i).getChildNodes();
            String low = children.item(1).getAttributes().getNamedItem(DATA_ATTRIBUTE_NAME).getNodeValue();
            String high = children.item(2).getAttributes().getNamedItem(DATA_ATTRIBUTE_NAME).getNodeValue();
            String condition = children.item(4).getAttributes().getNamedItem(DATA_ATTRIBUTE_NAME).getNodeValue();
            String iconUrl = children.item(3).getAttributes().getNamedItem(DATA_ATTRIBUTE_NAME).getNodeValue();
            this.getWeatherModel().addForecastModel(
                    new WeatherModel()
                    .setCondition(condition)
                    .setTemperature(Utilities.fahrenheitToCelsius(low) + ".." + Utilities.fahrenheitToCelsius(high))
                    .setIconUrl("http://www.google.com" + iconUrl)
            );
        }
        return this;
    }

    private String executeXPath(Document doc, String xpath, String attr) {
        String data = null;
        NodeList nl = Utilities.executeXPath(doc, xpath);
        if (nl != null && nl.item(0) != null) {
            data = nl.item(0).getAttributes().getNamedItem(attr).getNodeValue();
        }
        return data;
    }
}
