/**
 * 
 */
package io.vilya.helium;

import java.awt.List;
import java.awt.datatransfer.Clipboard;
import javax.lang.model.SourceVersion;

/**
 *
 * @author zhukuanxin <cafedada@vilya.io>
 * @created 2020-11-30 07:47:44
 */
public class Test1 {

    
    public static void main(String[] args) {
        System.out.println(List.class.getClassLoader());
        System.out.println(Test1.class.getClassLoader());
        System.out.println(SourceVersion.class.getClassLoader());
        System.out.println(Clipboard.class.getClassLoader());
    }
    
}
