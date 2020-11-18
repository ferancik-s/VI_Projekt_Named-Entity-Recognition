import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SortEntities {

    public static void main(String[] args) throws IOException {

        FileInputStream file = new FileInputStream("output/outputBFS.txt");
        PrintStream noredirects = new PrintStream("sorted/noredirects.txt");
        PrintStream redirects = new PrintStream("sorted/redirects.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        // First splits all entities into named and redirects and labels all disambiguations as unknown
        String line = reader.readLine();
        long start = System.currentTimeMillis();

        long startTime = System.currentTimeMillis();

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

        noredirects.close();
        redirects.close();
        file.close();
        reader.close();

        System.out.println((float)(System.currentTimeMillis() - start)/1000 + " sec");


        FileInputStream redirectsInput = new FileInputStream("sorted/redirects.txt");
        FileInputStream noredirectsInput = new FileInputStream("sorted/noredirects.txt");
        PrintStream redirectsMatched = new PrintStream("sorted/redirectsMatched.txt");
        BufferedReader redirectReader = new BufferedReader(new InputStreamReader(redirectsInput));
        BufferedReader noredirectReader = new BufferedReader(new InputStreamReader(noredirectsInput));

        Pattern pattern;
        Matcher matcher;

        String wordRedirectTo = "";
        String wordRedirectFrom = "";
        String category = "";

        String lineInRedirect = redirectReader.readLine();
        while (lineInRedirect != null) {
            pattern = Pattern.compile("(.*)[\\s]+\\[redirect][\\s]*\\{(.*)\\}.*");
            matcher = pattern.matcher(lineInRedirect);
            while (matcher.find()) {
                wordRedirectFrom = matcher.group(1);
                wordRedirectTo = matcher.group(2);
            }
            wordRedirectTo = wordRedirectTo.replaceAll("(\\()", "\\\\(");
            wordRedirectTo = wordRedirectTo.replaceAll("(\\))", "\\\\)");
            wordRedirectTo = wordRedirectTo.replaceAll("&#039;", "'");
            wordRedirectTo = wordRedirectTo.replaceAll("(\\+)", "\\\\+");



            String lineInNoRedirect = noredirectReader.readLine();
            while (lineInNoRedirect != null) {
                if (lineInNoRedirect.matches(wordRedirectTo + " \\[.*")) {
                    break;
                }
                lineInNoRedirect = noredirectReader.readLine();
            }
            if (lineInNoRedirect == null) category = "unknown";
            else {
                pattern = Pattern.compile(".*\\[(.*)\\]");
                matcher = pattern.matcher(lineInNoRedirect);
                while (matcher.find()) {
                    category = matcher.group(1);
                }
            }
            redirectsMatched.println(wordRedirectFrom + " [" + category + "]");


            lineInRedirect = redirectReader.readLine();

            noredirectsInput.getChannel().position(0);
            noredirectReader = new BufferedReader(new InputStreamReader(noredirectsInput));
        }
        redirectsInput.close();
        noredirectsInput.close();
        redirectsMatched.close();
        redirectReader.close();
        noredirectReader.close();

        System.out.println((float)(System.currentTimeMillis() - startTime)/1000 + " sec");

    }
}
