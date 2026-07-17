package com.siact.hydrocore.sec.utils;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 转换工具类
 */
public class ConvertUtils {
    private static Logger logger = LoggerFactory.getLogger(ConvertUtils.class);

    public static <T> T sourceToTarget(Object source, Class<T> target){
        if(source == null){
            return null;
        }
        T targetObject = null;
        try {
            targetObject = target.newInstance();
            BeanUtils.copyProperties(source, targetObject);
        } catch (Exception e) {
            logger.error("convert error ", e);
        }

        return targetObject;
    }

    public static <T> List<T> sourceToTarget(Collection<?> sourceList, Class<T> target){
        if(sourceList == null){
            return null;
        }

        List targetList = new ArrayList<>(sourceList.size());
        try {
            for(Object source : sourceList){
                T targetObject = target.newInstance();
                BeanUtils.copyProperties(source, targetObject);
                targetList.add(targetObject);
            }
        }catch (Exception e){
            logger.error("convert error ", e);
        }

        return targetList;
    }

    public static Map<String, Object> jsonToMap(JSONObject j){
        Map<String, Object> map = new HashMap<>();
        Iterator<String> iterator = j.keySet().iterator();
        while(iterator.hasNext())
        {
            String key = (String)iterator.next();
            Object value = j.get(key);
            map.put(key, value);
        }
        return map;
    }

    public static Map<String, Object> objToMap(Object obj){

        Map<String, Object> map = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        int len = fields.length;
        for(int i = 0; i < len; i++){
            Field field = fields[i];
            String varName = field.getName();
            varName = varName.toLowerCase();
            try {
                boolean accessFlag = field.isAccessible();
                field.setAccessible(true);
                Object o = field.get(obj);
                if(Objects.nonNull(o)){
                    map.put(varName, o.toString());
                }
                field.setAccessible(accessFlag);
            } catch (IllegalArgumentException ex){
                logger.warn("object field conversion skipped, field={}", varName, ex);
            } catch (IllegalAccessException ex){
                logger.warn("object field conversion denied, field={}", varName, ex);
            }
        }
        return map;
    }

    /**
     * 合并2个相同长度的List
     * @param tar
     * @param res
     * @return
     */
    public static List<String> addList(List<String> tar,List<String> res){
        int size = tar.size();
        for (int i = 0; i < size; i++) {
            tar.set(i,String.valueOf(Integer.parseInt(tar.get(i)) + Integer.parseInt(res.get(i))));
        }
        return tar;
    }


    //属性长码转实例长码
    public static String propDataCodeConvert(String propCode){
        return propCode.substring(0,propCode.length()-7)+"0000000";
    }
}
