package ex.rr.tasklist.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
public class FileExportImpl implements FileExport {

    private final Logger logger = LoggerFactory.getLogger(FileExportImpl.class);

    private static final String EXPORT_DIRECTORY = "temp";
    private final Path directoryPath = Paths.get("", EXPORT_DIRECTORY);
    private final File directory = directoryPath.toFile();

    private void checkDirectory() {
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public Path exportTxt(String fileName, Object contents) throws IOException {
        checkDirectory();
        Path filePath = directoryPath.resolve(fileName);
        return Files.write(filePath.toAbsolutePath(), contents.toString().getBytes(), StandardOpenOption.CREATE);
    }

    @Override
    public Path exportPdf(String fileName, Object contents) {
        return null;
    }
}
