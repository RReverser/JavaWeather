document.title = widget.name;

var weatherData;

function ajaxCall(URL, callback) {
    var ajax = new XMLHttpRequest;
    if(callback) ajax.onreadystatechange = function() { if(this.readyState == 4) callback(this.responseText) };
    ajax.open('GET', URL, !!callback);
    ajax.send();
    return ajax;
}

function preloadImage(url) { if(url) new Image().src = url }
function preprocessWeather(weatherData) { preloadImage(weatherData.iconUrl) }

var button = opera.contexts.toolbar.createItem({
    title: widget.name,
    icon: 'icon18.png',
    popup: {
        href: 'popup.html',
        width: 270
    },
    badge: {
        display: 'none',
        color: 'white',
        backgroundColor: 'red'
    }
});
    
opera.contexts.toolbar.addItem(button);

var timerId;
var getWeather = function() {
    if(timerId) {
        clearTimeout(timerId);
        timerId = null;
    }
    ajaxCall(widget.preferences.server + 'server/' + widget.preferences.provider + '/location/' + widget.preferences.location + '/lang/' + localeStr.id + '/', function(data) {
        try {
            weatherData = JSON.parse(data);
            if(weatherData.temperature > 0) weatherData.temperature = '+' + weatherData.temperature;
            if(!(weatherData.forecast instanceof Array)) weatherData.forecast = [];
            preprocessWeather(weatherData);
            weatherData.forecast.forEach(preprocessWeather);
        } catch(e) {
            weatherData = null;
        }
        if(weatherData && weatherData.temperature) {
            button.badge.textContent = weatherData.temperature;
            button.badge.display = 'block';
        } else {
            button.badge.display = 'none';
        }
        timerId = setTimeout(getWeather, weatherData ? 60000 : 1000);
    });
}

getWeather();