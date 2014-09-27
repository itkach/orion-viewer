package universe.constellation.orion.viewer.opds;

import android.support.v4.content.AsyncTaskLoader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import universe.constellation.orion.viewer.Common;
import universe.constellation.orion.viewer.OrionBaseActivity;
import universe.constellation.orion.viewer.opds.net.DownloadUtil;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class FeedLoader extends AsyncTaskLoader<FeedData> {

    FeedData mApps;
    private OrionBaseActivity context;
    private String url;

    public FeedLoader(OrionBaseActivity context, String url) {
        super(context);
        this.context = context;
        this.url = url;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public FeedData loadInBackground() {
        InputStream download;
        FeedData feedData = new FeedData(url);
        List<FeedEntry> result = feedData.entries;
        try {
            download = DownloadUtil.download(url);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(download));
            FeedEntry feed = null;
            Setter setter = null;
            int eventType = xpp.getEventType();
            ArrayList<String> tags = new ArrayList<String>(20);
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                    System.out.println("Start document");
                } else {
                    if(eventType == XmlPullParser.START_TAG) {
                        String xppName = xpp.getName();
                        tags.add(xppName);
                        if ("entry".equalsIgnoreCase(xppName)) {
                            feed = new FeedEntry(url);
                        } else if ("link".equalsIgnoreCase(xppName)) {
                            if (feed != null) {
                                String type = xpp.getAttributeValue(null, "type");
                                String href = xpp.getAttributeValue(null, "href");
                                String rel = xpp.getAttributeValue(null, "rel");
                                if (contains(type, "application/atom+xml")) {
                                    if (!"related".equals(rel) && !"alternate".equals(rel)) {
                                        feed.atomLink = href;
                                    } else {
                                        //skip
                                    }
                                } else {
                                    feed.addAnotherLink(href, type);
                                }
                            }
                        } else {
                            setter = null;
                        }
                    } else if(eventType == XmlPullParser.END_TAG) {
                        tags.remove(tags.size() - 1);
                        String xppName = xpp.getName();
                        if ("entry".equalsIgnoreCase(xppName)) {
                            result.add(feed);
                            feed = null;
                        }
                        setter = null;
                    } else if(eventType == XmlPullParser.TEXT) {
                        if (feed != null) {
                            String tag = tags.isEmpty() ? "" : tags.get(tags.size() - 1);
                            if ("title".equalsIgnoreCase(tag)) {
                                setter = Setter.TITLE;
                            } else if ("content".equalsIgnoreCase(tag) || "summary".equalsIgnoreCase(tag)) {
                                setter = Setter.CONTENT;
                            } else if ("name".equalsIgnoreCase(tag)) {
                                if("author".equals(getPrevLast(tags))) {
                                    setter = Setter.AUTHOR;
                                }
                            }
                            if (setter != null) {
                                setter.set(feed, xpp.getText());
                                setter = null;
                            }
                        }
                    }
                }
                eventType = xpp.next();
            }
            System.out.println("End document");
        } catch (Exception e) {
            Common.d(e);
            OrionBaseActivity.showAlert(e.getMessage(), e.getMessage(), context);
        } finally {
            Common.d("Feed entries size " + result.size());
            return feedData;
        }
    }

    private boolean contains(String in, String what) {
        if (in == null) {
            return false;
        }
        return in.contains(what);
    }

    private String getPrevLast(List<String> tags) {
        if (tags.size() < 2) {
            return "";
        }
        return tags.get(tags.size() - 2);
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(FeedData apps) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (apps != null) {
                onReleaseResources(apps);
            }
        }
        FeedData oldApps = apps;
        mApps = apps;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its entries.
            super.deliverResult(apps);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldApps != null) {
            onReleaseResources(oldApps);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (mApps != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mApps);
        }

        // Has something interesting in the configuration changed since we
        // last built the app list?
        if (takeContentChanged() || mApps == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(FeedData apps) {
        super.onCanceled(apps);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(apps);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mApps != null) {
            onReleaseResources(mApps);
            mApps = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(FeedData apps) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
}
