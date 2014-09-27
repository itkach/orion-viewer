package universe.constellation.orion.viewer.opds;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import java.io.File;
import java.util.Map;

import universe.constellation.orion.viewer.Common;
import universe.constellation.orion.viewer.OrionBaseActivity;
import universe.constellation.orion.viewer.R;
import universe.constellation.orion.viewer.opds.net.DownloadUtil;

/**
 * Created by mike on 9/20/14.
 */
public class BookFragment extends DialogFragment {



    static BookFragment newInstance(FeedEntry entry) {
        BookFragment f = new BookFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable("book", entry);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final FeedEntry book = (FeedEntry) getArguments().getSerializable("book");
        getDialog().setTitle(book.getTitle());

        View view = inflater
                .inflate(R.layout.opds_book_info, container, false);
        WebView webView = (WebView) view.findViewById(R.id.webView);

        Common.d("FEED" + book.getContent() );
        boolean isEmpty = book.getContent() == null || "".equals(book.getContent().trim());
        String summary = "<html>" +
                "<body>" + (!isEmpty ? book.getContent() : "No description") +"</body></html>";

        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        webView.loadDataWithBaseURL(null, summary, "text/html", "utf-8", null);

        final Button download = (Button) view.findViewById(R.id.download);

        download.setEnabled(book.canDownload());
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Map<String, String> downloadList = book.getDownloadList();
                for (Map.Entry<String, String> stringStringEntry : downloadList.entrySet()) {
                    Common.d((stringStringEntry.getKey() + " " + stringStringEntry.getValue()));
                }

                if (downloadList.size() == 1) {
                    String absoluteUrl = DownloadUtil.getAbsoluteUrl(downloadList.entrySet().iterator().next().getValue(), book.baseUrl);
                    startBookDownloading(absoluteUrl);
                    dismiss();
                    return;
                }

                PopupMenu pp = new android.support.v7.widget.PopupMenu(getActivity(), download);
                for (final String next : downloadList.keySet()) {
                    final MenuItem add = pp.getMenu().add(next);
                    add.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            try {
                                String absoluteUrl = DownloadUtil.getAbsoluteUrl(downloadList.get(next), book.baseUrl);
                                startBookDownloading(absoluteUrl);
                                dismiss();
                            } catch (Exception e) {
                                OrionBaseActivity.showAlert("Error on attempting download file", e.getMessage(), (OrionBaseActivity) getActivity());
                                Common.d(e);
                            }
                            return true;
                        }
                    });
                }
                pp.show();
            }
        });

        Button ok = (Button) view.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    private void startBookDownloading(String absoluteUrl) {
        int lastIndexOf = absoluteUrl.lastIndexOf('/');
        File dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? Environment.DIRECTORY_DOWNLOADS : ""), "orion");

        dirFile.mkdirs();
        File documentFile = new File(dirFile, absoluteUrl.substring(lastIndexOf + 1));
        DownloadUtil.downloadFile(getActivity(), absoluteUrl, documentFile);
    }
}
