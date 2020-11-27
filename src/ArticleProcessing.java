import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleProcessing {

    final static String TITLE = ".*<title>(.*)</title>.*";
    final static String PAGE_END = ".*</page>.*";
    final static String TEXT = ".*<text.*>.*";

    final static String NOUN = "(S[SFU]|Q)";
    final static String ADJECTIVE = "(A[AFU]|S[A])";

    final static String typeOfText = "regular";

    public static String title;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";




    public static void main(String[] args) throws IOException {

        FileInputStream file = new FileInputStream("files/text.txt");

        String plainText;
        if (typeOfText == "wikipedia") {
            plainText = extract_text_from_wiki(file);
        }
        if (typeOfText == "regular") {
            plainText = extract_text(file);
        }


        ArrayList<String> oneWord = new ArrayList<>();
        ArrayList<String> twoWord = new ArrayList<>();
        ArrayList<String> threeWord = new ArrayList<>();
        ArrayList<ArrayList<String>> oneWordOriginal = new ArrayList<>();
        ArrayList<ArrayList<String>> twoWordOriginal = new ArrayList<>();
        ArrayList<ArrayList<String>> threeWordOriginal = new ArrayList<>();
        ArrayList<String> temp;


        // extracts nouns and adjectives from text split into the words while keeping both original words and lemmatized words
        ArrayList<String> nouns_adjectives = extractNounsAndAdjectives(plainText.replaceAll("\\*", "\\\\*").split("[\\s]"));


        // divides extracted words into 1,2,3 word terms
        for (int i = 0; i < nouns_adjectives.size(); i++) {
            String[] found = Lemmatizer.searchLemma(nouns_adjectives.get(i));
            temp = new ArrayList<>();
            // if word is noun starting with capital letter
            // e.g. "Nemecko"
            if (found[1].matches(NOUN) && Character.isUpperCase(found[0].charAt(0))) {
                oneWord.add(found[0]);
                temp.add(nouns_adjectives.get(i));
            }
            // if word is all capital letters and at least two letters or first letter is capital and word not matches other type than noun or adjective
            // e.g. "OSN"
            else if ((isUpperCase(nouns_adjectives.get(i)) && nouns_adjectives.get(i).length() > 1) || (Character.isUpperCase(nouns_adjectives.get(i).charAt(0)) && !found[1].matches("([PNVGDEQTJRYWZ][PSANFUDIKMHLBY]*)"))) {
                oneWord.add(nouns_adjectives.get(i));
                temp.add(nouns_adjectives.get(i));
            }
            if (temp.size() > 0) oneWordOriginal.add(temp);
            temp = new ArrayList<>();
            if (i + 1 < nouns_adjectives.size()){
                String[] found1 = Lemmatizer.searchLemma(nouns_adjectives.get(i+1));
                // if first word is adjective and second is noun
                // e.g. "Európska únia"
                if (found[1].matches(ADJECTIVE) && found1[1].matches(NOUN)){
                    temp.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1));
                    twoWord.add(found[0] + " " + found1[0]);
                }
                // if first and second word stars with capital letter
                // e.g. "Isaac Newton"
                else if (Character.isUpperCase(nouns_adjectives.get(i).charAt(0)) && Character.isUpperCase(nouns_adjectives.get(i + 1).charAt(0))) {
                    twoWord.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1));
                    temp.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1));
                }
                // if first word starts with capital letter and is unknown type and second word is noun
                // like first case but throws away words that are capital at beginning of sentence and are not noun or adjective
                else if (Character.isUpperCase(nouns_adjectives.get(i).charAt(0)) && !found[1].matches("([PNVGDEQTJRYWZ][PSANFUDIKMHLBY]*)") && found1[1].matches(NOUN)) {
                    twoWord.add(nouns_adjectives.get(i) + " " + found1[0]);
                    temp.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1));
                }
            }
            if (temp.size() > 0) twoWordOriginal.add(temp);
            temp = new ArrayList<>();
            if (i + 2 < nouns_adjectives.size()) {
                String[] found1 = Lemmatizer.searchLemma(nouns_adjectives.get(i + 1));
                String[] found2 = Lemmatizer.searchLemma(nouns_adjectives.get(i + 2));
                // if first word is adjective, second is adjective and third is noun
                // e.g. "druhá svetová vojna"
                if (found[1].matches("(A[AFU]|SA|NA)") && found1[1].matches(ADJECTIVE) && found2[1].matches(NOUN)){
                    temp.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i + 1) + " " + nouns_adjectives.get(i + 2));
                    threeWord.add(found[0] + " " + found1[0] + " " + found2[0]);
                }
                // if first word is number, second is adjective and third is noun (we assume that number has to be followed with "." so it adds to it
                // e.g. "2. svetová vojna"
                if ((nouns_adjectives.get(i).matches("[0-9]*")) && found1[1].matches(ADJECTIVE) && found2[1].matches(NOUN)){
                    temp.add(nouns_adjectives.get(i) + ". " + nouns_adjectives.get(i + 1) + " " + nouns_adjectives.get(i + 2));
                    threeWord.add(nouns_adjectives.get(i) + ". " + found1[0] + " " + found2[0]);
                }
                // if all three words starts with capital letter
                // e.g. "Orson Scott Card"
                else if (Character.isUpperCase(nouns_adjectives.get(i).charAt(0)) && Character.isUpperCase(nouns_adjectives.get(i + 1).charAt(0)) && Character.isUpperCase(nouns_adjectives.get(i + 2).charAt(0))) {
                    threeWord.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1) + " " + nouns_adjectives.get(i+2));
                    temp.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1) + " " + nouns_adjectives.get(i+2));
                }
                // if first word is adjective, second word is noun and third word is noun starting with capital letter
                // e.g. "Spolková republika Nemecko"
                else if (found[1].matches(ADJECTIVE) && found1[1].matches(NOUN) && found2[1].matches(NOUN) && Character.isUpperCase(nouns_adjectives.get(i + 2).charAt(0))) {
                    temp.add(nouns_adjectives.get(i) + " " + nouns_adjectives.get(i+1) + " " + nouns_adjectives.get(i+2));
                    threeWord.add(found[0] + " " + found1[0] + " " + found2[0]);
                }
            }
            if (temp.size() > 0) threeWordOriginal.add(temp);
        }

        // assigns categories for each 1,2,3 word term that was found
        int numberOfMatches = 1;
        oneWordOriginal = assign_categories(oneWord, oneWordOriginal, numberOfMatches);
        twoWordOriginal = assign_categories(twoWord, twoWordOriginal, numberOfMatches);
        threeWordOriginal = assign_categories(threeWord, threeWordOriginal, numberOfMatches);


        // highlights words which represents some category example [{Slovensko}]
        plainText = highlightWords(plainText, threeWordOriginal);
        plainText = highlightWords(plainText, twoWordOriginal);
        plainText = highlightWords(plainText, oneWordOriginal);


        // splits text according to "[" and "]" and prints highlighted text: "{..text..}"
        printHighlightedText(plainText.split("[\\[\\]]"));


        //TODO: Pridať aj subkategórie do vyznačených entít

    }

    public static ArrayList<String> extractNounsAndAdjectives(String[] allWords) {
        ArrayList<String> nouns_adjectives = new ArrayList<>();
        for (String item : allWords) {
            if (!item.matches("[.,?:]")){
                String[] found = Lemmatizer.searchLemma(item.replaceAll("[,.;:?!()\"]", ""));

                if (found[1].matches("([SA][SAFU]|Q)") || Character.isUpperCase(item.charAt(0)) || item.matches("[0-9]*[.]")) {
                    nouns_adjectives.add(item.replaceAll("[,.;:?!()\"]", ""));
                }
            }
        }
        return nouns_adjectives;
    }

    public static boolean isUpperCase(String s) {
        for (int i=0; i<s.length(); i++) {
            if (!Character.isUpperCase(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void printHighlightedText(String[] rozdelene) {
        String sub = "";
        for (String word : rozdelene) {
            if (word.length() > 1) {
                if (word.charAt(0) == '1') {
                    if (word.charAt(1) == '1') sub = "town/city";
                    else if (word.charAt(1) == '2') sub = "state/country";
                    else if (word.charAt(1) == '3') sub = "gpe";
                    else if (word.charAt(1) == '4') sub = "facility";
                    System.out.print(ANSI_GREEN + word.replaceAll(word.charAt(0) + "" + word.charAt(1), "") + " [location - "+ sub +"]" + ANSI_RESET);
                } else if (word.charAt(0) == '2') {
                    if (word.charAt(1) == '1') sub = "";
                    else if (word.charAt(1) == '2') sub = " - corporation";
                    else if (word.charAt(1) == '3') sub = " - band";
                    System.out.print(ANSI_BLUE + word.replaceAll(word.charAt(0) + "" + word.charAt(1), "") + " [organization"+ sub +"]" + ANSI_RESET);
                } else if (word.charAt(0) == '3') {
                    System.out.print(ANSI_RED + word.replaceAll(word.charAt(0) + "" + word.charAt(1), "") + " [person - "+ sub +"]" + ANSI_RESET);
                } else if (word.charAt(0) == '4') {
                    if (word.charAt(1) == '1') sub = "event";
                    else if (word.charAt(1) == '2') sub = "norp";
                    else if (word.charAt(1) == '3') sub = "language";
                    else if (word.charAt(1) == '4') sub = "workOfArt";
                    else if (word.charAt(1) == '5') sub = "product";
                    System.out.print(ANSI_PURPLE + word.replaceAll(word.charAt(0) + "" + word.charAt(1), "") + " [miscellaneous - "+ sub +"]" + ANSI_RESET);
                } else System.out.print(word);
            } else System.out.print(word);
        }
    }

    public static String highlightWords(String text, ArrayList<ArrayList<String>> array) {

        for (ArrayList<String> word : array) {
            Pattern pattern = Pattern.compile("([,.;:?!()\"]?[\\s]+|^)(" + word.get(0) + ")([,.;:?!()\"]?[\\s]+|$)");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String mark = "";
                switch (word.get(1).split(" ")[0]){
                    case "location" -> {
                        if (word.get(1).split(" ").length > 1) {
                            switch (word.get(1).split(" ")[1]) {
                                case "(town/city)" -> mark = "11";
                                case "(state/country)" -> mark = "12";
                                case "(gpe)" -> mark = "13";
                                case "(facility)" -> mark = "14";
                            }
                        }
                    }
                    case "organization" -> {
                        if (word.get(1).split(" ").length > 1) {
                            switch (word.get(1).split(" ")[1]) {
                                case "(corporation)" -> mark = "22";
                                case "(band)" -> mark = "23";
                            }
                        } else mark = "21";
                    }
                    case "person" -> mark = "31";
                    case "miscellaneous" -> {
                        if (word.get(1).split(" ").length > 1) {
                            switch (word.get(1).split(" ")[1]) {
                                case "(event)" -> mark = "41";
                                case "(norp)" -> mark = "42";
                                case "(language)" -> mark = "43";
                                case "(workOfArt)" -> mark = "44";
                                case "(product)" -> mark = "45";
                            }
                        }
                    }
                }
                text = text.replaceAll( matcher.group(1).replaceAll("\\(", "\\\\(") + matcher.group(2) + matcher.group(3).replaceAll("\\)", "\\\\)"),  matcher.group(1) + "[" + mark + matcher.group(2) + mark + "]" + matcher.group(3));
            }
        }
        return text;
    }

    public static ArrayList<ArrayList<String>> assign_categories(ArrayList<String> nWord, ArrayList<ArrayList<String>>  nWordOriginal, int numberOfMatches) {
        for (int i = 0; i < nWord.size(); i++) {
            ArrayList<ArrayList<String>> toBePrinted = Indexer.searchEntityNominative("\"" + nWord.get(i) + "\"", "index/dictionary_nominatives", numberOfMatches);
            for (ArrayList<String> array : toBePrinted) {
                nWordOriginal.get(i).add((array.get(1)));
            }
        }
        for (int i = 0; i < nWordOriginal.size(); i++) {
            if (nWordOriginal.get(i).size() < 2){
                nWordOriginal.remove(i);
                i--;
            }
        }
        return nWordOriginal;
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

            while (!line.matches(".*</page>.*")) {
                line = reader.readLine();
            }
        }
        return text;
    }

    public static String extract_text(FileInputStream file) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        String text = "";

        String line = reader.readLine();
        while (line != null) {
            if (!line.equals("")) {
                text += line + "\n";
            }
            line = reader.readLine();
        }
        return text;
    }
}
