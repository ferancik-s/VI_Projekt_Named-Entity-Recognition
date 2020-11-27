import java.io.IOException;

public class Initialization {


    public static void main(String[] args) throws IOException {

        String searchMethod = "BFS";

        // Part I - category extraction and classification
        CategoryExtractor.extractCategories();
        Indexer.indexCategoriesTree();
        ParserCategories.extractEntities(searchMethod);

        // Part II - redirect matching and dictionary filtration
        Indexer.indexEntityDictionary("output/output" + searchMethod +".txt", "index/dictionary_with_redirects");
        SortEntities.splitDictionary("output/output" + searchMethod + ".txt", "sorted/noredirects.txt", "sorted/redirects.txt");
        SortEntities.findRedirectCategories("sorted/redirects.txt", "sorted/redirectsMatched.txt");
        SortEntities.mergeCompleteDictionaries("sorted/noredirects.txt", "sorted/redirectsMatched.txt", "files/namedEntityDictionary.txt");
        SortEntities.filterDictionary("files/namedEntityDictionary.txt", "files/filteredDictionary.txt");

        // Part III - initializing lemmatizer
        Lemmatizer.indexLemmatizer();

        // Part IV - base word entities dictionary creation
        Indexer.indexEntityNominative("files/filteredDictionary.txt", "index/dictionary_nominatives");



    }

}
