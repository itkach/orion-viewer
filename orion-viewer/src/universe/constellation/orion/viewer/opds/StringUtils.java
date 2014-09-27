package universe.constellation.orion.viewer.opds;

import java.util.Iterator;
import java.util.List;

/**
 * Created by mike on 9/20/14.
 */
public class StringUtils {

    public static String join(List<String> values, char separator) {
        StringBuilder sb = new StringBuilder("");
        for (Iterator<String> iterator = values.iterator(); iterator.hasNext(); ) {
            String next = iterator.next();
            sb.append(next);
            if (iterator.hasNext()) {
                sb.append(separator);
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    public static String buityfySize(long size) {
        if (size < 1024) {
            return size + "b";
        }
        if (size < 1024 * 1024) {
            return (size / 1024) + "." + (size % 1024)/103 + "Kb";
        }
        if (size < 1024 * 1024 * 1024) {
            return (size / (1024 * 1024)) + "." + (size % (1024 * 1024))/(103*1024) + "Mb";
        }
        return size + "b";
    }
}
