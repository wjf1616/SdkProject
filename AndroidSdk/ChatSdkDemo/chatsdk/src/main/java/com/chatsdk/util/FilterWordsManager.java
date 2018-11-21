package com.chatsdk.util;

import android.util.Log;

import com.chatsdk.model.TimeManager;
import com.chatsdk.model.UserManager;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by lzh on 17/2/22.
 */
public class FilterWordsManager {

    @SuppressWarnings("rawtypes")
    public static Map sensitiveWordMap = null;
    public static int minMatchTYpe = 1;
    public static int maxMatchType = 2;
    public static ArrayList<String> patternForbiddenWords = new ArrayList<String>();
    public static ArrayList<String> patternForSafeWords = new ArrayList<String>();
    public static ArrayList<String> patternBadWords = new ArrayList<String>();
    static final char DBC_CHAR_START = 33; // 半角!
    static final char DBC_CHAR_END = 126; // 半角~
    static final char SBC_CHAR_START = 65281; // 全角！
    static final char SBC_CHAR_END = 65374; // 全角～
    static final int CONVERT_STEP = 65248; // 全角半角转换间隔
    static final char SBC_SPACE = 12288; // 全角空格 12288
    static final char DBC_SPACE = ' '; // 半角空格

    /**
     * 是否包含违禁词汇，不论中间是否有其他字符 eg： (abcdefg) 包含 (adf)
     * 检查逻辑：遍历检查adf的每个字符在abcdefg中的index，如果d的index >
     * a的index，继续遍历，如果每个字符的index都大于上一个，则为true
     * 如果遍历中途发现index为-1（不包含这个字符）或小于上一个（顺序不对）,则返回false
     */
    public static boolean containsForbiddenWords( String msg)
    {
        String checkingStr = msg.toLowerCase().trim().replaceAll(" +", " ");
        // 回车换行回避
        checkingStr = checkingStr.replace("\n","");

        for (String word : patternForbiddenWords)
        {
            // 正则表达式前加{<level},来实现等级限制
            int level = 0;
            String levelStr = "";
            if (word.length() > 5 && word.substring(0, 2).equals("{<"))
            {
                try
                {
                    if (word.substring(3, 4).equals("}"))
                    {
                        levelStr = word.substring(0, 4);
                        level = Integer.parseInt(word.substring(2, 3));
                    }
                    else if (word.substring(4, 5).equals("}"))
                    {
                        levelStr = word.substring(0, 5);
                        level = Integer.parseInt(word.substring(2, 4));
                    }
                }
                catch (Exception e)
                {

                }
            }
            if (level != 0)
            {
                if (UserManager.getInstance().getCurrentUser().level < level)
                {
                    word = word.substring(levelStr.length());
                    Pattern pattern = Pattern.compile(word);
                    if (pattern.matcher(qj2bj(checkingStr)).find())
                    {
                        Log.d("containsForbiddenWords" ,"[msg]="+ msg + " [- forbidden word:]" + word);
                        return true;
                    }
                }
            }
            else
            {
                Pattern pattern = Pattern.compile(word);
                if (pattern.matcher(qj2bj(checkingStr)).find())
                {
                    Log.d("containsForbiddenWords" ,"[msg]="+ msg + " [- forbidden word:]" + word);
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 是否包含安全提示词汇，不论中间是否有其他字符 eg： (abcdefg) 包含 (adf)
     * 检查逻辑：遍历检查adf的每个字符在abcdefg中的index，如果d的index >
     * a的index，继续遍历，如果每个字符的index都大于上一个，则为true
     * 如果遍历中途发现index为-1（不包含这个字符）或小于上一个（顺序不对）,则返回false
     */
    public static boolean containsForSafeWords( String msg)
    {
        String checkingStr = msg.toLowerCase().trim().replaceAll(" +", " ");
        // 回车换行回避
        checkingStr = checkingStr.replace("\n","");

        for (String word : patternForSafeWords)
        {

            Pattern pattern = Pattern.compile(word);
            if (pattern.matcher(qj2bj(checkingStr)).find())
            {
                Log.d("containsForSafeWords" ,"[msg]="+ msg + " [- safe word:]" + word);
                return true;
            }
        }
        return false;
    }

    /**
     * 全角转半角
     * @param src
     * @return
     */
    public static String qj2bj(String src) {
        if (src == null) {
            return src;
        }
        StringBuilder buf = new StringBuilder(src.length());
        char[] ca = src.toCharArray();
        for (int i = 0; i < src.length(); i++) {
            if (ca[i] >= SBC_CHAR_START && ca[i] <= SBC_CHAR_END) {
                buf.append((char) (ca[i] - CONVERT_STEP));
            } else if (ca[i] == SBC_SPACE) {
                buf.append(DBC_SPACE);
            } else {
                buf.append(ca[i]);
            }
        }
        return buf.toString();
    }

    public static void initBadWords(String badWords){
        try {
            String[] keyWordSet = badWords.split(",");;
            sensitiveWordMap = addSensitiveWordToHashMap(keyWordSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ȡ���дʿ⣬�����дʷ���HashSet�У�����һ��DFA�㷨ģ�ͣ�<br>
     * �� = {
     *      isEnd = 0
     *      �� = {<br>
     *      	 isEnd = 1
     *           �� = {isEnd = 0
     *                �� = {isEnd = 1}
     *                }
     *           ��  = {
     *           	   isEnd = 0
     *           		�� = {
     *           			 isEnd = 1
     *           			}
     *           	}
     *           }
     *      }
     *  �� = {
     *      isEnd = 0
     *      �� = {
     *      	isEnd = 0
     *      	�� = {
     *              isEnd = 0
     *              �� = {
     *                   isEnd = 1
     *                  }
     *              }
     *      	}
     *      }
     * @author chenming
     * @date 2014��4��20�� ����3:04:20
     * @param keyWordSet  ���дʿ�
     * @version 1.0
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Map addSensitiveWordToHashMap(String[] keyWordSet) {
        sensitiveWordMap = new HashMap(keyWordSet.length);
        Map nowMap = null;
        Map<String, String> newWorMap = null;
        for (String key:keyWordSet){
            nowMap = sensitiveWordMap;
            for(int i = 0 ; i < key.length() ; i++){
                char keyChar = key.charAt(i);
                Object wordMap = nowMap.get(keyChar);

                if(wordMap != null){
                    nowMap = (Map) wordMap;
                }
                else{
                    newWorMap = new HashMap<String,String>();
                    newWorMap.put("isEnd", "0");
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }

                if(i == key.length() - 1){
                    nowMap.put("isEnd", "1");
                }
            }
        }
        return sensitiveWordMap;
    }


    public static String replaceBadWords(String nickName)
    {
        Iterator<String> it = patternBadWords.iterator();
        Pattern pattern = Pattern.compile("\\W");
        String text = nickName;
        long beginTime = TimeManager.getInstance().getCurrentTimeMS();
        try
        {
            while (it.hasNext())
            {
                String badword = it.next();
                if(StringUtils.isEmpty(badword))
                    continue;
                if (pattern.matcher(badword).find())
                { // 如果脏话库里面不是英文单词构成，则不用分词，整句替换
                    try {
                        int length = badword.length();
                        StringBuilder regexBuilder = new StringBuilder();
                        for (int i = 0; i < length; i++) {
                            regexBuilder.append(badword.charAt(i));
                            regexBuilder.append("\\s*");
                        }
                        String regex = regexBuilder.toString();
                        Pattern pattern1 = Pattern.compile(regex);
                        if (pattern1.matcher(nickName).find()) {
                            nickName = pattern1.matcher(nickName).replaceAll("**");
                        }
                    }catch(Exception e){

                    }

                }
                else
                {
                    try {
                        // 如果是英文单词类似的语法，则屏蔽整个单词，而不是屏蔽单词里匹配的部分
                        Pattern pattern2 = Pattern.compile("\\b(?i)" + badword + "\\b");
                        if (pattern2.matcher(nickName).find()) {
                            nickName = pattern2.matcher(nickName).replaceAll("**");
                        }
                    }catch(Exception e){

                    }
                }

            }
        }
        catch (Exception e)
        {
        }
        long endTime = TimeManager.getInstance().getCurrentTimeMS();
        System.out.print("消耗的时间为:"+(endTime -beginTime) + "原字符串为:"+text);
        return nickName;
    }

    /**
     * �ж������Ƿ���������ַ�
     * @author chenming
     * @date 2014��4��20�� ����4:28:30
     * @param txt  ����
     * @param matchType  ƥ�����&nbsp;1����Сƥ�����2�����ƥ�����
     * @return ����������true�������false
     * @version 1.0
     */
    public boolean isContaintSensitiveWord(String txt,int matchType){
        boolean flag = false;
        for(int i = 0 ; i < txt.length() ; i++){
            int matchFlag = this.CheckSensitiveWord(txt, i, matchType);
            if(matchFlag > 0){
                flag = true;
            }
        }
        return flag;
    }

    public static Set<String> getSensitiveWord(String txt , int matchType){
        Set<String> sensitiveWordList = new HashSet<String>();

        for(int i = 0 ; i < txt.length() ; i++){
            int length = CheckSensitiveWord(txt, i, matchType);
            if(length > 0){
                sensitiveWordList.add(txt.substring(i, i+length));
                i = i + length - 1;
            }
        }

        return sensitiveWordList;
    }

    /**
     * �滻�������ַ�
     * @author chenming
     * @date 2014��4��20�� ����5:12:07
     * @param txt
     * @param matchType
     * @param replaceChar �滻�ַ���Ĭ��*
     * @version 1.0
     */
    public static String replaceSensitiveWord(String txt,int matchType,String replaceChar){
        String resultTxt = txt;
        try {
            Set<String> set = getSensitiveWord(txt, matchType);
            Iterator<String> iterator = set.iterator();
            String word = null;
            String replaceString = null;
            while (iterator.hasNext()) {
                word = iterator.next();
                replaceString = getReplaceChars(replaceChar, word.length());
                resultTxt = resultTxt.replaceAll(word, replaceString);
            }
        }catch (Exception e){
//            Log.e(LogUtil.TAG_DEBUG,e);
        }

        return resultTxt;
    }

    /**
     * ��ȡ�滻�ַ���
     * @author chenming
     * @date 2014��4��20�� ����5:21:19
     * @param replaceChar
     * @param length
     * @return
     * @version 1.0
     */
    private static String getReplaceChars(String replaceChar,int length){
        String resultReplace = replaceChar;
        for(int i = 1 ; i < length ; i++){
            resultReplace += replaceChar;
        }

        return resultReplace;
    }

    /**
     * ����������Ƿ���������ַ������������£�<br>
     * @author chenming
     * @date 2014��4��20�� ����4:31:03
     * @param txt
     * @param beginIndex
     * @param matchType
     * @return��������ڣ�������д��ַ��ĳ��ȣ������ڷ���0
     * @version 1.0
     */
    @SuppressWarnings({ "rawtypes"})
    public static int CheckSensitiveWord(String txt,int beginIndex,int matchType){
        boolean  flag = false;
        int matchFlag = 0;
        char word = 0;
        Map nowMap = sensitiveWordMap;
        for(int i = beginIndex; i < txt.length() ; i++){
            word = txt.charAt(i);
            nowMap = (Map) nowMap.get(word);
            if(nowMap != null){
                matchFlag++;
                if("1".equals(nowMap.get("isEnd"))){
                    flag = true;
                    if(FilterWordsManager.minMatchTYpe == matchType){
                        break;
                    }
                }
            }
            else{
                break;
            }
        }
        if(matchFlag < 2 || !flag){
            matchFlag = 0;
        }
        return matchFlag;
    }
}
