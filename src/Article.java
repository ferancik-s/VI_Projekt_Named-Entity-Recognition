import java.util.ArrayList;

public class Article {
    String title;
    String mainCategory;
    ArrayList<Integer> infoboxCategoriesInteger;
    ArrayList<Integer> abstractCategoriesInteger;
    ArrayList<Integer> articleCategoriesInteger;

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

    public ArrayList<Integer> getInfoboxCategoriesInteger() {
        return infoboxCategoriesInteger;
    }

    public void setInfoboxCategories(ArrayList<Integer> infoboxCategories) {
        this.infoboxCategoriesInteger = infoboxCategories;
    }

    public ArrayList<Integer> getAbstractCategoriesInteger() {
        return abstractCategoriesInteger;
    }

    public void setAbstractCategoriesInteger(ArrayList<Integer> abstractCategoriesInteger) {
        this.abstractCategoriesInteger = abstractCategoriesInteger;
    }

    public ArrayList<Integer> getArticleCategoriesInteger() {
        return articleCategoriesInteger;
    }

    public void setArticleCategoriesInteger(ArrayList<Integer> articleCategoriesInteger) {
        this.articleCategoriesInteger = articleCategoriesInteger;
    }
}


