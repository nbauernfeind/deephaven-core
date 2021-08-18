package io.deephaven.db.appmode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

public class ApplicationUtil {
    private static final String FILE_PROP_PREFIX = "file_";

    public static Path[] findFilesFrom(final Properties properties) {
        OrderedFile[] orderedFiles =
            properties.stringPropertyNames().stream().filter(ApplicationUtil::isFileProperty)
                .map(prop -> toOrderedFile(prop, Paths.get(properties.getProperty(prop))))
                .toArray(OrderedFile[]::new);
        Arrays.sort(orderedFiles);

        return Arrays.stream(orderedFiles).map(of -> of.file).toArray(Path[]::new);
    }


    private static boolean isFileProperty(final String propName) {
        if (!propName.startsWith(FILE_PROP_PREFIX)
            || propName.length() == FILE_PROP_PREFIX.length()) {
            return false;
        }

        for (int i = FILE_PROP_PREFIX.length(); i < propName.length(); ++i) {
            char ch = propName.charAt(i);
            if (ch < '0' || ch > '9') {
                return false;
            }
        }

        return true;
    }

    private static OrderedFile toOrderedFile(final String propName, final Path path) {
        final String orderString = propName.substring(FILE_PROP_PREFIX.length());
        return new OrderedFile(Integer.parseInt(orderString), path);
    }

    private static class OrderedFile implements Comparable<OrderedFile> {
        int order;
        Path file;

        OrderedFile(int order, Path file) {
            this.file = file;
            this.order = order;
        }

        @Override
        public int compareTo(final OrderedFile o) {
            return Integer.compare(order, o.order);
        }
    }
}
