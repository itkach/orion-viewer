package universe.constellation.orion.viewer.opds.net;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import universe.constellation.orion.viewer.Common;
import universe.constellation.orion.viewer.OrionBaseActivity;
import universe.constellation.orion.viewer.OrionFileManagerActivity;
import universe.constellation.orion.viewer.R;
import universe.constellation.orion.viewer.opds.StringUtils;

/**
 * Created by mike on 9/19/14.
 */
public class DownloadUtil {

    private static int notificationId;

    public static InputStream download(String urlString) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(4096);
        try {
            download(urlString, byteArrayOutputStream, Progress.SKIP);
        } finally {
            byteArrayOutputStream.close();
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public static void downloadToFile(String urlString, File toFile, Progress progress) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(toFile);
        try {
            download(urlString, fileOutputStream, progress);
        } finally {
            try {
                fileOutputStream.close();
            } catch (Exception e) {
                Common.d(e);
            }
        }
    }

    public static void download(String urlString, OutputStream output, Progress progress) throws Exception {
        InputStream input = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }

            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            byte data[] = new byte[4096];
            int count;
            int downloadedSize = 0;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
                downloadedSize += count;
                if (fileLength > 0) {
                    progress.progressPercent(downloadedSize * 100 / fileLength);
                } else {
                    progress.progressTotal(downloadedSize);
                }
            }
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (IOException ignored) {
                Common.d(ignored);
            }

            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ignored) {
                Common.d(ignored);
            }

            if (connection != null)
                connection.disconnect();
        }
    }

    public static String getAbsoluteUrl(String url, String baseUrl) {
        Common.d("Catalog url original " + url);
        if (url.startsWith("http://")) {

        }
        else if (url.startsWith("//")) {
            url = "http:" + url;
        }
        else if (url.startsWith("/")) {
            int i = baseUrl.indexOf("//");
            if (i > 0) {
                i = baseUrl.indexOf("/", i +  2);
            }
            url = baseUrl.substring(0, i) + url;
        }
        else {
            url = baseUrl + "/" + url;
        }
        Common.d("Absolute catalogUrl is " + url);
        return url;
    }

    public static void downloadFile(Activity activity, String absoluteUrl, File fileToDownload) {
        NotificationCompat.Builder mBuilder = createNotification(activity, absoluteUrl, -1, fileToDownload, false);

        NotificationManager mNotificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        int id = ++notificationId;
        mNotificationManager.notify(id, mBuilder.build());
        new DownloadFilesTask(activity, absoluteUrl, fileToDownload, id).execute();
    }

    private static NotificationCompat.Builder createNotification(Activity activity, String absoluteUrl, long percent, File fileToDownload, boolean finished) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity);
        if (finished) {
            builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
            builder.setContentText(fileToDownload.getAbsolutePath()).setContentTitle(percent > 0 ? "Downloaded" : "Error");
        } else {
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
            String title = "Downloading " + (percent > 0 ? (percent <= 100 ? percent + "%" : StringUtils.buityfySize(percent)) : "") + "...";
            builder.setContentText(absoluteUrl).setContentTitle(title);
        }

        Intent intentForFile = OrionFileManagerActivity.createIntentForFile(activity, fileToDownload);
        builder.setContentIntent(PendingIntent.getActivity(activity, 0, intentForFile, 0 /*finished ? PendingIntent.FLAG_ONE_SHOT : PendingIntent.FLAG_NO_CREATE*/));

        return builder;
    }

    private static class DownloadFilesTask extends AsyncTask<String, Long, Long> implements Progress {

        private final Activity activity;
        private final String url;
        private File file;
        private File tempFile;
        private final int notificationId;

        public DownloadFilesTask(Activity activity, String url, File file, int notificationId) {
            this.activity = activity;
            this.url = url;
            this.file = file;
            tempFile = new File(file.getAbsolutePath() + ".tmp");
            this.notificationId = notificationId;
        }

        protected Long doInBackground(String... urls) {
            try {
                DownloadUtil.downloadToFile(url, tempFile, this);
            } catch (Exception e) {
                OrionBaseActivity.showAlert("Error", "Couldn't download " + url + " to " + file.getAbsolutePath() + " cause: " + e.getMessage(), (OrionBaseActivity) activity);
                Common.d(e);
                return -1l;
            }

            try {
                tempFile.renameTo(file);
            } catch (Exception e) {
                OrionBaseActivity.showAlert("Error", "Couldn't rename temporary file: " + e.getMessage(), (OrionBaseActivity) activity);
                Common.d(e);
                return -1l;
            }
            return 1l;
        }

        @Override
        public void progressPercent(int percent) {
            publishProgress(Long.valueOf(percent));
        }

        @Override
        public void progressTotal(long size) {
            publishProgress(size);
        }

        protected void onProgressUpdate(Long... progress) {
            NotificationCompat.Builder notification = createNotification(activity, url, progress[0], file, false);
            NotificationManager mNotificationManager =
                    (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(notificationId, notification.build());
        }

        protected void onPostExecute(Long result) {
            NotificationCompat.Builder notification = createNotification(activity, url, result, file, true);
            NotificationManager mNotificationManager =
                    (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(notificationId, notification.build());
        }
    }

    public interface Progress {

        void progressPercent(int percent);

        void progressTotal(long size);

        Progress SKIP = new Progress() {
            @Override
            public void progressTotal(long size) {

            }

            @Override
            public void progressPercent(int percent) {

            }
        };
    }

}
