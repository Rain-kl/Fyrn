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

package com.arctel.oms.utils;

import com.arctel.oms.common.base.BaseQueryPage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PagingUtil {

    public static <T> BaseQueryPage<T> page(
            List<T> fullList,
            Integer pageNo,
            Integer pageSize,
            Comparator<T> comparator,      // 排序 lambda（默认升序）
            List<Predicate<T>> filters     // 过滤规则
    ) {
        List<T> source = fullList == null ? Collections.emptyList() : fullList;

        int pNo = (pageNo == null || pageNo < 1) ? 1 : pageNo;
        int pSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;

        // 1) 过滤
        Predicate<T> finalFilter = t -> true;
        if (filters != null) {
            for (Predicate<T> f : filters) {
                if (f != null) {
                    finalFilter = finalFilter.and(f);
                }
            }
        }

        List<T> filtered = source.stream()
                .filter(finalFilter)
                .collect(Collectors.toList());

        long total = filtered.size();

        // 2) 排序（lambda，默认升序）
        if (comparator != null) {
            filtered.sort(comparator);
        }

        // 3) 分页截取
        long fromL = (long) (pNo - 1) * pSize;
        if (fromL >= total) {
            return new BaseQueryPage<>(total, pSize, pNo, Collections.emptyList());
        }

        int from = (int) fromL;
        int to = (int) Math.min(fromL + pSize, total);

        List<T> rows = filtered.subList(from, to);
        return new BaseQueryPage<>(total, pSize, pNo, rows);
    }

    // 便捷重载（单过滤 + 单排序）
    public static <T> BaseQueryPage<T> page(
            List<T> fullList,
            Integer pageNo,
            Integer pageSize,
            Comparator<T> comparator,
            Predicate<T> filter
    ) {
        return page(
                fullList,
                pageNo,
                pageSize,
                comparator,
                filter == null ? Collections.emptyList() : Collections.singletonList(filter)
        );
    }
}