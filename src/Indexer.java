
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
import java.util.ArrayList;
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
            long start = System.currentTimeMillis();

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
                    ArrayList<String> subcategoriesArray = new ArrayList<>();
                    line = reader.readLine();
                    while (line != null && !line.matches("NAME: .*")){
                        subcategoriesArray.add(line);
                        line = reader.readLine();
                    }
                    doc.add(new TextField("mainCategory", mainCategory.toLowerCase(), Field.Store.YES));
                    doc.add(new StoredField("categories", serialize(subcategoriesArray)));
                    writer.addDocument(doc);
                }
            }
            System.out.println("Categories indexed in: " + (float)(System.currentTimeMillis() - start)/1000 + " sec");

            writer.close();
            file.close();
            reader.close();

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    public static ArrayList<String> searchCategoriesTree(String word) {
        String index = "index/categories";
        ArrayList<String> categories = null;

        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("mainCategory", analyzer);

            Query query = parser.parse(word);

            TopDocs results = searcher.search(query, 1);
            ScoreDoc[] hits = results.scoreDocs;

            for (ScoreDoc scoreDoc : hits) {
                Document doc = searcher.doc(scoreDoc.doc);
                categories = (ArrayList<String>) unserialize(doc.getBinaryValue("categories").bytes);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
        return categories;
    }

    public static void indexEntityDictionary(String inputFile, String indexPath) throws IOException {
        FileInputStream file = new FileInputStream(inputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        try {
            long start = System.currentTimeMillis();

            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, iwc);

            String line = reader.readLine();
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
                doc.add(new TextField("entity", entity.toLowerCase(), Field.Store.YES));
                doc.add(new TextField("category", category, Field.Store.YES));
                writer.addDocument(doc);
                line = reader.readLine();
            }
            System.out.println("Entities indexed in: " + (float)(System.currentTimeMillis() - start)/1000 + " sec");

            writer.close();
            file.close();
            reader.close();
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    public static ArrayList<String> searchEntityDictionary(String word, String indexPath, int matches) {
        ArrayList<String> categories = new ArrayList<>();
        ArrayList<String> oneCategory = new ArrayList<>();
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();

            QueryParser parser = new QueryParser("entity", analyzer);
            Query query = parser.parse(word);

            TopDocs results = searcher.search(query, matches);
            ScoreDoc[] hits = results.scoreDocs;

            for (ScoreDoc scoreDoc : hits) {
                Document doc = searcher.doc(scoreDoc.doc);
                categories.add(doc.get("category"));
            }
            for (String category: categories) {
                if (category.equals(word)) {
                    oneCategory.add(category);
                    return oneCategory;
                }
            }
            reader.close();
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
        return categories;
    }

    public static void indexEntityNominative(String inputFile, String indexPath) throws IOException{
        FileInputStream file = new FileInputStream(inputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        try {
            long start = System.currentTimeMillis();

            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, iwc);

            String line = reader.readLine();
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
                String completeEntity = "";
                String[] split = entity.split(" ");
                for (String s : split) {
                    if (!s.contains("(") && (!s.contains(")"))) {
                        s = s.replaceAll("\\\\", "\\\\\\");
                        s = s.replaceAll("!", "\\\\!");
                        s = s.replaceAll(":", "\\\\:");
                        s = s.replaceAll(" - ", "\\\\-");
                        s = s.replaceAll("\\+", "\\\\+");
                        s = s.replaceAll("\\.", "");
                        if (s.matches("[^-]*-[^-]*")) {
                            String[] string = s.split("-");
                            for (String str : string) {
                                String result = Lemmatizer.searchLemma("\"" + str + "\"")[0];
                                completeEntity += ((!result.isEmpty()) ? result : str.toLowerCase()) + " ";
                            }
                        } else {
                            String result = Lemmatizer.searchLemma("\"" + s + "\"")[0];
                            completeEntity += ((!result.isEmpty()) ? result : s.toLowerCase()) + " ";
                        }
                    }
                }
                doc.add(new TextField("entityNominative", completeEntity, Field.Store.YES));
                doc.add(new TextField("entity", entity, Field.Store.YES));
                doc.add(new TextField("category", category, Field.Store.YES));
                writer.addDocument(doc);

                line = reader.readLine();
            }
            System.out.println("Entities indexed in: " + (float)(System.currentTimeMillis() - start)/1000 + " sec");

            writer.close();
            file.close();
            reader.close();
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    public static ArrayList<ArrayList<String>> searchEntityNominative(String word, String indexPath, int matches) {
        ArrayList<ArrayList<String>> entities = new ArrayList<>();

        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();

            QueryParser parser = new QueryParser("entityNominative", analyzer);
            Query query = parser.parse(word);

            TopDocs results = searcher.search(query, matches);
            ScoreDoc[] hits = results.scoreDocs;

            for (ScoreDoc scoreDoc : hits) {
                ArrayList<String> entity = new ArrayList<>();
                Document doc = searcher.doc(scoreDoc.doc);

                entity.add(doc.get("entity"));
                entity.add(doc.get("category"));
                entities.add(entity);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
        return entities;
    }


}
