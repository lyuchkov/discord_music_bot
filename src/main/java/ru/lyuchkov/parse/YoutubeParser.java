package ru.lyuchkov.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;

public class YoutubeParser implements Parser {
    public String getYoutubeUrl(List<String> query) {
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
    @Override
    public Element getSearchResultPage(String query) throws IOException {
        Document searchPage = Jsoup.connect(query)
                .ignoreContentType(true)
                .referrer("https://www.google.com")
                .timeout(300000)
                .userAgent("\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                .followRedirects(true)
                .get();
        return searchPage.body();
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
}
