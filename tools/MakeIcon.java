import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Generates icon.png: a magnifying glass over a gold coin (who sells it?), and a chat bubble with "/" in it (the
 * command typed into the chat). Drawn at 64x64 and scaled to 128x128 so it stays crisp in Mod Menu.
 * Run with a Java that has AWT: javac tools/MakeIcon.java and java -cp tools MakeIcon, from the project folder.
 */
public class MakeIcon {
    static final int N = 64;
    static final int[] PX = new int[N * N];

    public static void main(String[] args) throws Exception {
        badge(0xFF2A3346, 0xFF141822);
        coin(21, 41, 13);
        magnifier(36, 28, 11);
        bubble();
        write(new File("src/main/resources/assets/sellerlookup/icon.png"), 2);
    }

    static void set(int x, int y, int argb) {
        if (x < 0 || y < 0 || x >= N || y >= N || PX[y * N + x] == 0) return;
        PX[y * N + x] = argb;
    }

    static void badge(int top, int bottom) {
        int r = 10;
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                int cx = Math.max(r, Math.min(N - 1 - r, x));
                int cy = Math.max(r, Math.min(N - 1 - r, y));
                if ((x - cx) * (x - cx) + (y - cy) * (y - cy) > r * r) continue;
                PX[y * N + x] = mix(top, bottom, y / (double) (N - 1));
            }
        }
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                if (PX[y * N + x] == 0) continue;
                boolean edge = x == 0 || y == 0 || x == N - 1 || y == N - 1
                    || PX[y * N + x - 1] == 0 || PX[y * N + x + 1] == 0 || PX[(y - 1) * N + x] == 0 || PX[(y + 1) * N + x] == 0;
                if (edge) PX[y * N + x] = 0xFF4A5670;
            }
        }
    }

    /** A gold coin with a "$". */
    static void coin(int cx, int cy, int r) {
        for (int y = -r; y <= r; y++) {
            for (int x = -r; x <= r; x++) {
                double d = Math.sqrt(x * x + y * y);
                if (d > r + 0.4) continue;
                int color = d > r - 1.6 ? 0xFF9A6A12 : mix(0xFFFFD866, 0xFFD99A1E, (x + y + 2.0 * r) / (4.0 * r));
                set(cx + x, cy + y, color);
            }
        }
        String[] dollar = {
            "..#..",
            ".####",
            "#.#..",
            ".###.",
            "..#.#",
            "####.",
            "..#..",
        };
        for (int y = 0; y < dollar.length; y++) {
            for (int x = 0; x < dollar[y].length(); x++) {
                if (dollar[y].charAt(x) == '#') set(cx - 2 + x, cy - 3 + y, 0xFF7A4E08);
            }
        }
    }

    /** A chat bubble at the top right with "/" in it. */
    static void bubble() {
        int x0 = 38, y0 = 4, x1 = 59, y1 = 17;
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                boolean corner = (x == x0 || x == x1) && (y == y0 || y == y1);
                if (corner) continue;
                boolean edge = x == x0 || x == x1 || y == y0 || y == y1;
                set(x, y, edge ? 0xFF8FA3C8 : 0xFFE9EEF7);
            }
        }
        // the tail
        set(40, y1 + 1, 0xFF8FA3C8);
        set(41, y1 + 1, 0xFFE9EEF7);
        set(40, y1 + 2, 0xFF8FA3C8);
        // "/"
        for (int i = 0; i < 9; i++) set(49 - i / 2, y0 + 3 + i, 0xFF2A3346);
        // a text cursor
        for (int y = y0 + 4; y <= y1 - 3; y++) set(53, y, 0xFF5B6B8C);
    }

    /** A magnifying glass: dark rim, light glass with a shine, handle to the bottom right. */
    static void magnifier(int cx, int cy, int r) {
        for (int y = -r - 2; y <= r + 2; y++) {
            for (int x = -r - 2; x <= r + 2; x++) {
                double d = Math.sqrt(x * x + y * y);
                if (d > r + 1.5) continue;
                if (d > r - 1.5) {
                    set(cx + x, cy + y, 0xFF3C4250);
                } else {
                    // see-through glass: lighten what is under it
                    int under = PX[(cy + y) * N + cx + x];
                    if (under != 0) set(cx + x, cy + y, mix(under, 0xFFBFE6FF, 0.45));
                }
            }
        }
        for (int i = 0; i < 4; i++) set(cx - 6 + i, cy - 5 - (i < 2 ? 0 : 1), 0xFFFFFFFF);
        set(cx - 6, cy - 4, 0xFFFFFFFF);
        for (int i = 0; i < 12; i++) {
            int hx = cx + r - 1 + i, hy = cy + r - 1 + i;
            for (int w = -1; w <= 1; w++) {
                set(hx + w, hy, 0xFF6B4B2A);
                set(hx, hy + w, 0xFF6B4B2A);
            }
            set(hx - 1, hy + 1, 0xFF4A3218);
        }
    }

    static int mix(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int ar = a >> 16 & 255, ag = a >> 8 & 255, ab = a & 255;
        int br = b >> 16 & 255, bg = b >> 8 & 255, bb = b & 255;
        return 0xFF000000 | (int) (ar + (br - ar) * t) << 16 | (int) (ag + (bg - ag) * t) << 8 | (int) (ab + (bb - ab) * t);
    }

    static void write(File out, int factor) throws Exception {
        BufferedImage img = new BufferedImage(N * factor, N * factor, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < N * factor; y++) {
            for (int x = 0; x < N * factor; x++) img.setRGB(x, y, PX[(y / factor) * N + x / factor]);
        }
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("wrote " + out);
    }
}
