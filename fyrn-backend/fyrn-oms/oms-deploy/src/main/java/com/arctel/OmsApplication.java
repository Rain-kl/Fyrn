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

package com.arctel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@MapperScan("com.arctel.oms.mapper")
class OmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmsApplication.class, args);
    }

    //TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
    // 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
    public static class Main {
        public static void main(String[] args) {
            //TIP 当文本光标位于高亮显示的文本处时按 <shortcut actionId="ShowIntentionActions"/>
            // 查看 IntelliJ IDEA 建议如何修正。
            System.out.printf("Hello and welcome!");

            for (int i = 1; i <= 5; i++) {
                //TIP 按 <shortcut actionId="Debug"/> 开始调试代码。我们已经设置了一个 <icon src="AllIcons.Debugger.Db_set_breakpoint"/> 断点
                // 但您始终可以通过按 <shortcut actionId="ToggleLineBreakpoint"/> 添加更多断点。
                System.out.println("i = " + i);
            }
        }
    }
}
