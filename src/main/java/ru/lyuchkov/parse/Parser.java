package ru.lyuchkov.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Objects;

public class Parser {
    public static String getYoutubeUrl(String query) {
        try {
            String keys = getYoutubeSearchResult(getYoutubeQuery(query) + "");
            String[] str = Objects.requireNonNull(getYouTubeId(keys)).split("\"");
            return "https://www.youtube.com/watch?v=" + str[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }
    public static String getYoutubeQuery(String query){
        StringBuilder builder = new StringBuilder();
        builder.append("https://youtube.googleapis.com/youtube/v3/search?q=");
        String[] q = query.split(" ");
        for (String s:
             q) {
            builder.append(s.replaceAll(" ", "")
                    .trim());
            builder.append("+");
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }
    //todo сделать метод универсальным
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
}
