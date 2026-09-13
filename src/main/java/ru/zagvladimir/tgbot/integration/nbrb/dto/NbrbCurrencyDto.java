package ru.zagvladimir.tgbot.integration.nbrb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record NbrbCurrencyDto(
        @JsonProperty("Cur_ID") int curId,
        @JsonProperty("Cur_Abbreviation") String abbreviation,
        @JsonProperty("Cur_Name") String name,
        @JsonProperty("Cur_Scale") int scale,
        @JsonProperty("Cur_DateStart") LocalDateTime dateStart,
        @JsonProperty("Cur_DateEnd") LocalDateTime dateEnd) {}
