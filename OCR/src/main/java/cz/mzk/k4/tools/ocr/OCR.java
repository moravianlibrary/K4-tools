package cz.mzk.k4.tools.ocr;

import org.springframework.boot.SpringApplication;

/**
 * Created by holmanj on 12.6.15.
 */
public class OCR {
    public static void main(String [] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(
                BatchConfiguration.class, args)));
    }
}
