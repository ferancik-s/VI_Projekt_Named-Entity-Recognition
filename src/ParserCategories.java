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

    public static void load_dictionary() {
        JSONParser parser = new JSONParser();
        try {
            categoriesArray = (JSONArray) parser.parse(new FileReader("dictionaries/dictionary_categories_new.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String searchMethod = "DFS";
    public static int maxDepth = 2;
    public static String title = "";

    public static void main(String[] args) throws IOException {
        FileInputStream file;
        BufferedReader bufReader;
        Pattern pattern;
        Matcher matcher;
        ArrayList<String> categories;
        int depth = 0;
        boolean found;
        boolean searched;

        file = new FileInputStream("files/short.xml");
        bufReader = new BufferedReader(new InputStreamReader(file));

        if (searchMethod == "DFS") {
            output = new PrintStream(new FileOutputStream("output/outputDFS.txt"));
        }
        else if (searchMethod == "BFS") {
            output = new PrintStream(new FileOutputStream("output/outputBFS.txt"));
        }

<<<<<<< HEAD
        categoriesArray = Parser.load_dictionary("dictionaries/dictionary_categories_new.json");
=======
        load_dictionary();
        System.setOut(output);
>>>>>>> parent of 4340e13... redirect categories extraction && testing recognizing entities in text

        // cca 9707.585 sec
        // execution time counter
        long startTime = System.currentTimeMillis();

        String line = bufReader.readLine();
        //reads file line by line until end
        while (line != null) {
<<<<<<< HEAD

            // skips lines which contains not important articles right at the beginning of page
            if (line.matches(".*<title>.*(MediaWiki:|Wikipédia:|Kategória:|Pomoc:|Šablóna:|Portál:|Main Page|Hlavná stránka|WP:|Súbor:|Špeciálne:).*</title>.*")) {
=======
            line = bufReader.readLine();
            if (line.matches(".*<title>.*(MediaWiki:|Wikipédia:|Kategória:|Pomoc:|Šablóna:|Portál|Main Page|Hlavná stránka).*</title>.*")) {
>>>>>>> parent of 4340e13... redirect categories extraction && testing recognizing entities in text
                while(!line.matches(".*</page>.*")) {
                    line = bufReader.readLine();
                }
            }
            searched = false; // means that the page has not been searched yet
            if (line.matches(".*<title>.*</title>.*")) {
                categories = new ArrayList<>();
                pattern = Pattern.compile(".*<title>(.*)</title>.*");
                matcher = pattern.matcher(line);

                while (matcher.find()) {
                    title = matcher.group(1);
                }

                // if article is some kind of list is marked as non-entity / removed
                if (title.matches("^Zoznam.*")) {
                    searched = true;
                    //output.println(title + " - non-entity");
                }
                // if title is specific day in calendar
                else if (title.matches("^[0-3]*[0-9][.][\\s]*(((janu|febru)ár|marec|apríl|máj|jún|júl|august)(a)?|(septem|októ|novem|decem)(ber|bra))$")) {
                    searched = true;
                    //output.println(title + " - time (day)");
                }
                // if title is year or any kind of number
                else if (title.matches("^([\\d\\s]+)([\\s]pred[\\s]Kr.)?$")) {
                    searched = true;
                    //output.println(title + " - time (year)");
                }
                // if title is century
                else if (title.matches("^(([0-9]*[0-9][.][\\s]roky[\\s])?[0-9]*[0-9][.][\\s]storoč(ie|ia|í)[\\s](pred[\\s]Kr([.]|istom))?)$")) {
                    searched = true;
                    //output.println(title + " - time (century)");
                }
                // if title is .xx (country domain)
                else if (title.matches("^[.][\\S]{2}$")) {
                    searched = true;
                }
                //if title is letter or some kind of mark
                else if (title.matches("^[\\S]$")) {
                    searched = true;
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
<<<<<<< HEAD



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
                            break;
                        }
                        // finally if article is not redirect or disambiguation it collects all categories
                        if (line.matches("\\[\\[Kategória:([ÁA-Za-zÇ-ž0-9\\s,.()– -]*)([|\\]]).*")) {
                            pattern = Pattern.compile("\\[\\[Kategória:([ÁA-Za-zÇ-ž0-9\\s,.()– -]*)([|\\]]).*");
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
=======
                while (!line.matches(".*\\[\\[Kategória:([ÁA-Za-zÇ-ž0-9\\s,.()– -]*)([|\\]]).*") && !line.matches(".*</text.*>.*")) {
                    line = bufReader.readLine();
                    if (line.matches(".*#(REDIRECT|redirect|Redirect|presmeruj|Presmeruj|PRESMERUJ).*")) {
                        searched = true;
                        output.println(" - redirect");
                        break;
                    }
                    if (line.matches(".*\\{\\{(R|r)ozlišovacia stránka}}.*")) {
                        searched = true;
                        output.println(" - disambiguation");
                        break;
                    }
                }
                while (!line.matches(".*</text.*>.*") && !searched) {
                    pattern = Pattern.compile("\\[\\[Kategória:([ÁA-Za-zÇ-ž0-9\\s,.()– -]*)([|\\]]).*");
                    matcher = pattern.matcher(line);

                    while (matcher.find()) {
                        categories.add(matcher.group(1));
                    }
                    line = bufReader.readLine();
                }
                if (line.matches(".*\\[\\[Kategória:([ÁA-Za-zÇ-ž0-9\\s,.()– -]*)([|\\]]).*")) {
                    pattern = Pattern.compile("\\[\\[Kategória:([ÁA-Za-zÇ-ž0-9\\s,.()– -]*)([|\\]]).*");
                    matcher = pattern.matcher(line);

                    while (matcher.find()) {
                        categories.add(matcher.group(1).replaceAll(" ", " "));
                    }
                    if (categories.size() < 3) maxDepth = 4;
                    else maxDepth = 2;
                }


                // DFS
                if (searchMethod == "DFS") {
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
                if (searchMethod == "BFS") {
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
>>>>>>> parent of 4340e13... redirect categories extraction && testing recognizing entities in text
                        }
                        if (!searched) output.println(" - unknown");
                    }
                }
            }
            line = bufReader.readLine();
        }
        System.out.println((float)(System.currentTimeMillis() - startTime)/1000 + " sec");

    }


    public static Pattern pattern;
    public static Matcher matcher;
    public static String selectedCategory;
    public static String selectedSubCategory;
    public static ArrayList<String> queue;


    private static boolean search_categories_DFS(String category, int depth) throws IOException {

        FileInputStream fileCategories;
        BufferedReader reader;
        fileCategories = new FileInputStream("files/categories.txt");
        reader = new BufferedReader(new InputStreamReader(fileCategories));


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
                        output.println(title + " [" + selectedCategory + selectedSubCategory + "]");
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
        boolean found = false;

<<<<<<< HEAD
        if (depth == 10) {
            return false;
        }
=======
//        if (depth == 5) {
//            return false;
//        }
>>>>>>> parent of 4340e13... redirect categories extraction && testing recognizing entities in text

        String line = reader.readLine();
        while (line != null) {
            if (line.equals("NAME: " + category.replaceAll(" ", " "))) {
                line = reader.readLine();
                queue = new ArrayList<>();
                while (!line.matches(".*NAME:.*") && !found) {
                    found = check_category(line);
                    if (found) {
                        output.println(title + " [" + selectedCategory + selectedSubCategory + "]");
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
