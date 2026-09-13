package ru.zagvladimir.tgbot.integration.nbrb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NbrbDynamicsDto(
        @JsonProperty("Cur_ID") int curId,
        @JsonProperty("Date") LocalDateTime date,
        @JsonProperty("Cur_OfficialRate") BigDecimal officialRate) {}
