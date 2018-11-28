package com.swg.springcloudeureka;

/**
 * @Author 【swg】.
 * @Date 2018/11/28 14:55
 * @DESC
 * @CONTACT 317758022@qq.com
 */
public class Book {
    private String name;
    private int price;

    public Book(){}

    public Book(String name, int price){
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
