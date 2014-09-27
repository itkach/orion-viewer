package universe.constellation.orion.viewer.opds;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class holds the per-item data in our Loader.
 */
public class FeedEntry implements Serializable {

    public String title;

    public String content;

    public String atomLink;

    public String baseUrl;

    private Map<String, String> anotherLinks;

    private List<String> authors;

    public FeedEntry(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public FeedEntry(String title, String url) {
        this(title, title, url);
    }

    public FeedEntry(String title, String description, String url) {
        this.title = title;
        this.atomLink = url;
        this.content = description;
    }


    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAtomLink() {
        return atomLink;
    }

    public Drawable getIcon() {
        return null;
    }

    public void addAnotherLink(String link, String type) {
        if (anotherLinks == null) {
            anotherLinks = new HashMap<String, String>();
        }
        anotherLinks.put(type, link);
    }

    public void addAuthor(String author) {
        if (authors == null) {
            authors = new ArrayList<String>();
        }
        authors.add(author);
    }

    public Map<String, String> getAnotherLinks() {
        return anotherLinks == null ? Collections.<String, String>emptyMap() : anotherLinks;
    }

    public List<String> getAuthors() {
        return authors == null ? Collections.<String>emptyList() : authors;
    }

    public boolean isCatalog() {
        return getAtomLink() != null;
    }

    public boolean canDownload() {
        List<String> supportedTypes = FeedListFragment.getSupportedTypes();
        Map<String, String> anotherLinks1 = getAnotherLinks();
        for (String supportedType : supportedTypes) {
            if (anotherLinks1.containsKey(supportedType)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, String> getDownloadList() {
        Map<String, String> results = new HashMap<String, String>();
        List<String> supportedTypes = FeedListFragment.getSupportedTypes();
        Map<String, String> anotherLinks1 = getAnotherLinks();
        for (String supportedType : supportedTypes) {
            if (anotherLinks1.containsKey(supportedType)) {
                results.put(supportedType, anotherLinks1.get(supportedType));
            }
        }
        return results;
    }
}
