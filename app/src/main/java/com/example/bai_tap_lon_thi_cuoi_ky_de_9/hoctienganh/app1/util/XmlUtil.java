package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util;

import androidx.annotation.NonNull;
import androidx.core.util.Predicate;


import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.exception.NotEnoughItemsException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Các hàm thuật toán cho <code>NodeList</code> và <code>Element</code> trong XML
 */
public final class XmlUtil {
    private XmlUtil() {}

    /**
     * Chuyển <code>NodeList</code> sang mảng các <code>Element</code>
     */
    @NonNull
    public static Element[] toArray(@NonNull NodeList nodeList) {
        Element[] array = new Element[nodeList.getLength()];
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element elem = (Element)nodeList.item(i);
            array[i] = elem;
        }
        return array;
    }

    /**
     * Chuyển <code>NodeList</code> sang mảng các <code>Element</code> và lọc theo điều kiện
     */
    @NonNull
    public static Element[] toArray(@NonNull NodeList nodeList, Predicate<Element> filter) {
        List<Element> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element elem = (Element)nodeList.item(i);
            if (!filter.test(elem)) continue;
            list.add(elem);
        }
        return list.toArray(new Element[list.size()]);
    }

    /**
     * Chuyển <code>NodeList</code> sang mảng các <code>Element</code>
     */
    @NonNull
    public static List<Element> asList(@NonNull NodeList nodeList) {
        List<Element> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element elem = (Element)nodeList.item(i);
            list.add(elem);
        }
        return list;
    }

    /**
     * Chuyển <code>NodeList</code> sang mảng các <code>Element</code> và lọc theo điều kiện
     */
    @NonNull
    public static List<Element> asList(@NonNull NodeList nodeList, Predicate<Element> pred) {
        List<Element> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element elem = (Element)nodeList.item(i);
            if (!pred.test(elem)) continue;
            list.add(elem);
        }
        return list;
    }

    /**
     * Lấy giá trị của các <code>&lt;item></code> từ <code>NodeList</code>
     */
    @NonNull
    public static String[] getItems(@NonNull Element element) {
        NodeList nodeList = element.getElementsByTagName("item");
        String[] array = new String[nodeList.getLength()];
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element elem = (Element)nodeList.item(i);
            array[i] = elem.getTextContent();
        }
        return array;
    }

    /**
     * Lấy giá trị của các <code>&lt;item></code> từ <code>NodeList</code>
     */
    @NonNull
    public static List<String> getItemsList(@NonNull Element element) {
        NodeList nodeList = element.getElementsByTagName("item");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element elem = (Element)nodeList.item(i);
            list.add(elem.getTextContent());
        }
        return list;
    }

    /**
     * Chọn ra một <code>Element</code> ngẫu nhiên từ <code>NodeList</code>
     */
    @NonNull
    public static Element pickRandom(@NonNull NodeList nodeList) {
        if (nodeList.getLength() == 0) throw new NotEnoughItemsException(nodeList.getLength(), 1);
        return (Element)nodeList.item((int)(Math.random() * nodeList.getLength()));
    }

    /**
     * Chọn ra <code>n</code> <code>Element</code> ngẫu nhiên từ <code>NodeList</code>
     */
    @NonNull
    public static Element[] pickRandom(@NonNull NodeList nodeList, int n) {
        return Arrays.pickRandom(toArray(nodeList), n);
    }
}
