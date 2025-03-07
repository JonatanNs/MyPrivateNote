package com.myPrivateNote.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;

public class NoteUtils {

    public static String sanitizeHtml(String contentHtml) {
        Safelist safelist = Safelist.relaxed()
                .addTags("iframe", "video", "img", "a")  // Ajout de la balise <a>
                .addAttributes("iframe", "src", "allowfullscreen", "frameborder")
                .addAttributes("video", "controls", "autoplay")
                .addAttributes("img", "src", "alt", "width", "height", "style")
                .addAttributes("a", "href", "target")
                .addAttributes(":all", "class", "style")
                .preserveRelativeLinks(true)
                .addProtocols("img", "src", "http", "https", "data");

        // Nettoyage du HTML
        String result = Jsoup.clean(contentHtml, safelist);

        // Ajouter target="_blank" aux liens
        Document doc = Jsoup.parse(result);
        for (Element link : doc.select("a[href]")) {
            link.attr("target", "_blank");
        }

        return doc.html();
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

    public static boolean containsCode(String contentHtml) {
        Document doc = Jsoup.parse(contentHtml);
        return !doc.select("pre, code").isEmpty();  // Vérifie la présence de <pre> ou <code>
    }
}
