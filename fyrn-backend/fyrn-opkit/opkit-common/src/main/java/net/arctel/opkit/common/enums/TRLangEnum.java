/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.arctel.opkit.common.enums;

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
