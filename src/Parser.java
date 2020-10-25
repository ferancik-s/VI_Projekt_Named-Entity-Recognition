
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.text.Normalizer;
import java.util.*;


import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parser {

    final static String INFOBOX = ".*\\{\\{Infobox\\s*(.*)";
    final static String ABSTRACT = ".*'''.*'''.*";
    final static String ARTICLE = ".*==.*==.*";
    final static String PAGE_START = ".*<page>.*";
    final static String PAGE_END = ".*</page>.*";
    final static String TITLE = ".*<title>([A-Ž\\s\\S]*)</title>.*";
    final static String TEXT = ".*<text.*>.*";

    final static int numberOfCategories = 14;

    public static Article article = new Article();
    public static ArrayList<String> categoryString;
    public static ArrayList<Integer> categoryInteger;

    final static Boolean keepRedirects = false;
    public static Boolean isReference = false;
    public static int totalMatches = 0;



    public static int[] categoriesCount = new int[numberOfCategories];
    public static String[] categoriesNames = {
            "Osoba","Stavba","Národnosť/Vierovyznanie","Organizácia",
            "Geografia","Lokalita","Produkt","Udalosť","Umenie",
            "Jazyk","Organizmus","Veda","Šport","Komunikácia"
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

    public static void find_category(JSONArray categoriesArray, String line) {
        Pattern pattern;
        Matcher matcher;

        int index = 0;

        String string = Normalizer.normalize(line.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        if (!keepRedirects){
            string = string.replaceAll("[\\[]{2}([a-zA-Z\\s(),]*)[|]+", "");
        }
        if (string.matches(".*&lt;/ref&gt;.*")) {
            string = string.replaceAll(".*&lt;/ref&gt;","");
        }
        if (string.matches(".*&lt;ref(\\s*name\\s*=\\s*&quot.*&quot;\\s*)?&gt;.*")) {
            string = string.replaceAll("(&lt;ref(\\s*name\\s*=\\s*&quot;.*&quot;\\s*)?&gt;)([a-zA-Z0-9\\s,.?:\"'(){}]*[&]{0})(&lt;/ref&gt;)?","");
            isReference = true;
        }
        for (Object object : categoriesArray){
            JSONObject category = (JSONObject) object;
            ArrayList<String> key_words = (ArrayList<String>) category.get("key_words");
            //JSONArray subcategoriesArray = (JSONArray) category.get("subcategory");
            for (String key : key_words) {
                if (string.matches(".*[\\s+\\[(]" + key + ".*")) {
                    pattern = Pattern.compile("(" + key + ")");
                    matcher = pattern.matcher(string);

                    //if there is correct one set title of page
                    while (matcher.find()) {
                        categoriesCount[index]++;
                        totalMatches++;
                    }
                    //System.out.println("NAME:\t\t" + article.title);
                    //System.out.println("CATEGORY:\t" + article.mainCategory);
                    //System.out.println("LINE:\t\t" +line);
                    //System.out.println("GUESS:\t\t" + category.get("name"));
                    //System.out.println("KEY WORD:\t" + key);
                    //System.out.println("-----------------");
                    //categoriesCount[count]++;
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
                        bufReader.mark(1);
                        line = bufReader.readLine();
                        //ignore these pages
                        if (
                                line.matches(".*<title>.*MediaWiki:.*</title>.*") ||
                                        line.matches(".*<title>.*Pomoc:.*</title>.*") ||
                                        line.matches(".*<title>.*Wikipédia:.*</title>.*") ||
                                        line.matches(".*<title>.*Main Page.*</title>.*")  ||
                                        line.matches(".*<title>.*Hlavná stránka.*</title>.*")
                        ) {
                            //skip whole page
                            while (!line.matches(PAGE_END)){
                                line = bufReader.readLine();
                            }
                            inPage = false;
                            continue;
                        } else {
                            //every correct title that matches the regex
                            if (line.matches(TITLE)) {
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
                                        System.out.println(categoriesNames[i] + " - " + percent + "% - (" + categoriesCount[i] + ") matches");
                                    }
                                }
                                System.out.println("TOTAL MATCHES: " + totalMatches);
                                System.out.println("----result----");
                                for (int i = 0; i < categoryInteger.size(); i++){
                                    percent = (float) categoryInteger.get(i) / (float) newTotal * 100;
                                    System.out.println(categoryString.get(i) + " - " + percent + "%");
                                }

                                totalMatches = 0;
                                categoriesCount = new int[numberOfCategories];
                                System.out.println("--------------");
                                pattern = Pattern.compile(TITLE);
                                matcher = pattern.matcher(line);
                                //if there is correct one set title of page
                                if (matcher.find()) {
                                    article.setTitle(matcher.group(1));
                                    System.out.println("NAME: " + article.getTitle());
                                }
                                else {
                                    article.setTitle("No title");
                                }
                                //skips metadata until text tag
                                while (!line.matches(TEXT)) {
                                    line = bufReader.readLine();
                                }
                                if (line.matches(TEXT)) {
                                    while (!line.matches(INFOBOX)) {
                                        if (line.matches(ABSTRACT)) { break; }
                                        if (line.matches(ARTICLE))  { break; }
                                        if (line.matches(PAGE_END)) { break; }
                                        find_category(categoriesArray, line);
                                        if (isReference) {
                                            while (!line.matches(".*&lt;/ref&gt;.*")) {
                                                line = bufReader.readLine();
                                            }
                                            isReference = false;
                                            find_category(categoriesArray, line);
                                        }
                                        line = bufReader.readLine();
                                    }
                                }
                                if (line.matches(INFOBOX)){
                                    pattern = Pattern.compile(INFOBOX);
                                    matcher = pattern.matcher(line);
                                    if (matcher.find()) {
                                        article.setMainCategory(matcher.group(1));
                                        System.out.println("CATEGORY: " + article.getMainCategory());
                                    }
                                    else {
                                        article.setMainCategory("Unknown");
                                    }
                                    while (!line.matches(ABSTRACT)) {
                                        if (line.matches(ARTICLE))  { break; }
                                        if (line.matches(PAGE_END)) { break; }
                                        find_category(categoriesArray, line);
                                        if (isReference) {
                                            while (!line.matches(".*&lt;/ref&gt;.*")) {
                                                line = bufReader.readLine();
                                            }
                                            isReference = false;
                                            find_category(categoriesArray, line);
                                        }
                                        line = bufReader.readLine();
                                    }
                                }
                                if (line.matches(ABSTRACT)) {
                                    while (!line.matches(ARTICLE)) {
                                        if (line.matches(PAGE_END)) { break; }
                                        find_category(categoriesArray, line);
                                        if (isReference) {
                                            while (!line.matches(".*&lt;/ref&gt;.*")) {
                                                line = bufReader.readLine();
                                            }
                                            isReference = false;
                                            find_category(categoriesArray, line);
                                        }
                                        line = bufReader.readLine();
                                    }
                                }
                                if (line.matches(ARTICLE)) {
                                    while (!line.matches(PAGE_END)) {
                                        //find_category(categoriesArray, line);
                                        line = bufReader.readLine();
                                    }
                                }
                            }
                        }
                        inPage = true;
                    }
                    if (line.matches(PAGE_END)) {
                        inPage = false;
                        article.setTitle("");
                        article.setMainCategory("");
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
