import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class SaveFileManager {
    private SaveFileManager() {
    }

    public static File resolveSavesDirectory() {
        File direct = new File("saves");
        File mainRelative = new File("Main", "saves");
        if (direct.exists() && direct.isDirectory()) {
            return direct;
        }
        if (mainRelative.exists() && mainRelative.isDirectory()) {
            return mainRelative;
        }
        if (direct.mkdirs() || direct.exists()) {
            return direct;
        }
        if (mainRelative.mkdirs() || mainRelative.exists()) {
            return mainRelative;
        }
        return direct;
    }

    public static File[] listSaveFiles() {
        File savesDir = resolveSavesDirectory();
        File[] saves = savesDir.listFiles((dir, name) -> name.endsWith(".ser"));
        return saves == null ? new File[0] : saves;
    }

    public static Hotel load(String saveName) throws IOException, ClassNotFoundException {
        File saveFile = new File(resolveSavesDirectory(), saveName + ".ser");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            return (Hotel) ois.readObject();
        }
    }

    public static void save(String saveName, Hotel hotel) throws IOException {
        File saveFile = new File(resolveSavesDirectory(), saveName + ".ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(hotel);
        }
    }

    public static boolean delete(String saveName) {
        File saveFile = new File(resolveSavesDirectory(), saveName + ".ser");
        return saveFile.delete();
    }
}
