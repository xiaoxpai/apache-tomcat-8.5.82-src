package org.apache.ux;

/**
 * 抽象方法：
 *     - 面向功能的（对象的行为）
 *     - 有构造器
 *     - 只能单继承
 */
public abstract class Win {

    abstract void addWindows();
    abstract void addWindows(int size,int width,int hight);

    public Win() {
    }
}
