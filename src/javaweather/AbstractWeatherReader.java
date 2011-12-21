package javaweather;

import java.net.URLEncoder;
import java.util.HashMap;

public abstract class AbstractWeatherReader {

    private WeatherModel wm;
    protected HashMap<String, String> params;
    protected String location;

    public AbstractWeatherReader(HashMap<String, String> params) {
        this.params = params;
        setLocation(params.get("location"));
        wm = new WeatherModel();
    }

    public abstract AbstractWeatherReader process();

    private AbstractWeatherReader setLocation(String location) {
        try {
            this.location = URLEncoder.encode(location, "UTF-8");
        } catch (Exception e) {
            this.location = location;
        }
        return this;
    }

    public String getLocation() {
        return location;
    }

    public WeatherModel getWeatherModel() {
        return wm;
    }
}
