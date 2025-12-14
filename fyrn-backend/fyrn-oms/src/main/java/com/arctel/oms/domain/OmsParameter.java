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

package com.arctel.oms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 系统参数表
 * @TableName oms_parameter
 */
@TableName(value ="oms_parameter")
@Data
public class OmsParameter {
    /**
     * 主键
     */
    @TableId
    private Long paramCode;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数类型
     */
    private String kindCode;

    /**
     * 参数值
     */
    private String paramValue;

    /**
     * 参数描述
     */
    private String paramDesc;

    /**
     * 是否启用
     */
    private Integer enabledFlag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        OmsParameter other = (OmsParameter) that;
        return (this.getParamCode() == null ? other.getParamCode() == null : this.getParamCode().equals(other.getParamCode()))
            && (this.getParamName() == null ? other.getParamName() == null : this.getParamName().equals(other.getParamName()))
            && (this.getKindCode() == null ? other.getKindCode() == null : this.getKindCode().equals(other.getKindCode()))
            && (this.getParamValue() == null ? other.getParamValue() == null : this.getParamValue().equals(other.getParamValue()))
            && (this.getParamDesc() == null ? other.getParamDesc() == null : this.getParamDesc().equals(other.getParamDesc()))
            && (this.getEnabledFlag() == null ? other.getEnabledFlag() == null : this.getEnabledFlag().equals(other.getEnabledFlag()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getParamCode() == null) ? 0 : getParamCode().hashCode());
        result = prime * result + ((getParamName() == null) ? 0 : getParamName().hashCode());
        result = prime * result + ((getKindCode() == null) ? 0 : getKindCode().hashCode());
        result = prime * result + ((getParamValue() == null) ? 0 : getParamValue().hashCode());
        result = prime * result + ((getParamDesc() == null) ? 0 : getParamDesc().hashCode());
        result = prime * result + ((getEnabledFlag() == null) ? 0 : getEnabledFlag().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", paramCode=").append(paramCode);
        sb.append(", paramName=").append(paramName);
        sb.append(", kindCode=").append(kindCode);
        sb.append(", paramValue=").append(paramValue);
        sb.append(", paramDesc=").append(paramDesc);
        sb.append(", enabledFlag=").append(enabledFlag);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}