package cn.joylau.plugins.translation;

import java.util.List;
import java.util.Map;

/**
 * Created by loucyin on 2016/3/25.
 */
public class Translation {
    private final static String US_PHONETIC = "us-phonetic";
    private final static String UK_PHONETIC = "uk-phonetic";
    private final static String PHONETIC = "phonetic";
    private final static String EXPLAINS = "explains";

    private final static int SUCCESS = 0;

    private String[] translation;
    private String query;
    private int errorCode;
    private Map<String, Object> basic;
    private List<Map<String, Object>> web;

    public String[] getTranslation() {
        return translation;
    }

    public void setTranslation(String[] translation) {
        this.translation = translation;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public Map<String, Object> getBasic() {
        return basic;
    }

    public void setBasic(Map<String, Object> basic) {
        this.basic = basic;
    }

    public List<Map<String, Object>> getWeb() {
        return web;
    }

    public void setWeb(List<Map<String, Object>> web) {
        this.web = web;
    }

    private String getErrorMessage() {
        switch (errorCode) {
            case SUCCESS:
                return "成功";
        }
        return "参考错误代码列表: https://ai.youdao.com/DOCSIRMA/html/trans/api/wbfy/index.html#section-9";
    }

    private String getPhonetic() {
        if (basic == null) {
            return null;
        }
        String phonetic = null;
        String us_phonetic = (String) basic.get(US_PHONETIC);
        String uk_phonetic = (String) basic.get(UK_PHONETIC);
        if (us_phonetic == null && uk_phonetic == null && basic.get(PHONETIC) != null) {
            phonetic = "拼音：[" + basic.get(PHONETIC) + "];";
        } else {
            if (us_phonetic != null) {
                phonetic = "美式：[" + us_phonetic + "];";
            }
            if (uk_phonetic != null) {
                if (phonetic == null) {
                    phonetic = "";
                }
                phonetic = phonetic + "英式：[" + uk_phonetic + "];";
            }
        }
        return phonetic;
    }

    private String getExplains() {
        if (basic == null) {
            return null;
        }
        String result = null;
        List<String> explains = (List<String>) basic.get(EXPLAINS);
        if (explains.size() > 0) {
            result = "";
        }
        for (String explain : explains) {
            result += explain + "\n";
        }
        return result;
    }

    private String getTranslationResult() {
        if (translation == null) {
            return null;
        }
        String result = null;
        if (translation.length > 0) {
            result = "";
        }
        for (String r : translation) {
            result += (r + ";");
        }
        return result;
    }

    private String getWebResult() {
        if (web == null) {
            return null;
        }
        String result = null;
        if (web.size() > 0) {
            result = "";
        }
        for (Map<String, Object> map : web) {
            String key = (String) map.get("key");
            result += (key + " : ");
            List<String> value = (List<String>) map.get("value");
            for (String str : value) {
                result += (str + ",");
            }
            result += "\n";
        }
        return result;
    }

    private boolean isSentence() {
        return query.trim().contains(" ");
    }

    @Override
    public String toString() {
        String string = null;
        if (errorCode != SUCCESS) {
            string = "错误代码：" + errorCode + "\n" + getErrorMessage();
        } else {
            String translation = getTranslationResult();
            if (translation != null) {
                translation = translation.substring(0, translation.length() - 1);
                if (!translation.equals(query)) {
                    if (isSentence()) {
                        string = getTranslationResult() + "\n";
                    } else {
                        string = (query + ":" + getTranslationResult() + "\n");
                    }
                }
            }
            if (getPhonetic() != null) {
                if (string == null) {
                    string = "";
                }
                string += (getPhonetic() + "\n");
            }
            if (getExplains() != null) {
                if (string == null) {
                    string = "";
                }
                string += (getExplains());
            }
            if (getWebResult() != null) {
                if (string == null) {
                    string = "";
                }
                string += "网络释义：\n";
                string += (getWebResult());
            }
        }
        if (string == null) {
            string = "你选的内容：" + query + "\n抱歉,翻译不了...";
        }
        return string;
    }
}
