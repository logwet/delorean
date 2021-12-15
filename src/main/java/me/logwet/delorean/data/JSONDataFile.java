package me.logwet.delorean.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Objects;
import me.logwet.delorean.DeLorean;
import org.jetbrains.annotations.Nullable;

public class JSONDataFile<T> {
    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private final File file;
    private final Class<T> clazz;

    public JSONDataFile(File file, Class<T> clazz) {
        this.file = file;
        this.clazz = clazz;
    }

    @Nullable
    public T read() {
        if (file.exists() && !file.isDirectory()) {
            try (Reader reader = Files.newBufferedReader(file.toPath())) {
                try {
                    return GSON.fromJson(reader, clazz);
                } catch (ClassCastException e) {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public boolean write(@Nullable T obj) {
        if (Objects.isNull(obj)) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            return false;
        }

        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(obj, clazz, writer);
            return true;
        } catch (IOException e) {
            DeLorean.LOGGER.error("Failed to write data file" + e);
        }

        return false;
    }
}
