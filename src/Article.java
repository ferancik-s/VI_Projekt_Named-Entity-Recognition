import java.util.ArrayList;

public class Article {
    String title;
    String mainCategory;
    ArrayList<String> infoboxCategories;
    ArrayList<String> abstractCategories;
    ArrayList<String> articleCategories;

    public Article() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public ArrayList<String> getInfoboxCategories() {
        return infoboxCategories;
    }

    public void setInfoboxCategories(ArrayList<String> infoboxCategories) {
        this.infoboxCategories = infoboxCategories;
    }

    public ArrayList<String> getAbstractCategories() {
        return abstractCategories;
    }

    public void setAbstractCategories(ArrayList<String> abstractCategories) {
        this.abstractCategories = abstractCategories;
    }

    public ArrayList<String> getArticleCategories() {
        return articleCategories;
    }

    public void setArticleCategories(ArrayList<String> articleCategories) {
        this.articleCategories = articleCategories;
    }
}


