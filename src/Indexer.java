
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.*;

import java.io.*;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Indexer {


    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(200000);
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(obj);
        os.close();
        return bos.toByteArray();
    }

    public static Object unserialize(byte[] bytes) throws ClassNotFoundException, IOException {
        ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object result = is.readObject();
        is.close();
        return result;
    }

    public static void indexCategoriesTree() throws IOException {
        FileInputStream file = new FileInputStream("files/categories.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        // directory where index files are stored
        String indexPath = "index/categories";
        try {
            System.out.println("Creating index in directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(dir, iwc);

            String mainCategory = "";
            String line = reader.readLine();
            while (line != null) {
                Document doc = new Document();
                if (line.matches("NAME: .*")) {
                    Pattern pattern = Pattern.compile("NAME: (.*)");
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        mainCategory = matcher.group(1);
                    }
                    ArrayList<Object> categoryArray = new ArrayList<>();
                    ArrayList<Object> subcategoriesArray = new ArrayList<>();
                    categoryArray.add(mainCategory);
                    line = reader.readLine();
                    while (line != null && !line.matches("NAME: .*")){
                        subcategoriesArray.add(line);
                        line = reader.readLine();
                    }
                    categoryArray.add(subcategoriesArray);
                    doc.add(new TextField("mainCategory", mainCategory.toLowerCase(), Field.Store.YES));
                    doc.add(new StoredField("categories", serialize(categoryArray)));
                    writer.addDocument(doc);
                }
            }
            writer.close();
            file.close();
            reader.close();

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    public static void searchCategoriesTree(String word) {
        String index = "index/categories";

        int hitsPerPage = 10;
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("mainCategory", analyzer);

            Query query = parser.parse(word);
            System.out.println("Searching for: " + query.toString("mainCategory"));

            TopDocs results = searcher.search(query, hitsPerPage);
            ScoreDoc[] hits = results.scoreDocs;

            for (ScoreDoc scoreDoc : hits) {
                Document doc = searcher.doc(scoreDoc.doc);
                String mainCategory = doc.get("mainCategory");
                ArrayList<Object> categories = (ArrayList<Object>) unserialize(doc.getBinaryValue("categories").bytes);
                System.out.println("mainCategory: " + mainCategory + " category: " + categories);
            }
            System.out.println();

            reader.close();
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    public static void indexEntityDictionary() throws IOException {
        FileInputStream file = new FileInputStream("sorted/noredirects.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        // directory where index files are stored
        String indexPath = "index/dictionary";
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, iwc);

            String line = reader.readLine();
            int counter = 1;
            Pattern pattern;
            Matcher matcher;
            String entity = "";
            String category = "";
            while (line != null) {
                Document doc = new Document();
                pattern = Pattern.compile("(.*)[\\s]+\\[(.*)]");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    entity = matcher.group(1);
                    category = matcher.group(2);
                }
                ArrayList<Object> someList = new ArrayList<>();
                someList.add(entity + " => " + category);
                someList.add(counter);

                doc.add(new TextField("entity", entity.toLowerCase(), Field.Store.YES));
                doc.add(new TextField("category", category, Field.Store.YES));
                doc.add(new StringField("lineNumber", String.valueOf(counter), Field.Store.YES));
                doc.add(new StoredField("list", serialize(someList)));
                writer.addDocument(doc);
                counter++;
                line = reader.readLine();
            }

            writer.close();
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    public static void searchEntityDictionary(String word) {
        String index = "index/dictionary";
        int hitsPerPage = 10;
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();

            QueryParser parser = new QueryParser("entity", analyzer);

            Query query = parser.parse(word);
            System.out.println("Searching for: " + query.toString("entity"));

            TopDocs results = searcher.search(query, hitsPerPage);
            ScoreDoc[] hits = results.scoreDocs;

            for (ScoreDoc scoreDoc : hits) {
                Document doc = searcher.doc(scoreDoc.doc);
                String entity = doc.get("entity");
                String category = doc.get("category");
                String lineNumber = doc.get("lineNumber");
                ArrayList<Object> list = (ArrayList<Object>) unserialize(doc.getBinaryValue("list").bytes);
                System.out.println("lineNumber: " + lineNumber + " entity: " + entity + " category: " + category + " list: " + list);
            }
            System.out.println();

            reader.close();
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        //indexCategoriesTree();
        searchCategoriesTree("Slnečná && sústava");
    }




}
