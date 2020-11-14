import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserCategories {

    public static JSONArray categoriesArray;
    public static PrintStream output;


    final static String searchMethod = "BFS";
    public static int maxDepth = 2;

    public static void main(String[] args) throws IOException {
        FileInputStream file;
        BufferedReader bufReader;
        Pattern pattern;
        Matcher matcher;
        ArrayList<String> categories;
        int depth = 0;
        boolean found;
        boolean searched;
        String redirectTitle = null;

        file = new FileInputStream("files/skwiki.xml");
        bufReader = new BufferedReader(new InputStreamReader(file));

        if (searchMethod.equals("DFS")) {
            output = new PrintStream(new FileOutputStream("output/outputDFS.txt"));
        }
        else if (searchMethod.equals("BFS")) {
            output = new PrintStream(new FileOutputStream("output/outputBFS.txt"));
        }

        categoriesArray = Parser.load_dictionary("dictionaries/dictionary_categories_new.json");
        System.setOut(output);


        String line = bufReader.readLine();
        //reads file line by line until end
        while (line != null) {
            line = bufReader.readLine();
            if (line.matches(".*<title>.*(MediaWiki:|Wikipédia:|Kategória:|Pomoc:|Šablóna:|Portál:|Main Page|Hlavná stránka|WP:|Súbor:).*</title>.*")) {
                while(!line.matches(".*</page>.*")) {
                    line = bufReader.readLine();
                }
            }
            searched = false;
            if (line.matches(".*<title>.*</title>.*")) {
                categories = new ArrayList<>();
                pattern = Pattern.compile(".*<title>(.*)</title>.*");
                matcher = pattern.matcher(line);

                while (matcher.find()) {
                    output.print(matcher.group(1));
                }

                do {
                    line = bufReader.readLine();
                    if (line.matches("[\\s]*<redirect title=\"(.*)\"[\\s]*/>[\\s]*")) {
                        pattern = Pattern.compile("[\\s]*<redirect title=\"(.*)\"[\\s]*/>[\\s]*");
                        matcher = pattern.matcher(line);
                        while (matcher.find()){
                            redirectTitle = matcher.group(1);
                        }

                        searched = true;
                        output.println(" - redirect [" + redirectTitle + "]");
                        break;
                    }
                    if (line.matches(".*\\{\\{[Rr]ozlišovacia stránka}}.*")) {
                        searched = true;
                        output.println(" - disambiguation");
                        break;
                    }
                    if (line.matches("\\[\\[Kategória:([ÁA-Za-zÇ-ž0-9\\s,.()– -]*)([|\\]]).*")) {
                        pattern = Pattern.compile("\\[\\[Kategória:([ÁA-Za-zÇ-ž0-9\\s,.()– -]*)([|\\]]).*");
                        matcher = pattern.matcher(line);

                        while (matcher.find()) {
                            categories.add(matcher.group(1));
                        }
                    }
                } while (!line.matches(".*</text.*>.*"));

                if (categories.size() < 3) maxDepth = 4;
                else maxDepth = 2;

                if (!searched){
                    // DFS
                    if (searchMethod.equals("DFS")) {
                        for (String category : categories) {
                            if (!check_category(category)) {
                                searched = search_categories_DFS(category, depth + 1);
                                if (searched) break;

                            } else {
                                output.println(" - " + selectedCategory + selectedSubCategory);
                                searched = true;
                                break;
                            }
                        }
                        if (!searched) output.println(" - unknown");
                    }
                    // BFS
                    else if (searchMethod.equals("BFS")) {
                        queue = new ArrayList<>();
                        found = false;
                        for (String category : categories) {
                            if (!check_category(category)) {
                                queue.add(category);
                            } else {
                                output.println(" - " + selectedCategory + selectedSubCategory);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            for (String cat : queue) {
                                searched = search_categories_BFS(cat, depth + 1);
                                if (searched) break;
                            }
                            if (!searched) output.println(" - unknown");
                        }
                    }
                }
                line = bufReader.readLine();
            }
        }
    }

    public static String selectedCategory;
    public static String selectedSubCategory;
    public static ArrayList<String> queue;


    private static boolean search_categories_DFS(String category, int depth) throws IOException {

        FileInputStream fileCategories;
        BufferedReader reader;
        fileCategories = new FileInputStream("files/categories.txt");
        reader = new BufferedReader(new InputStreamReader(fileCategories));

        System.setOut(output);

        String line = reader.readLine();

        while (line != null) {
            if (line.equals("NAME: " + category.replaceAll(" ", " "))) {
                line = reader.readLine();
                while (!line.matches(".*NAME:.*")) {
                    if (!check_category(line)) {
                        if (depth < maxDepth && search_categories_DFS(line.replaceAll(" ", " "), depth+1)) {
                            return true;
                        }
                    }
                    else {
                        output.println(" - " + selectedCategory + selectedSubCategory);
                        return true;
                    }
                    line = reader.readLine();
                }
                if (line.matches(".*NAME:.*")){
                    return false;
                }
            }
            line = reader.readLine();
        }
        return false;
    }

    private static boolean search_categories_BFS(String category, int depth) throws IOException {

        FileInputStream fileCategories;
        BufferedReader reader;
        fileCategories = new FileInputStream("files/categories.txt");
        reader = new BufferedReader(new InputStreamReader(fileCategories));
        ArrayList<String> queue;
        boolean found;

        if (depth == 6) {
            return false;
        }

        String line = reader.readLine();
        while (line != null) {
            if (line.equals("NAME: " + category.replaceAll(" ", " "))) {
                line = reader.readLine();
                queue = new ArrayList<>();
                while (!line.matches(".*NAME:.*")) {
                    found = check_category(line);
                    if (found) {
                        output.println(" - " + selectedCategory + selectedSubCategory);
                        return true;
                    }
                    queue.add(line.replaceAll(" ", " "));
                    line = reader.readLine();
                }
                for (String cat : queue) {
                    if (search_categories_BFS(cat, depth + 1)) {
                        return true;
                    }
                }
                return false;
            }
            line = reader.readLine();
        }

        return false;
    }

    private static boolean check_category(String string) {
        Pattern pattern;
        Matcher matcher;

        string = string.toLowerCase();
        for (Object object : categoriesArray){
            JSONObject category = (JSONObject) object;
            ArrayList<String> key_words = (ArrayList<String>) category.get("key_words");
            JSONArray subcategoriesArray = (JSONArray) category.get("subcategory");
            for (String key : key_words) {
                if (string.matches("(^|.*\\W+)" + key + "($|\\W+.*)")) {
                    pattern = Pattern.compile("(" + key + ")");
                    matcher = pattern.matcher(string);

                    if (matcher.find()) {
                        selectedCategory = (String) category.get("name");

                        for (Object ob : subcategoriesArray) {
                            JSONObject subcategory = (JSONObject) ob;
                            ArrayList<String> key_words_sub = (ArrayList<String>) subcategory.get("key_words");
                            for (String key_sub: key_words_sub) {
                                if (string.matches("(^|.*\\W+)" + key + "($|\\W+.*)")) {
                                    pattern = Pattern.compile("(" + key_sub + ")");
                                    matcher = pattern.matcher(string);
                                    if (matcher.find()) {
                                        selectedSubCategory = " (" + subcategory.get("name") + ")";
                                        return true;
                                    }
                                }
                            }
                        }
                        selectedSubCategory = "";
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
