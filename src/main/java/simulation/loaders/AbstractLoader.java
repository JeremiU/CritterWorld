package simulation.loaders;

import java.io.*;

public abstract class AbstractLoader {

    private InputStream is;
    private final String fileName;

    public AbstractLoader(String fileName) {
        this.fileName = fileName;
    }

    public void readFile() {
        try {
            is = new FileInputStream(fileName);
            BufferedReader r = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = r.readLine()) != null) readLine(line);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        afterRead();

        // TODO: load new inputStream into AST parser
    }

    public abstract void readLine(String line);

    public abstract void afterRead();

    public InputStream getInputStream() {
        return is;
    }
}