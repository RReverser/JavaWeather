package javaweather;

import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.util.HashMap;

public class WUndergroundWeatherReader extends AbstractWeatherReader {

    private static final String LOCATION_URL = "http://api.wunderground.com/auto/wui/geo/GeoLookupXML/index.xml?query=";
    private static final String CURRENT_CONDITION_URL = "http://api.wunderground.com/auto/wui/geo/WXCurrentObXML/index.xml?query=";
    private static final String FORECAST_URL = "http://api.wunderground.com/auto/wui/geo/ForecastXML/index.xml?query=";
    private static final String AIRPORTID_XPATH = "//icao";
    private static final String[] XPATHS = new String[]{
        "//relative_humidity",
        "//temp_c",
        "//weather",
        "//wind_degrees",
        "//fcttext",
        "//icons/icon_set[@name='Default']/icon_url"
    };

    public WUndergroundWeatherReader(HashMap<String, String> params) {
        super(params);
    }

    @Override
    public AbstractWeatherReader process() {
        String aid = getAirportID();
        getCurrentConditions(aid);
        getForecastData();
        return this;
    }

    @Override
    public String toString() {
        return "WUndergroundWeatherReader for location: " + this.location;
    }

    private String getAirportID() {
        String aid = null;
        try {
            String xmlContent = new String(Utilities.readUrl(new URL(LOCATION_URL + this.location)));
            Document doc = Utilities.documentFromString(xmlContent, true);
            NodeList nl = Utilities.executeXPath(doc, AIRPORTID_XPATH);
            aid = processNodeList(nl);
        } catch (Exception e) {
        }
        return aid;
    }

    private WUndergroundWeatherReader getCurrentConditions(String airportID) {
        NodeList nl = null;
        try {
            String xmlContent = new String(Utilities.readUrl(new URL(CURRENT_CONDITION_URL + airportID)));
            Document doc = Utilities.documentFromString(xmlContent, true);
            this.getWeatherModel()
                    .setHumidity(Utilities.getAllMatches(processNodeList(Utilities.executeXPath(doc, XPATHS[0])), "[0-9]{1,3}").get(0))
                    .setTemperature(processNodeList(Utilities.executeXPath(doc, XPATHS[1])))
                    .setCondition(processNodeList(Utilities.executeXPath(doc, XPATHS[2])))
                    .setWind(processNodeList(Utilities.executeXPath(doc, XPATHS[3])))
                    .setIconUrl(processNodeList(Utilities.executeXPath(doc, XPATHS[5])));
        } catch (Exception e) {}
        return this;
    }

    private WUndergroundWeatherReader getForecastData() {
        try {
            String xmlContent = new String(Utilities.readUrl(new URL(FORECAST_URL + this.location)));
            Document doc = Utilities.documentFromString(xmlContent, true);
            NodeList nl = Utilities.executeXPath(doc, XPATHS[4]);
            NodeList icons = Utilities.executeXPath(doc, XPATHS[5]);
            fillForecast(nl, icons);
        } catch (Exception e) {}
        return this;
    }

    private WUndergroundWeatherReader fillForecast(NodeList nl, NodeList icons) {
        String forecast = null;
        for (int i = 1; i < nl.getLength(); i++) // first item is for today (not a forecast)
        {
            if (nl.item(i) != null) {
                forecast = nl.item(i).getTextContent();
                this.getWeatherModel().addForecastModel(
                        new WeatherModel()
                        .setCondition(Utilities.getAllMatches(forecast, "[a-zA-Z ]+").get(0))
                        .setTemperature(Utilities.getAllMatches(forecast, "-?[0-9]{1,2}").get(0))
                        .setIconUrl(icons.item(i).getTextContent())
                );
            }
        }
        return this;
    }

    private String processNodeList(NodeList nl) {

        if (nl != null && nl.item(0) != null) {
            return nl.item(0).getTextContent();
        } else {
            return "";
        }
    }
}
