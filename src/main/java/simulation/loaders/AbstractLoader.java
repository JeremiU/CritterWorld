package simulation.loaders;

import console.Logger;

import java.io.*;

public abstract class AbstractLoader {

    private final String fileName;

    public AbstractLoader(String fileName) {
        this.fileName = fileName;
    }

    public void readFile() {
        try {
            InputStream is = new FileInputStream(fileName);
            BufferedReader r = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = r.readLine()) != null) readLine(line);
        } catch (IOException ex) {
            Logger.error("Error reading file " + ex.getMessage(), "AbstractLoader:readFile", Logger.FLAG_ABSTRACT_LOADER);
            return;
        }
        afterRead();
    }

    public abstract void readLine(String line);

    public abstract void afterRead();
}