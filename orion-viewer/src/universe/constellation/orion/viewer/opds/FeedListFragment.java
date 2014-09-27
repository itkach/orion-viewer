package universe.constellation.orion.viewer.opds;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.internal.view.SupportMenuItem;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import universe.constellation.orion.viewer.OrionBaseActivity;
import universe.constellation.orion.viewer.R;
import universe.constellation.orion.viewer.opds.net.DownloadUtil;

/**
 * Created by mike on 9/20/14.
 */
public class FeedListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<FeedData> {

    // This is the Adapter being used to display the formats's feedData.
    FeedListAdapter mAdapter;

    // If non-null, this is the current filter the user has provided.
    private FeedData feedData;

    private static List<String> formats;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No feed data");
        setHasOptionsMenu(true);
        // Create an empty adapter we will use to display the loaded feedData.
        mAdapter = new FeedListAdapter(getActivity());
        setListAdapter(mAdapter);
        // Start out with a progress indicator.
        setListShown(false);
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        updateViewData(getRootFeed(), true);
        formats = getSupportedTypes();
    }

    private boolean upCatalog() {
        FeedData oldData = feedData;
        if (oldData != null && oldData.prevCatalog != null) {
            updateViewData(oldData.prevCatalog, false);
            oldData.entries.clear();
            return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        SupportMenuItem item = (SupportMenuItem) menu.add("Back");
        item.setIcon(R.drawable.prev);
        item.setShowAsAction(SupportMenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return upCatalog();
            }
        });

        SupportMenuItem item2 = (SupportMenuItem) menu.add("New entry...");
        item2.setIcon(R.drawable.plus);
        item2.setShowAsAction(SupportMenuItem.SHOW_AS_ACTION_IF_ROOM);
        item2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new NewOPDSDialog().show(getFragmentManager(), "new_opds");

                return true;

            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Insert desired behavior here.
        Log.i("LoaderCustom", "Item clicked: " + id);
        FeedEntry feedEntry = (FeedEntry) l.getItemAtPosition(position);
        if (feedEntry.isCatalog()) {
            String url = DownloadUtil.getAbsoluteUrl(feedEntry.getAtomLink(), feedData.catalogUrl);
            getLoaderManager().restartLoader(0, getBundle(url), this);
        } else {
            BookFragment newFragment = BookFragment.newInstance(feedEntry);
            newFragment.show(getFragmentManager(), "dialog");
        }
    }

    private static Bundle getBundle(String url) {
        Bundle b = new Bundle();
        b.putString("catalogUrl", url);
        return b;
    }

    @Override
    public Loader<FeedData> onCreateLoader(int id, Bundle args) {
        return new FeedLoader((OrionBaseActivity) getActivity(), args.getString("catalogUrl"));
    }

    @Override
    public void onLoadFinished(Loader<FeedData> loader, FeedData data) {
        updateViewData(data, true);
    }

    private void updateViewData(FeedData data, boolean isForward) {
        mAdapter.setData(data.entries);

        if (isForward) {
            data.prevCatalog = this.feedData;
        }

        this.feedData = data;

        // The formats should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<FeedData> loader) {
        // Clear the feedData in the adapter.
        mAdapter.setData(null);
    }

    public FeedData getRootFeed() {
        FeedData root = new FeedData("ROOT");

        SharedPreferences opds = getActivity().getSharedPreferences("opds", Context.MODE_PRIVATE);
        Map<String, String> all = (Map<String, String>) opds.getAll();
        for (Map.Entry<String, String> next : all.entrySet()) {
            root.entries.add(new FeedEntry(next.getValue(), next.getKey()));
        }
        root.entries.addAll(getDefaultCatalogs());
        return root;
    }

    public static List<FeedEntry> getDefaultCatalogs() {
        ArrayList<FeedEntry> entries = new ArrayList<FeedEntry>(10);
        entries.add(new FeedEntry("Project Gutenberg", "http://m.gutenberg.org/ebooks/?format=opds"));
        entries.add(new FeedEntry("Internet Archive", "http://bookserver.archive.org/catalog/"));
        entries.add(new FeedEntry("arXiv", "arXiv mirror on heroku.com", "http://arXiv-opds.heroku.com/catalog.atom"));
        entries.add(new FeedEntry("Feedbooks", "http://www.feedbooks.com/catalog.atom"));
        entries.add(new FeedEntry("Revues.org", "http://bookserver.revues.org/"));
        entries.add(new FeedEntry("Ebooks Libres et Gratuits", "Books in French", "http://www.ebooksgratuits.com/opds/index.php"));
        return entries;
    }

    public static List<String> getSupportedTypes() {
        if (formats == null) {
            formats = new ArrayList<String>();
            formats.add("image/vnd.djvu");
            formats.add("image/x-djvu");
            formats.add("application/djvu");
            formats.add("application/vnd.djvu");
            formats.add("application/pdf");
            formats.add("application/vnd.ms-xpsdocument");
            formats.add("application/oxps");
            formats.add("application/x-cbz");
        }
        return formats;
    }
}
