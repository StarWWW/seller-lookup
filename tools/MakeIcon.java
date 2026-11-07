import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

/**
 * Generates icon.png in Minecraft's own item style: a name tag (the seller's name) above Minecraft's chat box with
 * "/ah" being typed into it. 32 x 32 pixels, each written as 8 x 8.
 * Run from the project folder with a JDK that has AWT: java tools/MakeIcon.java
 */
public class MakeIcon {
    public static void main(String[] args) throws Exception {
        nameTag(1, 0, 23);
        chat(0, 22);
        write("src/main/resources/assets/sellerlookup/icon.png");
    }

    /**
     * A name tag lying from the bottom left to the top right of a {@code size} square: a pale tag shaded across its
     * width, two lines of writing on it, a hole near its pointed end and a string looped through the hole.
     */
    static void nameTag(int x0, int y0, int size) {
        layer();
        double sx = 3.5, sy = size - 3.5, ex = size - 6.5, ey = 6.5;
        double len = Math.hypot(ex - sx, ey - sy), dx = (ex - sx) / len, dy = (ey - sy) / len;
        double holeU = len - 2.2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double px = x + 0.5 - sx, py = y + 0.5 - sy;
                double u = px * dx + py * dy, w = -px * dy + py * dx;
                double half = 4.3 - Math.max(0, u - (len - 3.2)) * 1.25;
                if (u < -1.2 || u > len + 0.8 || Math.abs(w) > half) continue;
                int c = w < -2 ? 0xF4EEDA : w > 2 ? 0xBDB292 : 0xE0D6B8;
                if (Math.abs(w) > half - 0.9 || u < -0.4) c = shade(c, 0.84);
                // the name written on it
                boolean line = (Math.abs(w + 0.9) < 0.5 || Math.abs(w - 1.3) < 0.5) && u > 1 && u < len - 5;
                if (line && Math.floor(u * 1.4) % 4 != 3) c = 0x7D7258;
                if (Math.abs(u - holeU) < 0.8 && Math.abs(w) < 0.9) c = 0x3A3126;
                set(x0 + x, y0 + y, c);
            }
        }
        // the string, from the hole round to the right
        int hx = (int) Math.floor(sx + dx * holeU), hy = (int) Math.floor(sy + dy * holeU);
        int[][] loop = {{1, -1}, {2, -2}, {3, -3}, {4, -3}, {5, -2}, {5, -1}, {5, 0}, {4, 1}};
        for (int i = 0; i < loop.length; i++) set(x0 + hx + loop[i][0], y0 + hy + loop[i][1], i % 2 == 0 ? 0xEDEDED : 0xB4B4B4);
        flatten(true);
    }

    static final String[] SLASH = {"....#", "....#", "...#.", "..#..", ".#...", "#....", "#...."};
    static final String[] A = {".....", ".....", ".###.", "....#", ".####", "#...#", ".####"};
    static final String[] H = {"#....", "#....", "#.##.", "##..#", "#...#", "#...#", "#...#"};
    static final String[] CURSOR = {".....", ".....", ".....", ".....", ".....", ".....", "#####"};

    /** Minecraft's chat box: a dark bar with "/ah" and the cursor, in white pixel letters with their shadow. */
    static void chat(int x0, int y0) {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 32; x++) set(x0 + x, y0 + y, y == 0 ? 0x4A4A4A : 0x262626);
        }
        type(x0 + 4, y0 + 2, 0x3F3F3F, SLASH, A, H, CURSOR);
        type(x0 + 3, y0 + 1, 0xFFFFFF, SLASH, A, H, CURSOR);
    }

    /** Pixel letters side by side, one pixel apart. */
    static void type(int x, int y, int color, String[]... glyphs) {
        for (String[] glyph : glyphs) {
            sprite(x, y, glyph, "#", color);
            x += glyph[0].length() + 1;
        }
    }

    // ---------------------------------------------------------------- pixel kit (the same in every mod's icon tool)

    /** 32 x 32 pixels: items at the size Minecraft draws them (16 x 16), each pixel written as 8 x 8 (256 x 256). */
    static final int N = 32, SCALE = 8;
    static final int[] PX = new int[N * N];
    /** Where drawing goes: the picture, or a layer that gets an outline before it is put on the picture. */
    static int[] target = PX;

    static void set(int x, int y, int rgb) {
        if (x < 0 || y < 0 || x >= N || y >= N) return;
        target[y * N + x] = 0xFF000000 | rgb;
    }

    static int shade(int rgb, double f) {
        int r = (int) Math.min(255, Math.round(((rgb >> 16) & 255) * f));
        int g = (int) Math.min(255, Math.round(((rgb >> 8) & 255) * f));
        int b = (int) Math.min(255, Math.round((rgb & 255) * f));
        return (r << 16) | (g << 8) | b;
    }

    static int mix(int a, int b, double t) {
        int r = (int) Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    /** Starts a layer: an item drawn on its own, so it can get its dark outline. */
    static void layer() {
        target = new int[N * N];
    }

    /**
     * Puts the layer on the picture. With {@code outline}, every empty pixel next to it first gets a dark shade of
     * its neighbour, the way Minecraft's item sprites are outlined, so the item stands out from what is behind it.
     */
    static void flatten(boolean outline) {
        int[] layer = target;
        target = PX;
        if (outline) {
            for (int y = 0; y < N; y++) {
                for (int x = 0; x < N; x++) {
                    if (layer[y * N + x] != 0) continue;
                    int n = neighbour(layer, x, y);
                    if (n != 0) PX[y * N + x] = 0xFF000000 | shade(n, 0.32);
                }
            }
        }
        for (int i = 0; i < PX.length; i++) if (layer[i] != 0) PX[i] = layer[i];
    }

    static int neighbour(int[] layer, int x, int y) {
        int[][] d = {{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        for (int[] o : d) {
            int nx = x + o[0], ny = y + o[1];
            if (nx >= 0 && ny >= 0 && nx < N && ny < N && layer[ny * N + nx] != 0) return layer[ny * N + nx];
        }
        return 0;
    }

    /** Pixel rows, one character per pixel: '.' is empty, any other is looked up in {@code keys}/{@code colors}. */
    static void sprite(int x0, int y0, String[] rows, String keys, int... colors) {
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                int k = keys.indexOf(rows[y].charAt(x));
                if (k >= 0) set(x0 + x, y0 + y, colors[k]);
            }
        }
    }

    /** A 16 x 16 texture, every pixel one of the colours at random: the grain of Minecraft's textures. */
    static int[] texture(long seed, int... colors) {
        Random rnd = new Random(seed);
        int[] t = new int[256];
        for (int i = 0; i < 256; i++) t[i] = colors[rnd.nextInt(colors.length)];
        return t;
    }

    /** A 16 x 16 texture from 8 x 8 pixel rows, each pixel doubled (the way mob skins are drawn). */
    static int[] skin(String[] rows, String keys, int... colors) {
        int[] t = new int[256];
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) t[y * 16 + x] = colors[keys.indexOf(rows[y / 2].charAt(x / 2))];
        }
        return t;
    }

    /**
     * A block the way Minecraft shows one in the inventory: seen from above at an angle, the top fully lit, the left
     * side a little darker and the right side darker still. (x0, y0) is the top left of a w x w square.
     */
    static void block(int x0, int y0, int w, int[] top, int[] left, int[] right) {
        double hw = w / 2.0, q = w / 4.0;
        for (int py = 0; py < w; py++) {
            for (int px = 0; px < w; px++) {
                double x = px + 0.5, y = py + 0.5;
                double a = (y / q + (x - hw) / hw) / 2, b = (y / q - (x - hw) / hw) / 2;
                if (in(a) && in(b)) {
                    set(x0 + px, y0 + py, sample(top, a, b));
                    continue;
                }
                a = x / hw;
                b = (y - q - a * q) / hw;
                if (in(a) && in(b)) {
                    set(x0 + px, y0 + py, shade(sample(left, a, b), 0.8));
                    continue;
                }
                a = (x - hw) / hw;
                b = (y - hw + a * q) / hw;
                if (in(a) && in(b)) set(x0 + px, y0 + py, shade(sample(right, a, b), 0.6));
            }
        }
    }

    static boolean in(double v) {
        return v >= 0 && v < 1;
    }

    static int sample(int[] tex, double u, double v) {
        return tex[Math.min(15, (int) (v * 16)) * 16 + Math.min(15, (int) (u * 16))];
    }

    /** Writes the picture with every pixel as SCALE x SCALE, so it stays sharp at any size. */
    static void write(String path) throws Exception {
        BufferedImage img = new BufferedImage(N * SCALE, N * SCALE, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < N * SCALE; y++) {
            for (int x = 0; x < N * SCALE; x++) img.setRGB(x, y, PX[(y / SCALE) * N + x / SCALE]);
        }
        File out = new File(path);
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("Wrote " + out);
    }
}
