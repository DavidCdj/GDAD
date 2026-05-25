import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public final class RutaAplicacion {

    private static File baseDir;

    private RutaAplicacion() {
    }

    public static synchronized File baseDir() {
        if (baseDir == null) {
            baseDir = detectarBaseDir();
        }
        return baseDir;
    }

    public static File recurso(String nombre) {
        return new File(new File(baseDir(), "recursos"), nombre);
    }

    public static File archivo(String... partes) {
        File actual = baseDir();
        for (String parte : partes) {
            actual = new File(actual, parte);
        }
        return actual;
    }

    private static File detectarBaseDir() {
        File desdeCodigo = desdeCodeSource();
        File base = buscarBase(desdeCodigo);
        if (base != null) {
            return base;
        }

        File desdeUserDir = new File(System.getProperty("user.dir"));
        base = buscarBase(desdeUserDir);
        return base != null ? base : desdeUserDir;
    }

    private static File desdeCodeSource() {
        try {
            URL location = RutaAplicacion.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return new File(System.getProperty("user.dir"));
            }
            File locationFile = new File(location.toURI());
            if (locationFile.isFile()) {
                return locationFile.getParentFile();
            }
            return locationFile;
        } catch (URISyntaxException ex) {
            return new File(System.getProperty("user.dir"));
        }
    }

    private static File buscarBase(File inicio) {
        File actual = inicio;
        while (actual != null) {
            File recursos = new File(actual, "recursos");
            File dist = new File(actual, "dist");
            if (recursos.exists() || dist.exists()) {
                return actual;
            }
            actual = actual.getParentFile();
        }
        return null;
    }
}