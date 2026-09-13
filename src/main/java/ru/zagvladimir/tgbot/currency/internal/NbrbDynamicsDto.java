package ru.zagvladimir.tgbot.currency.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

record NbrbDynamicsDto(
        @JsonProperty("Cur_ID") int curId,
        @JsonProperty("Date") LocalDateTime date,
        @JsonProperty("Cur_OfficialRate") BigDecimal officialRate) {}
