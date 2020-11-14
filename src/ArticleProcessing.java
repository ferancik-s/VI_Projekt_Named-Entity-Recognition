import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ArticleProcessing {

    final static String TITLE = ".*<title>(.*)</title>.*";
    final static String PAGE_END = ".*</page>.*";
    final static String TEXT = ".*<text.*>.*";
    final static String TEXTEND = ".*</text.*>.*";

    final static String typeOfText = "regular";

    public static String title;


    public static void main(String[] args) throws IOException {

        Pattern pattern;
        Matcher matcher;

        String plainText;

        FileInputStream file = new FileInputStream("files/text.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));


//        Dictionary categoriesDic = new Hashtable();
//
//        String riadok = reader.readLine();
//        int counter = 1;
//        while (riadok != null) {
//            if (riadok.matches("NAME: .*")) {
//                categoriesDic.put(riadok, counter);
//            }
//            riadok = reader.readLine();
//            counter++;
//        }



//        int n = 210000; // The line number
//        String riadok;
//        try (Stream<String> lines = Files.lines(Paths.get("files/categories.txt"))) {
//            riadok = lines.skip(n).findFirst().get();
//            System.out.println(riadok);
//        }
//        catch(IOException e){
//            System.out.println(e);
//        }
//
//        int count = 0;
//        while (count != 210000) {
//            reader.readLine();
//            count++;
//        }
//        System.out.println(reader.readLine());

        if (typeOfText == "wikipedia") {
            plainText = extract_text_from_wiki(file);
        }
        if (typeOfText == "regular") {
            plainText = extract_text(file);
        }

        System.out.println(plainText);
    }

    public static String extract_text_from_wiki(FileInputStream file) throws IOException {
        Pattern pattern;
        Matcher matcher;
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        String text = "";

        String line = reader.readLine();
        while (line != null) {
            while (!line.matches(TITLE)) {
                line = reader.readLine();
            }
            if (line.matches(".*<title>.*(MediaWiki:|Wikipédia:|Kategória:|Pomoc:|Šablóna:|Portál|Main Page|Hlavná stránka).*</title>.*")) {
                while (!line.matches(PAGE_END)) {
                    line = reader.readLine();
                }
                continue;
            }
            pattern = Pattern.compile(TITLE);
            matcher = pattern.matcher(line);

            title = ((matcher.find() ? matcher.group(1) : "Unknown Title"));
            while (!line.matches(TEXT)) {
                line = reader.readLine();
            }

            while (!line.matches(".*'''.*'''.*")) {
                line = reader.readLine();
            }
            while (!line.equals("")) {
                text += line;
                line = reader.readLine();
            }
            text = text.replaceAll("([{]{1,2}[^{}]*[}]{1,2})", "");
            text = text.replaceAll("[\\[]{2}([ÁA-Za-zÇ-ž0-9\\s(),.:!?#]*)[|]+", "");
            text = text.replaceAll("]]", "");
            text = text.replaceAll("\\[\\[", "");
            text = text.replaceAll("(&lt;ref(\\s*name\\s*=\\s*&quot;[^&]*&quot;\\s*)?&gt;[^&]*&lt;/ref&gt;)", " ");
            text = text.replaceAll("'''", "");
            text = text.replaceAll("(&quot;)", "\"");
            text = text.replaceAll("(&lt;)", "<");
            text = text.replaceAll("(&gt;)", ">");

            text = "";
            while (!line.matches(".*</page>.*")) {
                line = reader.readLine();
            }
        }
        return text;
    }

    public static String extract_text(FileInputStream file) throws IOException {
        Pattern pattern;
        Matcher matcher;
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        String text = "";

        String line = reader.readLine();
        while (line != null) {
            text += line;
            line = reader.readLine();
        }

        return text;
    }
}
