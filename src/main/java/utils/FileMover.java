package utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileMover {
    private FileMover() {
    }

    public static void moveAllFile(String path) throws IOException {
        FileUtils.deleteDirectory(new File(path));
        File stock = new File(path);

        try (Stream<Path> paths = Files.walk(Paths.get(""))) {
            List<String> files = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().matches("^\\w{3}\\.txt$"))
                    .map(Path::toString)
                    .collect(Collectors.toList());

            files.forEach(file -> {
                try {
                    FileUtils.moveFileToDirectory(new File(file), stock, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
