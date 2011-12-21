package javaweather;

import java.util.ArrayList;
import java.util.List;

public final class WeatherModel {
    private String humidity;
    private String temperature;
    private String condition;
    private String wind;
    private String iconUrl;
    private List<WeatherModel> forecast;

    public WeatherModel() {
        setForecast(new ArrayList<WeatherModel>());
    }

    public WeatherModel(String humidity, String temperature, String condition) {
        this();
        setHumidity(humidity).
                setTemperature(temperature).
                setCondition(condition);
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public WeatherModel setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public String getHumidity() {
        return humidity;
    }

    public WeatherModel setHumidity(String humidity) {
        this.humidity = humidity;
        return this;
    }

    public String getTemperature() {
        return temperature;
    }

    public WeatherModel setTemperature(String temperature) {
        this.temperature = temperature;
        return this;
    }

    public String getCondition() {
        return condition;
    }

    public WeatherModel setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public String getWind() {
        return wind;
    }

    public WeatherModel setWind(String wind) {
        this.wind = Utilities.convertWindInformation(wind).getCode();
        return this;
    }

    public WeatherModel setWind(WindDirection windDirection) {
        this.wind = (windDirection != null ? windDirection.getCode() : null);
        return this;
    }

    public List<WeatherModel> getForecast() {
        return this.forecast;
    }

    public WeatherModel setForecast(List<WeatherModel> forecast) {
        if (forecast != null) {
            this.forecast = forecast;
        }
        return this;
    }

    public WeatherModel addForecastModel(WeatherModel forecastModel) {
        if (forecastModel != null) {
            forecast.add(forecastModel);
        }
        return this;
    }

    @Override
    public String toString() {
        return prepareString("today", "", false);
    }
    
    private String jsonString(String str) {
        return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private String jsonValue(String name, String value) {
        return jsonString(name) + ": " + jsonString(value);
    }

    private String prepareString(String id, String tab, boolean endComma) {
        StringBuilder sb = new StringBuilder();
        String newTab = tab + "  ";
        sb
        .append(tab)
        .append("{\n");
        if (humidity != null && !humidity.isEmpty()) {
            sb
            .append(newTab)
            .append(jsonValue("humidity", humidity))
            .append(",\n");
        }
        if (temperature != null && !temperature.isEmpty()) {
            sb
            .append(newTab)
            .append(jsonValue("temperature", temperature))
            .append(",\n");
        }
        if (condition != null && !condition.isEmpty()) {
            sb
            .append(newTab)
            .append(jsonValue("condition", condition))
            .append(",\n");
        }
        if (wind != null && !wind.isEmpty()) {
            sb
            .append(newTab)
            .append(jsonValue("wind", wind))
            .append(",\n");
        }
        if (iconUrl != null && !iconUrl.isEmpty()) {
            sb
            .append(newTab)
            .append(jsonValue("iconUrl", iconUrl))
            .append(",\n");
        }
        if (forecast != null && !forecast.isEmpty()) {
            sb
            .append(newTab)
            .append("\"forecast\":\n")
            .append(newTab)
            .append("[\n");
            String innerTab = newTab + "  ";
            for (int i = 0; i < forecast.size(); i++) {
                sb.append(forecast.get(i).prepareString("day" + (i + 1), innerTab, i < forecast.size() - 1));
            }
            sb
            .append(newTab)
            .append("],\n");
        }
        return sb
        .append(newTab)
        .append(jsonValue("id", id))
        .append("\n").append(tab)
        .append("}")
        .append(endComma ? "," : "")
        .append("\n")
        .toString();
    }
}
