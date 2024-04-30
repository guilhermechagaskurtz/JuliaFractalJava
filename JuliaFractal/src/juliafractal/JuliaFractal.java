package juliafractal;

//-0.223, 0.745
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.math3.complex.Complex;

public class JuliaFractal {

    private double RANGE_REAL_INI = 0.00, RANGE_REAL_FIM = 0.12;
    private double RANGE_IMAG_INI = 0.15, RANGE_IMAG_FIM = 0.27;
    private double FACTOR = 0.000025;
    private int MAX_INTERATIONS = 256;
    private int xPos, yPos;
    private int width, height;
    private Complex c;
    private BufferedImage img;
    private Color corPintar;
    private int TIPO_EQ = 1;

    long tempoPintar = 0;
    long tempoVerificar = 0;

    public void functionDefinition(Complex c) {
        this.c = c;
    }

    public int getColor(int r, int g, int b) {
        int col = (r << 16) | (g << 8) | b;
        return col;
    }

    public BufferedImage getImage() {
        return img;
    }

    public void createImage() {
        width = (int) ((RANGE_REAL_FIM - RANGE_REAL_INI) / FACTOR) + 1;
        height = (int) ((RANGE_IMAG_FIM - RANGE_IMAG_INI) / FACTOR) + 1;
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int white = getColor(255, 255, 255);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, white);
            }

        }
    }

    public double getMagnitude(Complex zIni) {
        double mag = Math.sqrt(zIni.getReal() * zIni.getReal() + zIni.getImaginary() * zIni.getImaginary());
        return mag;
    }

    public Complex f(Complex z) {
        switch (TIPO_EQ) {
            case 1:
                return c.add(z.multiply(z));
            case 2:
                return c.add(z.multiply(z.multiply(z)));
            case 3:
                return (c.add(z.multiply(z))).divide(z.subtract(c));
            case 4:
                return c.add((z.multiply(z)).subtract(z));
            case 5:
                return (z.exp()).subtract(c);
            case 6:
                return (((z.multiply(z)).add(z)).divide(z.log())).add(c);
            case 7:
                return (z.multiply(z.multiply(z))).exp().add(c);
            default:
                return c.add(z.multiply(z));
        }
    }

    public void blur() {
        float[] matrix = {0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f,
            0.0625f, 0.125f, 0.0625f};

        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));

        BufferedImage bdest = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        op.filter(img, bdest);
        img = bdest;
    }

    private BufferedImage escreveLimites(BufferedImage old) {
        int w = old.getWidth();
        int h = old.getHeight();
        BufferedImage img = new BufferedImage(
                w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(old, 0, 0, null);
        g2d.setPaint(Color.red);
        g2d.setFont(new Font("Serif", Font.BOLD, 20));
        String s = new DecimalFormat("##.#####").format(RANGE_REAL_INI);
        FontMetrics fm = g2d.getFontMetrics();
        int x = 0;
        int y = img.getHeight()/2;
        g2d.drawString(s, x, y);
        s = new DecimalFormat("##.#####").format(RANGE_REAL_FIM);
        x = img.getWidth()-fm.stringWidth(s);
        g2d.drawString(s, x, y);
        g2d.dispose();
        return img;
    }
    public void antialias() {
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, 0, 0, null);
    }

    public void paintColoredPoint(int x, int y) {
        if (x < width && y < height) {
            try {
                img.setRGB(x, (height - 1) - y, corPintar.getRGB());
            } catch (Exception e) {
                System.out.println("Error: " + x + "," + ((width - 1) - y));
            }
        }
    }

    public void setCorPintar(int i) {
        float Brightness = i < MAX_INTERATIONS ? 1f : 0;
        float Hue = 0.1f + (i % MAX_INTERATIONS) / 255.0f;
        corPintar = Color.getHSBColor((float) Hue, 1f, Brightness);
    }

    public void setCorPintarPsicodelico(int i, double zAbs) {

        double nsmooth = i + 1 - Math.log(Math.log(zAbs)) / Math.log(2);
        float Brightness = i < MAX_INTERATIONS ? 1f : 0;
        corPintar = Color.getHSBColor((float) nsmooth, 0.9f, Brightness);
    }

    public void checkConvergence(Complex zIni) {
        int i;
        for (i = 0; i < MAX_INTERATIONS && zIni.abs() < 2; i++) {
            zIni = f(zIni);
        }
        setCorPintar(i);
    }

    public class juliaThread extends Thread {

        private double tRANGE_REAL_INI, tRANGE_REAL_FIM;
        int xIni;

        public juliaThread(double tRRINI, double TRRFIM, int xIni) {
            tRANGE_REAL_INI = tRRINI;
            tRANGE_REAL_FIM = TRRFIM;
            this.xIni = xIni;
        }

        public void run() {
            int y = 0;
            for (double r = tRANGE_REAL_INI; r < tRANGE_REAL_FIM; r += FACTOR, xIni++) {
                y = 0;
                for (double i = RANGE_IMAG_INI; i < RANGE_IMAG_FIM; i += FACTOR, y++) {
                    if (xIni < width && y < height) {
                        Complex zIni = new Complex(r, i);
                        checkConvergence(zIni);
                        paintColoredPoint(xIni, y);
                    }
                }
            }
        }
    }

    public void startComThreads(double real, double imaginary) {
        long tini = System.currentTimeMillis();
        functionDefinition(new Complex(real, imaginary));
        createImage();

        juliaThread threads[] = new juliaThread[4];
        double porcao = (RANGE_REAL_FIM - RANGE_REAL_INI) / 4;
        int x = 0;
        for (int i = 0; i < 4; i++) {
            threads[i] = new juliaThread(RANGE_REAL_INI + i * porcao, RANGE_REAL_INI + (i + 1) * porcao, x);
            threads[i].start();
            x += porcao / FACTOR;
        }
        for (int i = 0; i < 4; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(JuliaFractal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        long tfim = System.currentTimeMillis();
        tempoVerificar = tfim - tini;
        System.out.println(tempoVerificar + " milissegundos");
    }

    public void start(double real, double imaginary) {
        long tini = System.currentTimeMillis();
        functionDefinition(new Complex(real, imaginary));
        createImage();
        int x = 0, y = 0;
        x = 0;
        for (double r = RANGE_REAL_INI; r < RANGE_REAL_FIM; r += FACTOR, x++) {
            y = 0;
            for (double i = RANGE_IMAG_INI; i < RANGE_IMAG_FIM; i += FACTOR, y++) {
                if (x < width && y < height) {
                    Complex zIni = new Complex(r, i);
                    checkConvergence(zIni);
                    paintColoredPoint(x, y);
                }
            }
        }
        img = escreveLimites(img);
        //blur();
        //antialias();
    }

    public JuliaFractal(double real, double imaginary, double RANGE_REAL_INI, double RANGE_REAL_FIM, double RANGE_IMAG_INI, double RANGE_IMAG_FIM, double FACTOR, int MAX_INTERATIONS, int TIPO_EQ, int xPos, int yPos) {
        this.RANGE_REAL_INI = RANGE_REAL_INI;
        this.RANGE_REAL_FIM = RANGE_REAL_FIM;
        this.RANGE_IMAG_INI = RANGE_IMAG_INI;
        this.RANGE_IMAG_FIM = RANGE_IMAG_FIM;
        this.FACTOR = FACTOR;
        this.MAX_INTERATIONS = MAX_INTERATIONS;
        this.TIPO_EQ = TIPO_EQ;
        this.xPos = xPos;
        this.yPos = yPos;
        start(real, imaginary);
    }

    public JuliaFractal(double real, double imaginary) {
        start(real, imaginary);
    }

    public JuliaFractal() {
        start(-0.67319, 0.34442);
    }

    public void saveImage(double r, double i) {
        File f = new File("(" + xPos + "," + yPos + ")_rangeReal_(" + RANGE_REAL_INI + "," + RANGE_REAL_FIM + ")_rangeImag_(" + RANGE_IMAG_INI + "," + RANGE_IMAG_FIM + ")_factor(" + FACTOR + ")_c(" + r + "," + i + "i).png");
        try {
            ImageIO.write(img, "PNG", f);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
