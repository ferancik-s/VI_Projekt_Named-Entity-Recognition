
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
    final static String ENDBOX = "}}";
    final static String ABSTRACT = "\\s*(<text.*>)?'''.*'''.*";
    final static String ARTICLE = "==.*==.*";
    final static String PAGE_START = ".*<page>.*";
    final static String PAGE_END = ".*</page>.*";
    final static String TITLE = ".*<title>(.*)</title>.*";
    final static String TEXT = ".*<text.*>.*";


    public static Article article = new Article();
    public static ArrayList<String> categoryString;
    public static ArrayList<Integer> categoryInteger;
    public static ArrayList<Integer> categoryInfoboxInteger;
    public static ArrayList<Integer> categoryAbstractInteger;
    public static ArrayList<Integer> categoryArticleInteger;

    final static boolean keepRedirects = false;
    public static boolean isReference = false;
    public static final int TRESHOLD = 30;
    public static int totalMatches = 0;
    public static int totalMatchesInfobox = 0;
    public static int totalMatchesAbstract = 0;
    public static int totalMatchesArticle = 0;


    final static int numberOfCategories = 14;
    public static int[] categoriesCount = new int[numberOfCategories];
    public static int[] categoriesInfobox = new int[numberOfCategories];
    public static int[] categoriesAbstract = new int[numberOfCategories];
    public static int[] categoriesArticle = new int[numberOfCategories];
    public static String[] categoriesNames = {
            "Osoba","Stavba","Národnosť/Vierovyznanie","Organizácia",
            "Geografia","Lokalita","Produkt","Udalosť","Umenie",
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
            string = string.replaceAll("[\\[]{2}([A-zÀ-ž0-9\\s(),.:!?#]*)[|]+", "");
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
                        if( partOfText == "infobox"){
                            categoriesInfobox[index]++;
                            totalMatchesInfobox++;
                        }
                        if( partOfText == "abstract"){
                            categoriesAbstract[index]++;
                            totalMatchesAbstract++;
                        }
                        if( partOfText == "article"){
                            categoriesArticle[index]++;
                            totalMatchesArticle++;
                        }
                        categoriesCount[index]++;
                        totalMatches++;

                        /*System.out.println("LINE:\t\t" + string);
                        System.out.println("GUESS:\t\t" + category.get("name"));
                        System.out.println("KEY WORD:\t" + key);
                        System.out.println("---");*/

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

    public static void select_category() {
        categoryString = new ArrayList<>();
        categoryInteger = new ArrayList<>();
        float percent = 0;
        int newTotal = 0;
        for (int i = 0; i < numberOfCategories; i++){
            if (categoriesCount[i] != 0){
                percent = (float) categoriesCount[i] / (float) totalMatches * 100;
                if(percent > 30) {
                    categoryString.add(categoriesNames[i]);
                    categoryInteger.add(categoriesCount[i]);
                    newTotal += categoriesCount[i];
                }
                System.out.println("\t" + categoriesNames[i] + " - " + percent + "% - (" + categoriesCount[i] + ") matches");
            }
        }
        System.out.println("TOTAL MATCHES: " + totalMatches);
        System.out.println("----------");
        for (int i = 0; i < categoryInteger.size(); i++){
            percent = (float) categoryInteger.get(i) / (float) newTotal * 100;
            System.out.println(categoryString.get(i) + " - " + percent + "%");
        }
        System.out.println("----------");
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
                        if (line.matches(".*<title>.*(MediaWiki:|Wikipédia:|Kategória:|Pomoc:|Main Page|Hlavná stránka).*</title>.*")) {
                            while (!line.matches(PAGE_END)){
                                line = bufReader.readLine();
                            }
                            inPage = false;
                        } else {
                            //every correct title that matches the regex
                            if (line.matches(TITLE)) {
                                pattern = Pattern.compile(TITLE);
                                matcher = pattern.matcher(line);

                                //if there is correct one set title of page
                                article.setTitle((matcher.find() ? matcher.group(1) : "Unknown Title"));
                                System.out.println("NAME: " + article.getTitle());

                                //skips metadata until text tag
                                while (!line.matches(TEXT)) {
                                    line = bufReader.readLine();
                                }
                                if (line.matches(".*<text.*>#(REDIRECT|redirect).*</text>.*")) {
                                    continue;
                                }
                                line = line.replaceAll("(.*<.*text.*>)", "");


                                while (!line.matches(PAGE_END)) {

                                    if (line.matches(INFOBOX) || line.matches(GEOBOX)){
                                        pattern = Pattern.compile(".*\\{\\{(Infobox|Geobox)\\s*(.*)");
                                        matcher = pattern.matcher(line);
                                        if (article.getMainCategory() == null) {
                                            article.setMainCategory((matcher.find() ? matcher.group(2) : "Unknown"));
                                            System.out.println("INFOBOX CATEGORY: " + article.getMainCategory());
                                            System.out.println("----------");
                                        }

                                        while (!line.matches(ENDBOX)) {
                                            find_category_keyword(categoriesArray, line, "infobox");
                                            if (isReference) {
                                                skip_reference(line, bufReader, categoriesArray, "infobox");
                                            }
                                            line = bufReader.readLine();
                                        }
                                        if (totalMatches != 0) {
                                            System.out.println("INFOBOX:");
                                            select_category();
                                        }
                                    }
                                    if (line.matches(ABSTRACT)) {



                                        if (article.getMainCategory() == null) {
                                            article.setMainCategory("Neznáma kategória");
                                        }
                                        while (!line.matches(ARTICLE) && !line.matches(PAGE_END)) {
                                            find_category_keyword(categoriesArray, line, "abstract");
                                            if (isReference) {
                                                skip_reference(line, bufReader, categoriesArray, "abstract");
                                            }
                                            line = bufReader.readLine();
                                        }
                                        if (totalMatches != 0) {
                                            System.out.println("ABSTRACT:");
                                            select_category();
                                        }
                                    }
                                    if (line.matches(ARTICLE)) {

                                        while (!line.matches(PAGE_END)) {
                                            find_category_keyword(categoriesArray, line, "article");
                                            if (isReference) {
                                                skip_reference(line, bufReader, categoriesArray, "article");
                                            }
                                            line = bufReader.readLine();
                                        }
                                        if (totalMatches != 0) {
                                            System.out.println("ARTICLE:");
                                            select_category();
                                        }
                                    }
                                    if (line.matches(PAGE_END)) {

                                        inPage = false;
                                        article.setTitle(null);
                                        article.setMainCategory(null);
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
