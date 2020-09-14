package com.botmasterzzz.social.dto;

import com.google.common.base.Objects;

import java.io.Serializable;

public class KafkaKeyDTO implements Serializable {

    private Long instanceKey;
    private Integer updateId;
    private String fileName;
    private boolean loading;

    public Long getInstanceKey() {
        return instanceKey;
    }

    public void setInstanceKey(Long instanceKey) {
        this.instanceKey = instanceKey;
    }

    public Integer getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    @Override
    public String toString() {
        return "KafkaKeyDTO{" +
                "instanceKey=" + instanceKey +
                ", updateId=" + updateId +
                ", fileName='" + fileName + '\'' +
                ", loading=" + loading +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaKeyDTO that = (KafkaKeyDTO) o;
        return loading == that.loading &&
                Objects.equal(instanceKey, that.instanceKey) &&
                Objects.equal(updateId, that.updateId) &&
                Objects.equal(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(instanceKey, updateId, fileName, loading);
    }
}
