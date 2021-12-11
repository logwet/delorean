package me.logwet.delorean.patch.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Objects;
import me.logwet.delorean.DeLorean;
import org.jetbrains.annotations.Nullable;

public class DataFile<T> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File file;
    private final Type type = new TypeToken<T>() {}.getType();

    public DataFile(File file) {
        this.file = file;
    }

    @Nullable
    public T read() {
        if (file.exists() && !file.isDirectory()) {
            try (Reader reader = Files.newBufferedReader(file.toPath())) {
                return GSON.fromJson(reader, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public boolean write(@Nullable T obj) {
        if (Objects.isNull(obj)) {
            file.delete();
            return false;
        }

        try (Writer writer = new FileWriter(file)) {
            System.out.println(GSON.toJson(obj, type));
            GSON.toJson(obj, type, writer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            DeLorean.LOGGER.debug(e);
        }

        return false;
    }
}
