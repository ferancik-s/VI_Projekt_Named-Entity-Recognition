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
        System.setOut(categories);


        String line = bufReader.readLine();
        //reads file line by line until end
        while (!line.matches(".*</mediawiki>.*")) {
            if (line.matches(".*<title>.*Kategória:.*</title>.*")) {
                pattern = Pattern.compile(".*<title>Kategória:(.*)</title>.*");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    categories.println("NAME: " + matcher.group(1));
                }
                while (!line.matches(".*\\[\\[Kategória:([ÁA-Za-zÇ-ž0-9\\s.,-]*)([|\\]]).*") && !line.matches(".*</text.*>.*")) {
                    line = bufReader.readLine();
                }
                while (!line.matches(".*</text.*>.*")) {
                        pattern = Pattern.compile("\\[\\[Kategória:([A-Za-zÇ-ž0-9\\s.,-]*)([|\\]]).*");
                        matcher = pattern.matcher(line);

                        while (matcher.find()) {
                            categories.println(matcher.group(1));
                        }
                        line = bufReader.readLine();
                }
                if (line.matches(".*\\[\\[Kategória:([A-Za-zÇ-ž0-9\\s.,-]*)([|\\]]).*")) {
                    pattern = Pattern.compile("\\[\\[Kategória:([A-Za-zÇ-ž0-9\\s.,-]*)([|\\]]).*");
                    matcher = pattern.matcher(line);

                    while (matcher.find()) {
                        categories.println(matcher.group(1));
                    }
                }
            }
            line = bufReader.readLine();
        }
    }

}
