package universe.constellation.orion.viewer.opds;

import java.util.ArrayList;

/**
* Created by mike on 9/20/14.
*/
public class FeedData {

    public FeedData prevCatalog;

    public final String catalogUrl;

    public final ArrayList<FeedEntry> entries = new ArrayList<FeedEntry>();

    public FeedData(String catalogUrl) {
        this.catalogUrl = catalogUrl;
    }

}
