import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lemmatizer {

    public static void indexLemmatizer() throws IOException {
        FileInputStream file = new FileInputStream("lemmatizer/words.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        // directory where index files are stored
        String indexPath = "index/lemmas";
        try {
            long start = System.currentTimeMillis();

            System.out.println("Creating index in directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(dir, iwc);

            String line = reader.readLine();
            while (line != null) {
                Document doc = new Document();

                Pattern pattern = Pattern.compile("(.*)\\t(.*)\\t([A-Z]*)");
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    String wordBase = matcher.group(1);
                    String searchedWord = matcher.group(2);
                    String wordClass = matcher.group(3);

                    doc.add(new TextField("wordBase", wordBase, Field.Store.YES));
                    doc.add(new TextField("searchedWord", searchedWord, Field.Store.YES));
                    doc.add(new TextField("wordClass", wordClass, Field.Store.YES));
                    writer.addDocument(doc);
                }
                line = reader.readLine();
            }
            System.out.println("Lemmatizer indexed in: " + (float)(System.currentTimeMillis() - start)/1000 + " sec");

            writer.close();
            file.close();
            reader.close();

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    public static String[] searchLemma(String word) {
        String index = "index/lemmas";
        String wordBase = "";
        String wordClass = "";

        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("searchedWord", analyzer);

            Query query = parser.parse(word);

            TopDocs results = searcher.search(query, 2);
            ScoreDoc[] hits = results.scoreDocs;

            for (ScoreDoc scoreDoc : hits) {
                Document doc = searcher.doc(scoreDoc.doc);
                wordBase = doc.get("wordBase");
                wordClass = doc.get("wordClass");
                if (hits.length > 1 ) {
                    if (word.charAt(0) == wordBase.charAt(0)) break;
                }
            }
            reader.close();
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
        return new String[]{wordBase, wordClass};
    }

}
