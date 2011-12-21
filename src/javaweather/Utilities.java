package javaweather;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.nio.channels.Channels;
import java.net.URL;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Utilities {

    private Utilities() {
        // no instances of this class are allowed
    }

    public static byte[] readUrl(URL url) {
        String cacheFN = "cache/" + url.getHost() + url.getFile().replaceAll("[:*?\"<>|]", ".");
        File cacheFile = new File(cacheFN);
        try {
            cacheFile.getParentFile().mkdirs();
            
            if(System.currentTimeMillis() - cacheFile.lastModified() > 3600000) {
                cacheFile.delete();
            }
             
            if (cacheFile.createNewFile()) {
                InputStream in = url.openStream();
                FileOutputStream out = new FileOutputStream(cacheFile);
                out.getChannel().transferFrom(Channels.newChannel(in), 0, Long.MAX_VALUE);
                out.close();
                in.close();
            }
            byte[] b = new byte[(int)cacheFile.length()];
            InputStream is = new FileInputStream(cacheFile);
            is.read(b);
            is.close();
            return b;
        } catch (Exception e) {
            return new byte[]{};
        }
    }

    public static NamespaceContext getNamespaceContext() {
        HashMap<String, String> prefixes = new HashMap<String, String>();
        prefixes.put("yahoo", "http://www.yahooapis.com/v1/base.rng");
        prefixes.put("yweather", "http://xml.weather.yahoo.com/ns/rss/1.0");
        prefixes.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        prefixes.put("yahooDefault", "http://where.yahooapis.com/v1/schema.rng");
        return new WeatherNamespaceContext(prefixes);
    }

    public static Document documentFromString(String xmlContent, boolean namespaceAware) {
        Document doc = null;
        try {
            doc = documentFromString(new InputSource(new StringReader(xmlContent)), namespaceAware);
        } catch (Exception e) {
        }
        return doc;
    }

    public static Document documentFromString(InputSource is, boolean namespaceAware) {
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(namespaceAware);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            doc = builder.parse(is);
        } catch (Exception e) {
        }
        return doc;
    }

    public static NodeList executeXPath(Document doc, String xpathString) {
        Object result = null;
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(getNamespaceContext());
            XPathExpression expr = xpath.compile(xpathString);
            result = expr.evaluate(doc, XPathConstants.NODESET);
        } catch (Exception e) {
        }
        return (NodeList) result;
    }

    public static WindDirection convertWindInformation(int degrees) {
        return WindDirection.getDirection(degrees);
    }

    public static WindDirection convertWindInformation(String degrees) {
        return convertWindInformation(Integer.parseInt(degrees));
    }

    public static ArrayList<String> getAllMatches(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        ArrayList<String> matches = new ArrayList<String>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }

    public static int celsiusToFahrenheit(int tempInCelsius) {
        return ((tempInCelsius * 9 / 5) + 32);
    }

    public static int celsiusToFahrenheit(String tempInCelsius) {
        return celsiusToFahrenheit(Integer.parseInt(tempInCelsius));
    }

    public static int fahrenheitToCelsius(int tempInFahrenheit) {
        return ((tempInFahrenheit - 32) * 5 / 9);
    }

    public static int fahrenheitToCelsius(String tempInFahrenheit) {
        return fahrenheitToCelsius(Integer.parseInt(tempInFahrenheit));
    }
}
