import java.util.ArrayList;

public class Article {
    String title;
    String mainCategory;
    String categoryFromInfobox;
    String categoryFromAbstract;
    String categoryFromArticle;

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

    public String getCategoryFromInfobox() {
        return categoryFromInfobox;
    }

    public void setCategoryFromInfobox(String categoryFromInfobox) {
        this.categoryFromInfobox = categoryFromInfobox;
    }

    public String getCategoryFromAbstract() {
        return categoryFromAbstract;
    }

    public void setCategoryFromAbstract(String categoryFromAbstract) {
        this.categoryFromAbstract = categoryFromAbstract;
    }

    public String getCategoryFromArticle() {
        return categoryFromArticle;
    }

    public void setCategoryFromArticle(String categoryFromArticle) {
        this.categoryFromArticle = categoryFromArticle;
    }
}


