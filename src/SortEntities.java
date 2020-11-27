import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SortEntities {

    public static void splitDictionary(String input, String output1, String output2) throws IOException {

        FileInputStream inputFile = new FileInputStream(input);
        PrintStream noredirects = new PrintStream(output1);
        PrintStream redirects = new PrintStream(output2);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));

        // First splits all entities into named and redirects and labels all disambiguations as unknown
        String line = reader.readLine();


        long start = System.currentTimeMillis();
        // if is not disambiguation or redirect or MetaPages writes to noredirects
        // if is redirect writes to redirects
        while (line != null) {
            if (!line.matches(".*\\[disambiguation\\].*") && !line.matches(".*\\[redirect\\].*") && !line.matches(".*(Wikipédia:|MediaWiki:|Portál:|Šablóna:|Pomoc:|Kategória:|WP:|Súbor:|Špeciálne:|Hlavná stránka|Main page).*.*")){
                noredirects.println(line);
            }
            if (line.matches(".*\\[redirect\\].*") && !line.matches(".*(Wikipédia:|MediaWiki:|Portál:|Šablóna:|Pomoc:|Kategória:|WP:|Súbor:|Špeciálne:|Hlavná stránka|Main page).*.*")) {
                redirects.println(line);
            }
            line = reader.readLine();
        }

        System.out.println("File split into redirects and noredirects in: " + (float)(System.currentTimeMillis() - start)/1000 + " sec");

        noredirects.close();
        redirects.close();
        inputFile.close();
        reader.close();
    }

    public static void findRedirectCategories(String input, String output) throws IOException {
        FileInputStream inputFile = new FileInputStream(input);
        PrintStream outputFile = new PrintStream(output);
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputFile));

        String wordRedirectTo = "";
        String wordRedirectFrom = "";

        long startTime = System.currentTimeMillis();

        String lineInRedirect = inputReader.readLine();
        while (lineInRedirect != null) {
            Pattern pattern = Pattern.compile("(.*)[\\s]+\\[redirect][\\s]*\\{(.*)\\}.*");
            Matcher matcher = pattern.matcher(lineInRedirect);
            while (matcher.find()) {
                wordRedirectFrom = matcher.group(1);
                wordRedirectTo = matcher.group(2);
            }
            wordRedirectTo = "\"" + wordRedirectTo.replaceAll("[ \\s]+", " ") + "\"";

            String category = null;
            if (!Indexer.searchEntityDictionary(wordRedirectTo, "index/dictionary_with_redirects", 3).isEmpty()) {
                category = Indexer.searchEntityDictionary(wordRedirectTo, "index/dictionary_with_redirects", 3).get(0);
            }

            outputFile.println(wordRedirectFrom + " [" + category + "]");

            lineInRedirect = inputReader.readLine();
        }

        System.out.println("Categories assigned to redirects in: " + (float)(System.currentTimeMillis() - startTime)/1000 + " sec");

        inputReader.close();
        inputFile.close();
        outputFile.close();

    }

    public static void mergeCompleteDictionaries(String input1, String input2, String output) throws IOException {

        FileInputStream inputFile1 = new FileInputStream(input1);
        FileInputStream inputFile2 = new FileInputStream(input2);
        PrintStream outputFile = new PrintStream(output);
        BufferedReader readerInput1 = new BufferedReader(new InputStreamReader(inputFile1));
        BufferedReader readerInput2 = new BufferedReader(new InputStreamReader(inputFile2));

        long startTime = System.currentTimeMillis();
        String line = readerInput1.readLine();
        while (line != null) {
            outputFile.println(line);
            line = readerInput1.readLine();
        }
        line = readerInput2.readLine();
        while (line != null) {
            // if did not catch and assign all redirects remove leftover
            if (!line.matches(".*\\[redirect].*"))
            outputFile.println(line);
            line = readerInput2.readLine();
        }
        System.out.println("Files merged in: " + (float)(System.currentTimeMillis() - startTime)/1000 + " sec");

        inputFile1.close();
        inputFile2.close();
        outputFile.close();
        readerInput1.close();
        readerInput2.close();

    }

    public static void filterDictionary(String input, String output) throws IOException{
        FileInputStream inputFile = new FileInputStream(input);
        PrintStream outputFile = new PrintStream(output);
        BufferedReader readerInput = new BufferedReader(new InputStreamReader(inputFile));

        long startTime = System.currentTimeMillis();
        String line = readerInput.readLine();
        while (line != null) {
            String[] split = line.split(" ");
            int numberOfWords = 0;
            for (String value : split) {
                if (!value.contains("[") && !value.contains("]")){
                    numberOfWords++;
                }
            }

            if (!line.matches(".*\\[(unknown|disambiguation|non-entity|null|time \\(.*\\))].*") && numberOfWords < 5) {
                outputFile.println(line);
            }
            line = readerInput.readLine();
        }

        System.out.println("Dictionary filtered in: " + (float)(System.currentTimeMillis() - startTime)/1000 + " sec");

    }

}
