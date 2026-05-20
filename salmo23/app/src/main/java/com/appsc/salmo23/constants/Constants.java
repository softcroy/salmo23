package com.appsc.salmo23.constants;

public class Constants {

    public final static String STICKERS_DIRECTORY_PATH;

    static {
        String packageName = Constants.class.getPackage().getName().replaceAll(".constants","");
        STICKERS_DIRECTORY_PATH = "/data/data/" + packageName + "/files/";
    }
    public final static int STICKER_PACK_IDENTIFIER_LENGHT = 20;
}
