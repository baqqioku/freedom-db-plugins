package com.fc.platform.commons.util;

import com.fasterxml.uuid.Generators;

/**
 * Created by rocky on 2015/10/13.
 */
public class GeneralUUID {

    private GeneralUUID(){}

    public static String UUID() {
        return Generators.randomBasedGenerator().generate().toString();
    }

}
