package ru.lyuchkov.parse;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ParseUtil {
    private static final GoogleParser googleParser = new GoogleParser();
    private static final YoutubeParser youtubeParser = new YoutubeParser();
    public static String getText(String query){
        try {
            Element page = googleParser.getSearchResultPage(googleParser.getUrl(query));
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
    public static String getYoutubeUrl(List<String> query) {
        try {
            String keys = youtubeParser.getSearchResultPage(youtubeParser.getYoutubeUrl(query) + "").html();
            String[] str = Objects.requireNonNull(YoutubeParser.getYouTubeId(keys)).split("\"");
            return "https://www.youtube.com/watch?v=" + str[1];
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

}
