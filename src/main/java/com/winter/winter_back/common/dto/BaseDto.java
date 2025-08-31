package com.winter.winter_back.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseDto {
    @JsonProperty("pNum")
    @NotNull(message = "페이지 번호는 필수입니다.")
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    public Integer pNum;
    @JsonProperty("pSize")
    @NotNull(message = "페이지 크기는 필수입니다.")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    public Integer pSize;
    @JsonProperty("pIndex")
    @JsonIgnore
    public Integer pIndex;
    @JsonProperty("keyword")
    public String keyword;

    /* ========================= Getter / Setter ========================= */
    public Integer getpIndex() {
        if (this.pNum != null && this.pSize != null)
            return (this.pNum - 1) * this.pSize;

        return pIndex;
    }
}
