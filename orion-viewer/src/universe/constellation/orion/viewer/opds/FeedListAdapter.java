package universe.constellation.orion.viewer.opds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
* Created by mike on 9/20/14.
*/
public class FeedListAdapter extends ArrayAdapter<FeedEntry> {

    private FeedData data;

    private final LayoutInflater mInflater;

    public FeedListAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_2);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<FeedEntry> data) {
        clear();
        if (data != null) {
            for (FeedEntry feedEntry : data) {
                add(feedEntry);
            }
        }
    }

    /**
     * Populate new items in the list.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        } else {
            view = convertView;
        }

        FeedEntry item = getItem(position);
        ((TextView) view.findViewById(android.R.id.text1)).setText(item.getTitle());
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        if (item.isCatalog()) {
            text2.setText(item.getContent());
        } else {
            text2.setText(StringUtils.join(item.getAuthors(), ','));
        }

        return view;
    }
}
