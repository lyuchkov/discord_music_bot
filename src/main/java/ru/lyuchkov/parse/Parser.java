package ru.lyuchkov.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Parser {
    private static final String PUNCT = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    public static String getYoutubeUrl(List<String> query) {
        try {
            String keys = getYoutubeSearchResult(getYoutubeQuery(query) + "");
            String[] str = Objects.requireNonNull(getYouTubeId(keys)).split("\"");
            return "https://www.youtube.com/watch?v=" + str[1];
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }
    public static String getYoutubeQuery(List<String> query){
        StringBuilder builder = new StringBuilder();
        builder.append("https://youtube.googleapis.com/youtube/v3/search?q=");
        for (String s:
             query) {
            builder.append(s);
            builder.append("+");
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }
    public static String getYoutubeSearchResult(String urlQuery) throws IOException {
            Document searchPage = Jsoup.connect(urlQuery)
                  .ignoreContentType(true)
                  .referrer("https://www.google.com")
                  .timeout(300000)
                    .userAgent("\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                    .followRedirects(true)
                    .get();
           return searchPage.body().html();
    }
    public static String getYouTubeId(String keys)  {
        keys = keys.replace("{", "");
        keys = keys.replace("}", "");
        keys = keys.replace(",", "");
        keys = keys.replace(" ", "");
        String[] arr = keys.split(":");
        for (int i=0; i<arr.length; i++) {
              if(arr[i].equals("\"youtube#video\"\"videoId\""))
               return arr[i+1];
        }
        return "null";
    }
    public static String getGoogleQuery(String query){
        StringBuilder builder = new StringBuilder();
        builder.append("https://www.google.com/search?ie=UTF-8&oe=UTF-8&q=");
        for (int i = 0; i < query.length(); i++) {
            char temp = query.charAt(i);
            if(PUNCT.indexOf(temp)<0){
                builder.append(temp);
            }
            else {
                builder.append("+").append(temp).append("+");
            }
        }
        builder.append("+").append("текст");
        return builder.toString().replaceAll(" ", "");
    }
    public static Element getGoogleSearchResult(String urlQuery) throws IOException {
        Document searchPage = Jsoup.connect(urlQuery)
                .ignoreContentType(true)
                .referrer("https://www.google.com")
                .timeout(300000)
                .userAgent("\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                .followRedirects(false)
                .get();
        return searchPage.body();
    }
    public static String getText(String query){
        try {
            Element page = getGoogleSearchResult(getGoogleQuery(query));
            Elements divs = page.select("div.bbVIQb");
            if(divs.html().isEmpty())
                divs = page.select("div.PZPZlf");
            divs.select("div.ujudUb.WRZytc.OULBYb").remove();
            Elements spans1 = divs.select("div.ujudUb");
            return spans1.select("span").html();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }
}
