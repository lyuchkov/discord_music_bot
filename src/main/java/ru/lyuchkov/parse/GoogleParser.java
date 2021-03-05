package ru.lyuchkov.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class GoogleParser implements Parser {
    @Override
    public String getUrl(String query) {
        StringBuilder builder = new StringBuilder();
        builder.append("https://www.google.com/search?ie=UTF-8&oe=UTF-8&q=");
        for (int i = 0; i < query.length(); i++) {
            char temp = query.charAt(i);
            if (PUNCT.indexOf(temp) < 0) {
                builder.append(temp);
            } else {
                builder.append("+").append(temp).append("+");
            }
        }
        builder.append("+").append("текст");
        return builder.toString().replaceAll(" ", "");
    }

    @Override
    public Element getSearchResultPage(String query) throws IOException {
        Document searchPage = Jsoup.connect(query)
                .ignoreContentType(true)
                .referrer("https://www.google.com")
                .timeout(300000)
                .userAgent("\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                .followRedirects(false)
                .get();
        return searchPage.body();
    }
}
