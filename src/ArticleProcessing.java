import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ArticleProcessing {

    final static String TITLE = ".*<title>(.*)</title>.*";
    final static String PAGE_END = ".*</page>.*";
    final static String TEXT = ".*<text.*>.*";

    final static String NOUN = "(S[SFU]|Q)";
    final static String ADJECTIVE = "(A[SAFU]|S[A])";

    final static String typeOfText = "regular";

    public static String title;




    public static void main(String[] args) throws IOException {

        FileInputStream file = new FileInputStream("files/text.txt");

        String plainText;
        if (typeOfText == "wikipedia") {
            plainText = extract_text_from_wiki(file);
        }
        if (typeOfText == "regular") {
            plainText = extract_text(file);
        }


        ArrayList<String> nouns_adjectives = new ArrayList<>();
        ArrayList<String> oneWord = new ArrayList<>();
        ArrayList<String> twoWord = new ArrayList<>();
        ArrayList<String> threeWord = new ArrayList<>();

        plainText = plainText.replaceAll("[,.;:?!()\"]", "");
        String[] split = plainText.split("[\\s]");

        for (String item : split) {
            String[] found = Lemmatizer.searchLemma(item);
            if (found[1].matches("([SA][SAFU]|Q)") || Character.isUpperCase(item.charAt(0))) {
                nouns_adjectives.add(item);
            }
        }


        for (int i = 0; i < nouns_adjectives.size(); i++) {
            String[] found = Lemmatizer.searchLemma(nouns_adjectives.get(i));
            String[] found1;
            String[] found2;

            if (found[1].matches(NOUN) && Character.isUpperCase(found[0].charAt(0))) {
                if (!oneWord.contains(found[0])) oneWord.add(found[0]);
            }
            else if (Character.isUpperCase(nouns_adjectives.get(i).charAt(0))) {
                if (!oneWord.contains(nouns_adjectives.get(i))) oneWord.add(nouns_adjectives.get(i));
            }

            if (i + 1 < nouns_adjectives.size()){
                found1 = Lemmatizer.searchLemma(nouns_adjectives.get(i+1));
                if (found[1].matches(ADJECTIVE) && found1[1].matches(NOUN)){
                    //twoWord.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1));
                    twoWord.add(found[0] + " " + found1[0]);
                }
                else if (Character.isUpperCase(nouns_adjectives.get(i).charAt(0)) && Character.isUpperCase(nouns_adjectives.get(i + 1).charAt(0))) {
                    twoWord.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1));
                    //twoWord.add(found[0] + " " + found1[0]);
                }
            }
            if (i + 2 < nouns_adjectives.size()){
                found1 = Lemmatizer.searchLemma(nouns_adjectives.get(i+1));
                found2 = Lemmatizer.searchLemma(nouns_adjectives.get(i+2));
                if (found[1].matches(ADJECTIVE) && found1[1].matches(ADJECTIVE) && found2[1].matches(NOUN))
                    //threeWord.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1) + " " + nouns_adjectives.get(i+2));
                    threeWord.add(found[0] + " " + found1[0] + " " + found2[0]);
                else if (Character.isUpperCase(nouns_adjectives.get(i).charAt(0)) && Character.isUpperCase(nouns_adjectives.get(i + 1).charAt(0)) && Character.isUpperCase(nouns_adjectives.get(i + 2).charAt(0))) {
                    threeWord.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1) + " " + nouns_adjectives.get(i+2));
                    //threeWord.add(found[0] + " " + found1[0] + " " + found2[0]);
                }
                else if (found[1].matches(ADJECTIVE) && found1[1].matches(NOUN) && found2[1].matches(NOUN) && Character.isUpperCase(nouns_adjectives.get(i + 2).charAt(0))) {
                    //threeWord.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1) + " " + nouns_adjectives.get(i+2));
                    threeWord.add(found[0] + " " + found1[0] + " " + found2[0]);
                }
            }
        }


        ArrayList<String> toBePrinted;
        for (String s : oneWord) {
            toBePrinted = Indexer.searchEntityNominative("\"" + s + "\"", "index/dictionary_nominatives", 1);
            if (toBePrinted.size() != 0){
                System.out.print(toBePrinted.get(0) + " - ");
                System.out.println(toBePrinted.get(1));
            }
        }
        for (String s : twoWord) {
            toBePrinted = Indexer.searchEntityNominative("\"" + s + "\"", "index/dictionary_nominatives", 1);
            if (toBePrinted.size() != 0){
                System.out.print(toBePrinted.get(0) + " - ");
                System.out.println(toBePrinted.get(1));
            }
        }
        for (String s : threeWord) {
            toBePrinted = Indexer.searchEntityNominative("\"" + s + "\"", "index/dictionary_nominatives", 1);
            if (toBePrinted.size() != 0){
                System.out.print(toBePrinted.get(0) + " - ");
                System.out.println(toBePrinted.get(1));
            }
        }


    }

    public static String extract_text_from_wiki(FileInputStream file) throws IOException {
        Pattern pattern;
        Matcher matcher;
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        String text = "";

        String line = reader.readLine();
        while (line != null) {
            while (!line.matches(TITLE)) {
                line = reader.readLine();
            }
            if (line.matches(".*<title>.*(MediaWiki:|Wikipédia:|Kategória:|Pomoc:|Šablóna:|Portál|Main Page|Hlavná stránka).*</title>.*")) {
                while (!line.matches(PAGE_END)) {
                    line = reader.readLine();
                }
                continue;
            }
            pattern = Pattern.compile(TITLE);
            matcher = pattern.matcher(line);

            title = ((matcher.find() ? matcher.group(1) : "Unknown Title"));
            while (!line.matches(TEXT)) {
                line = reader.readLine();
            }

            while (!line.matches(".*'''.*'''.*")) {
                line = reader.readLine();
            }
            while (!line.equals("")) {
                text += line;
                line = reader.readLine();
            }
            text = text.replaceAll("([{]{1,2}[^{}]*[}]{1,2})", "");
            text = text.replaceAll("[\\[]{2}([ÁA-Za-zÇ-ž0-9\\s(),.:!?#]*)[|]+", "");
            text = text.replaceAll("]]", "");
            text = text.replaceAll("\\[\\[", "");
            text = text.replaceAll("(&lt;ref(\\s*name\\s*=\\s*&quot;[^&]*&quot;\\s*)?&gt;[^&]*&lt;/ref&gt;)", " ");
            text = text.replaceAll("'''", "");
            text = text.replaceAll("(&quot;)", "\"");
            text = text.replaceAll("(&lt;)", "<");
            text = text.replaceAll("(&gt;)", ">");

            while (!line.matches(".*</page>.*")) {
                line = reader.readLine();
            }
        }
        return text;
    }

    public static String extract_text(FileInputStream file) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        String text = "";

        String line = reader.readLine();
        while (line != null) {
            if (!line.equals("")) {
                text += line + "\n";
            }
            line = reader.readLine();
        }

        return text;
    }
}
