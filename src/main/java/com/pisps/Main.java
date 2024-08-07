package com.pisps;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        CrptApi api = new CrptApi(10, TimeUnit.SECONDS);
        for (int i = 0; i<30; i++){
            Thread.sleep(30);
            CrptApi.Document randomDocument = new CrptApi.Document(
                    "gfdergvc",
                    "gfvby5434543",
                    "Drdefgdsdft",
                    "fecgtdvf",
                    true,
                    "gredfre432",
                    "fderfg43234",
                    "fed43",
                    "rfedg43",
                    "vgfredf543223",
                    List.of(new CrptApi.Product(
                            "ab13",
                            "frdvg5432345",
                            "dftrew",
                            "trefgf",
                            "gfdsdfg",
                            "gfdfgfd",
                            "fddgfd",
                            "gfdgfd",
                            "fdgfd"
                    )),
                    "gfrdfvgt5432",
                    "gfet9876543"
            );
            api.createDoc(randomDocument,"trfgtr");
        }
    }
}