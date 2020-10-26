
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.util.*;


import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parser {

    final static String INFOBOX = ".*\\{\\{Infobox\\s*(.*)";
    final static String GEOBOX = ".*\\{\\{Geobox\\s*(.*)";
    final static String ENDBOX = "[|\\s]*}}";
    final static String ABSTRACT = "\\s*(<text.*>)?'''.*'''.*";
    final static String ARTICLE = "==.*==.*";
    final static String PAGE_START = ".*<page>.*";
    final static String PAGE_END = ".*</page>.*";
    final static String TITLE = ".*<title>(.*)</title>.*";
    final static String TEXT = ".*<text.*>.*";
    final static String TEXTEND = ".*</text.*>.*";



    public static Article article = new Article();

    final static boolean keepRedirects = false;
    public static boolean isReference = false;
    public static final int TRESHOLD = 50;
    public static int totalMatches = 0;
    public static String categoryString;


    final static int numberOfCategories = 13;
    public static int[] categoriesCount = new int[numberOfCategories];
    public static int[] categoriesInfobox = new int[numberOfCategories];
    public static int[] categoriesAbstract = new int[numberOfCategories];
    public static int[] categoriesArticle = new int[numberOfCategories];
    public static String[] categoriesNames = {
            "Osoba","Stavba","Národnosť/Vierovyznanie","Organizácia",
            "Geografia","Produkt","Udalosť","Umenie",
            "Jazyk","Organizmus","Veda","Šport","Médiá"
    };

    public static JSONArray load_dictionary() {
        JSONParser parser = new JSONParser();
        JSONArray categoriesArray = null;
        try {
            categoriesArray = (JSONArray) parser.parse(new FileReader("src/dictionary_categories.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categoriesArray;
    }

    public static void find_category_keyword(JSONArray categoriesArray, String line, String partOfText) {
        Pattern pattern;
        Matcher matcher;

        int index = 0;

        String string = line.toLowerCase();
        string = string.replaceAll("", "");
        if (!keepRedirects){
            string = string.replaceAll("[\\[]{2}([A-Za-zÇ-ž0-9\\s(),.:!?#]*)[|]+", "");
        }
        if (string.matches(".*(&lt;ref(\\s*name\\s*=\\s*&quot.*&quot;\\s*)?&gt;.*&lt;/ref&gt;).*")) {
            string = string.replaceAll("(&lt;ref(\\s*name\\s*=\\s*&quot.*&quot;\\s*)?&gt;.*?&lt;/ref&gt;)", "");
        }
        if (string.matches(".*&lt;/ref&gt;.*")) {
            string = string.replaceAll(".*&lt;/ref&gt;","");
        }
        if (string.matches(".*&lt;ref(\\s*name\\s*=\\s*&quot.*&quot;\\s*)?&gt;.*")) {
            string = string.replaceAll("(&lt;ref(\\s*name\\s*=\\s*&quot;.*&quot;\\s*)?&gt;.*)","");
            isReference = true;
        }
        for (Object object : categoriesArray){
            JSONObject category = (JSONObject) object;
            ArrayList<String> key_words = (ArrayList<String>) category.get("key_words");
            //JSONArray subcategoriesArray = (JSONArray) category.get("subcategory");
            for (String key : key_words) {
                if (string.matches("(^|.*\\W+)" + key + "($|\\W+.*)")) {
                    pattern = Pattern.compile("(" + key + ")");
                    matcher = pattern.matcher(string);

                    while (matcher.find()) {
                        categoriesCount[index]++;
                        totalMatches++;

                        //System.out.println("LINE:\t\t" + string);
                        //System.out.println("GUESS:\t\t" + category.get("name"));
                        //System.out.println("KEY WORD:\t" + key);
                        //System.out.println("---");

                    }
                    /*
                    if (subcategoriesArray != null) {
                        for (Object subcategory : subcategoriesArray){
                            JSONObject subcategoryJSON = (JSONObject) subcategory;
                            ArrayList<String> subcategory_key_words = (ArrayList<String>) subcategoryJSON.get("key_words");
                        }
                    }
                    */
                }
                //System.out.println(key);
            }
            index++;
        }
    }

    public static PrintStream out;
    public static PrintStream matched;
    public static PrintStream unmatched;

    public static void match_category() {
        System.setOut(out);
        System.setOut(matched);
        System.setOut(unmatched);

//        out.println("NAME: " + article.getTitle());
//        out.println("CATEGORY INFOBOX: " + article.getMainCategory());
//        out.println("CATEGORY: " + article.getCategoryFromInfobox() + "," +  article.getCategoryFromAbstract() + "," + article.getCategoryFromArticle());

        if (article.getCategoryFromInfobox() == null && article.getCategoryFromAbstract() == null && article.getCategoryFromArticle() == null) {
            unmatched.println(article.getTitle() + "\t{" + article.getMainCategory() + "}");
        } else {
            matched.println(article.getTitle() + "\t{" + article.getMainCategory() + "}\t[" + article.getCategoryFromInfobox() + ", " + article.getCategoryFromAbstract() + ", " + article.getCategoryFromArticle() + "]");
        }

    }

    public static void select_category(String partOfText) {
        System.setOut(out);

        categoryString = null;
        int max = 0;
        if (totalMatches > 4) {
            for (int i = 0; i < numberOfCategories; i++) {
                if (categoriesCount[i] > max) {
                    max = categoriesCount[i];
                    categoryString = categoriesNames[i];
                }
            }

            switch (partOfText) {
                case "infobox" -> article.setCategoryFromInfobox(totalMatches > 4 ? categoryString : null);
                case "abstract" -> article.setCategoryFromAbstract(totalMatches > 8 ? categoryString : null);
                case "article" -> article.setCategoryFromArticle(totalMatches > 20 ? categoryString : null);
            }
        }

        totalMatches = 0;
        categoriesCount = new int[numberOfCategories];

    }

    public static void skip_reference(String line, BufferedReader bufReader, JSONArray categoriesArray, String partOfText) throws IOException {
        while (!line.matches(".*&lt;/ref&gt;.*")) {
            line = bufReader.readLine();
        }
        isReference = false;
        find_category_keyword(categoriesArray, line, partOfText);
    }


    public static void main(String[] args) {
        FileInputStream file;
        BufferedReader bufReader;
        Pattern pattern;
        Matcher matcher;

        boolean inPage = false;

        try {
            JSONArray categoriesArray = load_dictionary();

            out = new PrintStream(new FileOutputStream("output.txt"));
            matched = new PrintStream(new FileOutputStream("matched.txt"));
            unmatched = new PrintStream(new FileOutputStream("unmached.txt"));
            System.setOut(out);

            file = new FileInputStream("src/skwiki.xml");
            bufReader = new BufferedReader(new InputStreamReader(file));

            String line = bufReader.readLine();
            //reads file line by line until end
            while (line != null){
                line = bufReader.readLine();
                if (line != null) {
                    //ignore siteinfo
                    if (line.matches(".*<siteinfo>.*")) {
                        while (!line.matches(".*</siteinfo>.*")){
                            line = bufReader.readLine();
                        }
                    }
                    //checks if page starts
                    if (line.matches(PAGE_START) || inPage) {
                        inPage = true;
                        bufReader.mark(1);
                        line = bufReader.readLine();
                        //ignore these pages
                        if (line.matches(".*<title>.*(MediaWiki:|Wikipédia:|Kategória:|Pomoc:|Šablóna:|Portál|Main Page|Hlavná stránka).*</title>.*")) {
                            while (!line.matches(PAGE_END)){
                                line = bufReader.readLine();
                            }
                            inPage = false;
                        } else {
                            //every correct title that matches the regex
                            if (line.matches(TITLE)) {
                                pattern = Pattern.compile(TITLE);
                                matcher = pattern.matcher(line);

                                //skips metadata until text tag
                                while (!line.matches(TEXT)) {
                                    line = bufReader.readLine();
                                }
                                if (line.matches(".*<text.*>#(REDIRECT|redirect|Redirect).*</text>.*")) {
                                    continue;
                                }

                                //if there is correct one set title of page
                                article.setTitle((matcher.find() ? matcher.group(1) : "Unknown Title"));

                                line = line.replaceAll("(.*<.*text.*>)", "");

                                while (!line.matches(PAGE_END)) {

                                    if (line.matches(INFOBOX) || line.matches(GEOBOX)){
                                        pattern = Pattern.compile(".*\\{\\{(Infobox|Geobox\\s*[|]{1})\\s*([A-Za-zÇ-ž0-9,.:()!?-_'\"\\s]*)(|)?");
                                        matcher = pattern.matcher(line);
                                        if (article.getMainCategory() == null) {
                                            article.setMainCategory((matcher.find() ? matcher.group(2) : null));
                                        }

                                        while (!line.matches(ENDBOX)) {
                                            find_category_keyword(categoriesArray, line, "infobox");
                                            if (isReference) {
                                                skip_reference(line, bufReader, categoriesArray, "infobox");
                                            }
                                            line = bufReader.readLine();
                                        }
                                        if (totalMatches != 0) {
                                            bufReader.mark(2500);
                                            if (!bufReader.readLine().matches(".*\\{\\{Infobox.*")) {
                                                select_category("infobox");
                                            }
                                            bufReader.reset();
                                        }
                                    }
                                    if (line.matches(ABSTRACT)) {

                                        while (!line.matches(ARTICLE) && !line.matches(TEXTEND)) {
                                            find_category_keyword(categoriesArray, line, "abstract");
                                            if (isReference) {
                                                skip_reference(line, bufReader, categoriesArray, "abstract");
                                            }
                                            line = bufReader.readLine();
                                        }
                                        if (totalMatches != 0) {
                                            select_category("abstract");
                                        }
                                    }
                                    if (line.matches(ARTICLE)) {

                                        while (!line.matches(TEXTEND)) {
                                            find_category_keyword(categoriesArray, line, "article");
                                            if (isReference) {
                                                skip_reference(line, bufReader, categoriesArray, "article");
                                            }
                                            line = bufReader.readLine();
                                        }
                                        if (totalMatches != 0) {
                                            select_category("article");
                                        }
                                    }
                                    if (line.matches(TEXTEND)) {
                                        while (!line.matches(PAGE_END)) {
                                            line = bufReader.readLine();
                                        }
                                        inPage = false;
                                        match_category();
                                        article.setTitle(null);
                                        article.setMainCategory(null);
                                        article.setCategoryFromInfobox(null);
                                        article.setCategoryFromAbstract(null);
                                        article.setCategoryFromArticle(null);
                                        continue;
                                    }
                                    line = bufReader.readLine();
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error while reading the file!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
