package at.modalog.cordova.plugin.html2pdf;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by apostolov on 1/18/17.
 */

public class FileUtils {
    public static String renameFileExtension
            (String source, String newExtension)
    {
        String target;
        String currentExtension = getFileExtension(source);

        if (currentExtension.equals("")){
            target = source + "." + newExtension;
        }
        else {
            target = source.replaceFirst(Pattern.quote("." +
                    currentExtension) + "$", Matcher.quoteReplacement("." + newExtension));

        }
        return target;
    }

    public static String getFileExtension(String f) {
        String ext = "";
        int i = f.lastIndexOf('.');
        if (i > 0 &&  i < f.length() - 1) {
            ext = f.substring(i + 1);
        }
        return ext;
    }
}
