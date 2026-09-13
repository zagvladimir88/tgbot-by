package ru.zagvladimir.tgbot.integration.nbrb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NbrbRateDto(
        @JsonProperty("Cur_ID") int curId,
        @JsonProperty("Date") LocalDateTime date,
        @JsonProperty("Cur_Abbreviation") String abbreviation,
        @JsonProperty("Cur_Scale") int scale,
        @JsonProperty("Cur_Name") String name,
        @JsonProperty("Cur_OfficialRate") BigDecimal officialRate) {}
