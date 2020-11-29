import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ParserCategories {

    public static JSONArray categoriesArray;
    public static PrintStream output;


    public static int maxDepth = 2;
    public static String title = "";


    /**
     * This method loads and stores dictionary JSON from given file
     * @param path - path for given JSON file
     * @return - JSONArray of parsed JSON file
     */
    public static JSONArray load_dictionary(String path) {
        JSONParser parser = new JSONParser();
        JSONArray categoriesArray = null;
        try {
            categoriesArray = (JSONArray) parser.parse(new FileReader(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categoriesArray;
    }



    public static void extractEntities(String searchMethod) throws IOException {

        ArrayList<String> categories;
        int depth = 0;
        boolean found;
        boolean searched;
        String redirectTitle = null;

        FileInputStream file = new FileInputStream("files/skwiki.xml");
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(file));

        if (searchMethod.equals("DFS")) {
            output = new PrintStream(new FileOutputStream("output/outputDFS.txt"));
        }
        else if (searchMethod.equals("BFS")) {
            output = new PrintStream(new FileOutputStream("output/outputBFS.txt"));
        }


        categoriesArray = load_dictionary("dictionaries/dictionary_categories_new.json");

        // cca 9707.585 sec (line by line approach) cca 370 sec indexed
        // execution time counter
        long startTime = System.currentTimeMillis();
        System.out.println("Extracting named entities...");

        String line = bufReader.readLine();
        //reads file line by line until end
        while (line != null) {

            // skips lines which contains not important articles right at the beginning of page
            if (line.matches(".*<title>.*(MediaWiki:|Wikipédia:|Kategória:|Pomoc:|Šablóna:|Portál:|Main Page|Hlavná stránka|WP:|Súbor:|Špeciálne:).*</title>.*")) {
                while(!line.matches(".*</page>.*")) {
                    line = bufReader.readLine();
                }
            }
            searched = false; // means that the page has not been searched yet
            if (line.matches(".*<title>.*</title>.*")) {
                categories = new ArrayList<>();
                Pattern pattern = Pattern.compile(".*<title>(.*)</title>.*");
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    title = matcher.group(1);
                }

                // if article is some kind of list is marked as non-entity / removed
                if (title.matches("^Zoznam.*")) {
                    searched = true;
                    output.println(title + " [non-entity]");
                }
                // if title is specific day in calendar
                else if (title.matches("^[0-3]*[0-9][.][\\s]*(((janu|febru)ár|marec|apríl|máj|jún|júl|august)(a)?|(septem|októ|novem|decem)(ber|bra))$")) {
                    searched = true;
                    output.println(title + " [time (day)]");
                }
                // if title is year or any kind of number
                else if (title.matches("^([\\d\\s]+)([\\s]pred[\\s]Kr.)?$")) {
                    searched = true;
                    output.println(title + " [time (year)]");
                }
                // if title is century
                else if (title.matches("^(([0-9]*[0-9][.][\\s]roky[\\s])?[0-9]*[0-9][.][\\s]storoč(ie|ia|í)[\\s]*(pred[\\s]Kr([.]|istom))?)$")) {
                    searched = true;
                    output.println(title + " [time (century)]");
                }
                // if title is millennium
                else if (title.matches("^([0-9]*[0-9][.][\\s]tisícroč(ie|ia|í)[\\s]*(pred[\\s]Kr([.]|istom))?)$")) {
                    searched = true;
                    output.println(title + " [time (millennium)]");
                }
                // if title is .xx (country domain)
                else if (title.matches("^[.][\\S]{2}$")) {
                    searched = true;
                    output.println(title + " [miscellaneous]");
                }
                //if title is letter or some kind of mark
                else if (title.matches("^[\\S]$")) {
                    searched = true;
                    output.println(title + " [non-entity]");
                }
                // if title is month
                else if (title.matches("^((Janu|Febru)ár|Marec|Apríl|Máj|Jún|Júl|August|(Septem|Októ|Novem|Decem)ber)$")) {
                    searched = true;
                    output.println(title + " [time (month)]");
                }
                // if title is day of week
                else if (title.matches("^((Pondel|Utor|Štvrt|Piat)ok|Streda)$")) {
                    searched = true;
                    output.println(title + " [time (day)]");
                }

                if (!searched) {
                    do {
                        line = bufReader.readLine();
                        //if article is a redirect to different article it marks as redirect and add original article name in square brackets
                        if (line.matches("[\\s]*<redirect title=\"(.*)\"[\\s]*/>[\\s]*")) {
                            pattern = Pattern.compile("[\\s]*<redirect title=\"(.*)\"[\\s]*/>[\\s]*");
                            matcher = pattern.matcher(line);
                            while (matcher.find()) {
                                redirectTitle = matcher.group(1);
                            }
                            searched = true;
                            output.println(title + " [redirect] {" + redirectTitle + "}");
                            break;
                        }
                        // if article is disambiguation page it marks as disambiguation
                        if (line.matches(".*\\{\\{[Rr]ozlišovacia stránka}}.*")) {
                            searched = true;
                            output.println(title + " [disambiguation]");
                            break;
                        }
                        // finally if article is not redirect or disambiguation it collects all categories
                        if (line.matches("\\[\\[[Kk]ategória:[\\s]*([ÁA-Za-zÇ-ž0-9\\s,.()– -]*)([|][^]]*)?([\\s\\]]*)(</text>)?")) {
                            pattern = Pattern.compile("\\[\\[[Kk]ategória:[\\s]*([ÁA-Za-zÇ-ž0-9\\s,.()– -]*)([|\\]])");
                            matcher = pattern.matcher(line);

                            while (matcher.find()) {
                                categories.add(matcher.group(1));
                            }
                        }
                    } while (!line.matches(".*</text.*>.*"));
                }

                if (!searched){
                    // DFS
                    // first checks extracted category and then recursively
                    // nests into categories of given categories until match found or max depth reached
                    if (searchMethod.equals("DFS")) {
                        // if the number of categories for specific article is less than 3 max search depth is set to 4
                        // if it is more than max depth is set to 2 (only applies when using DFS for searching)
                        if (categories.size() < 3) maxDepth = 4;
                        else maxDepth = 2;
                        for (String category : categories) {
                            if (!check_category(category)) {
                                searched = search_categories_DFS(category, depth + 1);
                                if (searched) break;

                            } else {
                                output.println(title + " [" + selectedCategory + selectedSubCategory + "]");
                                searched = true;
                                break;
                            }
                        }
                        if (!searched) output.println(title + " [unknown]");
                    }
                    // BFS
                    // first checks all extracted categories and then recursively
                    // nests categories and search them gradually until match found or max depth reached
                    else if (searchMethod.equals("BFS")) {
                        queue = new ArrayList<>();
                        found = false;
                        for (String category : categories) {
                            if (!check_category(category)) {
                                queue.add(category);
                            } else {
                                output.println(title + " [" + selectedCategory + selectedSubCategory + "]");
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            for (String cat : queue) {
                                searched = search_categories_BFS(cat, depth + 1);
                                if (searched) break;
                            }
                            if (!searched) output.println(title + " [unknown]");
                        }
                    }
                }
            }
            line = bufReader.readLine();
        }
        System.out.println("Entities categorized in: " + (float)(System.currentTimeMillis() - startTime)/1000 + " sec");

    }

    public static String selectedCategory;
    public static String selectedSubCategory;
    public static ArrayList<String> queue;

    /**
     * This method recursively searches categories and prints labeled entities into text file
     * @param category - category to search recursively
     * @param depth - aktual depth of recursion
     * @return - boolean (found matching category or not)
     */
    private static boolean search_categories_DFS(String category, int depth) throws IOException {

        String searchedCategory = "\"" + category.replaceAll("[ \\s]+", " ") + "\"";
        ArrayList<String> categoriesArray = Indexer.searchCategoriesTree(searchedCategory);

        if (depth == maxDepth || categoriesArray == null) {
            return false;
        }

        for (String cat: categoriesArray) {
            if (!check_category(cat)) {
                if(search_categories_DFS(cat, depth + 1)) {
                    return true;
                }
            }
            else {
                output.println(title + " [" + selectedCategory + selectedSubCategory + "]");
                return true;
            }
        }
        return false;
    }

    /**
     * This method recursively searches categories and prints labeled entities into text file
     * @param category - category to search recursively
     * @param depth - aktual depth of recursion
     * @return - boolean (found matching category or not)
     */
    private static boolean search_categories_BFS(String category, int depth) {

        ArrayList<String> queue = new ArrayList<>();
        boolean found;

        String searchedCategory = "\"" + category.replaceAll("[ \\s]+", " ") + "\"";
        ArrayList<String> categoriesArray = Indexer.searchCategoriesTree(searchedCategory);


        if (depth == 10 || categoriesArray == null) {
            return false;
        }

        for (String cat: categoriesArray) {
            found = check_category(cat);
            if (found) {
                output.println(title + " [" + selectedCategory + selectedSubCategory + "]");
                return true;
            }
            queue.add(cat);
        }
        for (String cat: queue) {
            if (search_categories_BFS(cat, depth + 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method looks for match of the word in JSON dictionary with given string
     * @param string -
     * @return
     */
    private static boolean check_category(String string) {
        Pattern pattern;
        Matcher matcher;

        string = string.toLowerCase();
        // first searches for the general category
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

                        // if general category was matched, searches for specific category
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
