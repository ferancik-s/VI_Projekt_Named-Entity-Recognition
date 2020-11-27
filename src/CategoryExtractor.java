import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoryExtractor {

    public static void extractCategories() throws IOException {
        FileInputStream file = new FileInputStream("files/skwiki.xml");
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(file));
        PrintStream categories = new PrintStream(new FileOutputStream("files/categories.txt"));

        long start = System.currentTimeMillis();
        System.out.println("Extracting categories...");

        String line = bufReader.readLine();
        //reads file line by line until end
        while (!line.matches(".*</mediawiki>.*")) {
            if (line.matches(".*<title>.*Kategória:.*</title>.*")) {
                Pattern pattern = Pattern.compile(".*<title>Kategória:(.*)</title>.*");
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    categories.println("NAME: " + matcher.group(1).replaceAll(" ", " "));
                }
                while (!line.matches(".*<text.*")) {
                    line = bufReader.readLine();
                }
                while (!line.matches(".*</text.*>.*")) {
                        pattern = Pattern.compile("\\[\\[Kategória:[\\s]*([A-Za-zÇ-ž0-9\\s.,()– -]*)([|\\]]).*");
                        matcher = pattern.matcher(line);

                        while (matcher.find()) {
                            categories.println(matcher.group(1).replaceAll(" ", " "));
                        }
                        line = bufReader.readLine();
                }
                pattern = Pattern.compile("\\[\\[Kategória:[\\s]*([A-Za-zÇ-ž0-9\\s.,()– -]*)([|\\]]).*");
                matcher = pattern.matcher(line);

                while (matcher.find()) {
                    categories.println(matcher.group(1).replaceAll(" ", " "));
                }

            }
            line = bufReader.readLine();
        }
        System.out.println("Categories extracted in: " + (float)(System.currentTimeMillis() - start)/1000 + " sec");

    }

}
