package ru.lyuchkov.parse;

import org.jsoup.nodes.Element;

import java.io.IOException;

public interface Parser {
    String PUNCT = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    String getUrl(String s);
    Element getSearchResultPage(String s) throws IOException;
}
