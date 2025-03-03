package com.myPrivateNote.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import java.util.ArrayList;
import java.util.List;

public class NoteUtils {

    public static String sanitizeHtml(String contentHtml) {
        return Jsoup.clean(contentHtml, Safelist.relaxed().addTags("iframe", "video").addAttributes("iframe", "src"));
    }

    public static String extractTitle(String contentHtml) {
        Document doc = Jsoup.parse(contentHtml);

        // Cherche un titre <h1>
        Element titleElement = doc.selectFirst("h1");
        if (titleElement != null) {
            return titleElement.text();
        }

        // Cherche un <p> (paragraphe)
        Element firstParagraph = doc.selectFirst("p");
        if (firstParagraph != null) {
            return firstParagraph.text();
        }

        // Sinon, prend les 50 premiers caractères du texte brut du document
        String plainText = doc.body().text().trim();
        if (!plainText.isEmpty()) {
            return plainText.length() > 50 ? plainText.substring(0, 50) : plainText;
        }

        // Si toujours rien, retourne "Sans titre"
        return "Sans titre";
    }

    public static List<String> extractLinks(String contentHtml) {
        Document doc = Jsoup.parse(contentHtml);
        List<String> links = new ArrayList<>();
        for (Element link : doc.select("a[href]")) {
            links.add(link.attr("href"));
        }
        return links;
    }

    public static List<String> extractVideos(String contentHtml) {
        Document doc = Jsoup.parse(contentHtml);
        List<String> videos = new ArrayList<>();
        for (Element video : doc.select("iframe, video")) {
            videos.add(video.attr("src"));  // Récupère les URLs des vidéos
        }
        return videos;
    }

    public static boolean containsCode(String contentHtml) {
        Document doc = Jsoup.parse(contentHtml);
        return !doc.select("pre, code").isEmpty();  // Vérifie la présence de <pre> ou <code>
    }
}
