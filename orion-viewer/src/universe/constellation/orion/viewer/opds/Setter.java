package universe.constellation.orion.viewer.opds;

/**
* Created by mike on 9/20/14.
*/
public enum Setter {

    TITLE {
        @Override
        public void set(FeedEntry entry, String text) {
            entry.title = text;
        }
    },
    CONTENT {
        @Override
        public void set(FeedEntry entry, String text) {
            entry.content = text;
        }
    },

    AUTHOR {
        @Override
        public void set(FeedEntry entry, String text) {
            entry.addAuthor(text);
        }
    };

    public void set(FeedEntry entry, String text) {

    }
}
