package com.quadbaze.x2c;

import com.quadbaze.x2c.common.Container;
import com.quadbaze.x2c.core.Transformer;

/**
 * Created by Olatunji O. Longe on 04/05/2018 3:24 PM
 */
public class Executor {

    public static void main(String... args){
        try {
            (new Transformer()).extractExcelAndTransformToCsv(Container.SOURCE, Container.DESTINATION);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
