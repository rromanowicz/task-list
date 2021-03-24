package ex.rr.tasklist.files;

import java.io.IOException;
import java.nio.file.Path;

public interface FileExport {

    Path exportTxt(String fileName, Object contents) throws IOException;

    Path exportPdf(String fileName, Object contents);

}
