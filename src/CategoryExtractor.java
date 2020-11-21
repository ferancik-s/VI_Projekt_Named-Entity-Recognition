import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoryExtractor {

    public static void main(String[] args) throws IOException {
        FileInputStream file;
        BufferedReader bufReader;
        Pattern pattern;
        Matcher matcher;
        PrintStream categories;

        file = new FileInputStream("files/skwiki.xml");
        bufReader = new BufferedReader(new InputStreamReader(file));
        categories = new PrintStream(new FileOutputStream("files/categories.txt"));

        long start = System.currentTimeMillis();

        String line = bufReader.readLine();
        //reads file line by line until end
        while (!line.matches(".*</mediawiki>.*")) {
            if (line.matches(".*<title>.*Kategória:.*</title>.*")) {
                pattern = Pattern.compile(".*<title>Kategória:(.*)</title>.*");
                matcher = pattern.matcher(line);
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
