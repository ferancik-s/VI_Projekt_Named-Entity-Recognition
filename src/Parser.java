
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
    final static String ENDBOX = "(.*}}\\[\\[.*|}}'''.*|[|\\s]*}}[\\s]*|[^{]*}}[\\s]*|.*\\{\\{Infobox.*}})";
    final static String ABSTRACT = "\\s*(<text.*>)?'''.*'''.*";
    final static String ARTICLE = "==.*==.*";
    final static String PAGE_START = ".*<page>.*";
    final static String PAGE_END = ".*</page>.*";
    final static String TITLE = ".*<title>(.*)</title>.*";
    final static String TEXT = ".*<text.*>.*";
    final static String TEXTEND = ".*</text.*>.*";

    public static ArrayList<String> categoriesList;

    public static Article article = new Article();

    public static boolean isReference = false;
    public static final int TRESHOLD = 50;
    public static int totalMatches = 0;
    public static String categoryString;


    final static int numberOfCategories = 5;
    public static int[] categoriesCount = new int[numberOfCategories];
    public static String[] categoriesNames = {
            "location","organization","person","miscellaneous",
            "non-entity"
    };

    public static JSONArray load_dictionary() {
        JSONParser parser = new JSONParser();
        JSONArray categoriesArray = null;
        try {
            categoriesArray = (JSONArray) parser.parse(new FileReader("dictionaries/dictionary_categories_slim.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categoriesArray;
    }

    public static void find_category_keyword(JSONArray categoriesArray, String line, String partOfText) {
        Pattern pattern;
        Matcher matcher;

        int index = 0;

        //lowercase
        String string = line.toLowerCase();
        //remove redirects
        string = string.replaceAll("[\\[]{2}([ÁA-Za-zÇ-ž0-9\\s(),.:!?#]*)[|]+", "");
        string = string.replaceAll("]]", "");
        string = string.replaceAll("\\[\\[", "");
        //remove reference
        string = string.replaceAll("(&lt;ref(\\s*name\\s*=\\s*&quot.*&quot;\\s*)?&gt;.*?&lt;/ref&gt;)", "");
        string = string.replaceAll(".*&lt;/ref&gt;","");
        if (string.matches(".*&lt;ref(\\s*name\\s*=\\s*&quot.*&quot;\\s*)?&gt;.*")) {
            string = string.replaceAll("(&lt;ref(\\s*name\\s*=\\s*&quot;.*&quot;\\s*)?&gt;.*)","");
            isReference = true;
        }
        //remove long spaces
        string = string.replaceAll("[\\s]{2,}", " ");

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
                    }
                }
            }
            index++;
        }
    }

    public static PrintStream matched;


    public static void match_category() {
        System.setOut(matched);


        if (article.getCategoryFromInfobox() == null && article.getCategoryFromAbstract() == null && article.getCategoryFromArticle() == null) {
            matched.print(article.getTitle());
            matched.println(" - unknown");

        } else {
            matched.print(article.getTitle());
            if(article.getCategoryFromInfobox() != null) {
                matched.print(" - " + article.getCategoryFromInfobox());
            }
            if(article.getCategoryFromAbstract() != null) {
                matched.print(" - " + article.getCategoryFromAbstract());
            }
            if(article.getCategoryFromArticle() != null) {
                matched.print(" - " + article.getCategoryFromArticle());
            }

            matched.println();
        }

    }

    public static boolean select_category(String partOfText) {

        categoryString = null;
        int max = 0;
        if (totalMatches > 0) {
            for (int i = 0; i < numberOfCategories; i++) {
                if (categoriesCount[i] > max) {
                    max = categoriesCount[i];
                    categoryString = categoriesNames[i];
                }
            }

            categoriesCount = new int[numberOfCategories];

            switch (partOfText) {
                case "infobox" -> {
                    article.setCategoryFromInfobox(totalMatches > 4 ? categoryString : null);
                    totalMatches = 0;
                    return true;
                }
                case "abstract" -> {
                    article.setCategoryFromAbstract(totalMatches > 4 ? categoryString : null);
                    totalMatches = 0;
                    return true;
                }
                case "article" -> {
                    article.setCategoryFromArticle(totalMatches > 20 ? categoryString : null);
                    totalMatches = 0;
                    return true;
                }
            }
        }


        return false;
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

            matched = new PrintStream(new FileOutputStream("output/outputFULLTEXT.txt"));

            file = new FileInputStream("files/short.xml");
            bufReader = new BufferedReader(new InputStreamReader(file));

            String line = bufReader.readLine();
            //reads file line by line until end
            while (line != null){
                line = bufReader.readLine();

                //ignore siteinfo
                if (line.matches(".*<siteinfo>.*")) {
                    while (!line.matches(".*</siteinfo>.*")){
                        line = bufReader.readLine();
                    }
                }
                //checks if page starts
                if (line.matches(PAGE_START) || inPage) {
                    inPage = true;
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

                            article.setTitle((matcher.find() ? matcher.group(1) : "Unknown Title"));

                            //skips metadata until text tag
                            while (!line.matches(TEXT)) {
                                line = bufReader.readLine();
                            }
                            if (line.matches(".*#(REDIRECT|redirect|Redirect|presmeruj|Presmeruj|PRESMERUJ).*")) {
                                matched.println(article.getTitle() + " - redirect");
                                while (!line.matches(PAGE_END)) {
                                    line = bufReader.readLine();
                                }
                                continue;
                            }

                            //if there is correct one set title of page
                            line = line.replaceAll("(.*<.*text.*>)", "");

                            while (!line.matches(PAGE_END)) {

                                if (line.matches(INFOBOX) || line.matches(GEOBOX)){
                                    pattern = Pattern.compile(".*\\{\\{(Infobox|Geobox\\s*[|]{1})\\s*([ÁA-Za-zÇ-ž0-9,.:()!?-_'\"\\s]*)(|)?");
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
                                            if(select_category("infobox")) {
                                                while (!line.matches(TEXTEND)) {
                                                    line = bufReader.readLine();
                                                }
                                                continue;
                                            }
                                        }
                                        bufReader.reset();
                                    }
                                }
                                if (line.matches(ABSTRACT)) {
                                    while (!line.matches("") && !line.matches(".*\\{\\{.*}}.*")) {
                                        find_category_keyword(categoriesArray, line, "abstract");
                                        if (isReference) {
                                            skip_reference(line, bufReader, categoriesArray, "abstract");
                                        }
                                        line = bufReader.readLine();
                                    }
                                    if (totalMatches != 0) {
                                        if(select_category("abstract")) {
                                            while (!line.matches(TEXTEND)) {
                                                line = bufReader.readLine();
                                            }
                                            continue;
                                        }
                                    }
                                }
                                if (line.matches(".*\\{\\{(R|r)ozlišovacia stránka}}.*")) {
                                    matched.println(article.getTitle() + " - disambiguation");
                                    while (!line.matches(PAGE_END)) {
                                        line = bufReader.readLine();
                                    }
                                    continue;
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
                                        if(select_category("article")) {
                                            while (!line.matches(TEXTEND)) {
                                                line = bufReader.readLine();
                                            }
                                            continue;
                                        }
                                    }
                                }
                                if (line.matches(".*\\{\\{(R|r)ozlišovacia stránka}}.*")) {
                                    matched.println(article.getTitle() + " - disambiguation");
                                    while (!line.matches(PAGE_END)) {
                                        line = bufReader.readLine();
                                    }
                                    continue;
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
        } catch (FileNotFoundException e) {
            System.out.println("Error while reading the file!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
