package net.arctel.opkit.domain.enums;

public enum TRLangEnum {

    ZH("zh", "中文"),
    EN("en", "英文"),
    JA("ja", "日文"),
    KO("ko", "韩文"),
    FR("fr", "法文"),
    DE("de", "德文"),
    ES("es", "西班牙文"),
    RU("ru", "俄文"),
    IT("it", "意大利文"),
    PT("pt", "葡萄牙文"),
    NL("nl", "荷兰文"),
    SV("sv", "瑞典文"),
    NO("no", "挪威文"),
    DA("da", "丹麦文"),
    FI("fi", "芬兰文"),
    PL("pl", "波兰文"),
    TR("tr", "土耳其文"),
    AR("ar", "阿拉伯文"),
    HI("hi", "印地文"),
    TH("th", "泰文"),
    VI("vi", "越南文");

    private final String code;
    private final String description;

    TRLangEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TRLangEnum fromCode(String code) {
        for (TRLangEnum lang : TRLangEnum.values()) {
            if (lang.getCode().equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return null;
    }
}
