package javaweather;

import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.util.HashMap;

public final class YahooWeatherReader extends AbstractWeatherReader {

    private static final String WOEID_URL = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.places%20where%20text%3D%22";
    private static final String WEATHER_URL = "http://weather.yahooapis.com/forecastrss?w=";
    private static final String[] XPATHS = {"//yweather:atmosphere[@humidity]",
        "//yweather:condition[@temp]",
        "//yweather:condition[@text]",
        "//yweather:wind[@direction]",
        "//item/description"};
    private static final String FORECAST_XPATH = "//yweather:forecast";
    private Document doc;
    private String units = null;

    public YahooWeatherReader(HashMap<String, String> params) {
        super(params);
        setUnits(params.get("units"));
    }

    public String getUnits() {
        return this.units;
    }

    public YahooWeatherReader setUnits(String units) {
        this.units = (units != null ? units.toLowerCase() : "c");
        return this;
    }

    @Override
    public AbstractWeatherReader process() {
        String woeid = getWoeid();
        String weatherData = getWeatherData(woeid);
        doc = Utilities.documentFromString(weatherData, true);

        this.getWeatherModel()
                .setHumidity(executeXPath(doc, XPATHS[0], "humidity"))
                .setTemperature(executeXPath(doc, XPATHS[1], "temp"))
                .setCondition(executeXPath(doc, XPATHS[2], "text"))
                .setWind(executeXPath(doc, XPATHS[3], "direction"))
                .setIconUrl(Utilities.getAllMatches(processNodeList(Utilities.executeXPath(doc, XPATHS[4])), "[^'\"]+?\\.gif").get(0));
        processForecast();
        return this;
    }

    @Override
    public String toString() {
        return "YahooWeatherReader for location: " + this.location;
    }

    private String getWoeid() {
        String woeid = null;
        try {
            String content = new String(Utilities.readUrl(new URL(WOEID_URL + location + "%22")));
            Document doc = Utilities.documentFromString(content, true);
            NodeList nl = Utilities.executeXPath(doc, "//yahooDefault:woeid");
            if (nl.item(0) != null) {
                woeid = nl.item(0).getTextContent();
            }
        } catch (Exception e) {
        }
        return woeid;
    }

    private String getWeatherData(String woeid) {
        String data = null;
        try {
            data = new String(Utilities.readUrl(new URL(WEATHER_URL + woeid + "&u=" + units)));
        } catch (Exception e) {
        }
        return data;
    }

    private YahooWeatherReader processForecast() {
        NodeList forecastList = Utilities.executeXPath(doc, FORECAST_XPATH);
        for (int i = 0; i < forecastList.getLength(); i++) {
            Node forecastItem = forecastList.item(i);
            String temp = forecastItem.getAttributes().getNamedItem("low").getNodeValue() + ".."
                    + forecastItem.getAttributes().getNamedItem("high").getNodeValue();
            this.getWeatherModel().addForecastModel(
                    new WeatherModel()
                    .setTemperature(temp)
                    .setCondition(forecastItem.getAttributes().getNamedItem("text").getNodeValue())
            );
        }
        return this;
    }

    private String executeXPath(Document doc, String xpath, String attr) {
        NodeList nl = Utilities.executeXPath(doc, xpath);
        String data = null;
        if (nl != null & nl.item(0) != null) {
            data = nl.item(0).getAttributes().getNamedItem(attr).getNodeValue();
        }
        return data;
    }

    private String processNodeList(NodeList nl) {

        if (nl != null && nl.item(0) != null) {
            return nl.item(0).getTextContent();
        } else {
            return "";
        }
    }
}
